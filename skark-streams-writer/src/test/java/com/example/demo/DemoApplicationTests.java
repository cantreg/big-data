package com.example.demo;

import com.example.demo.dto.Message;
import com.example.demo.service.AvroWriterService;
import com.example.demo.service.SparkService;
import com.mongodb.spark.MongoSpark;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.commons.io.FileUtils;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import static org.awaitility.Awaitility.await;

@Slf4j
public class DemoApplicationTests {

    private static final String CHECKPOINT_DIR = "./checkpoint";
    private static final String AVRO_PATH = "./build/tmp/avro";

    private static JavaStreamingContext streamingContext;
    private static JavaSparkContext sparkContext;

    private static MongodExecutable mongodExe;
    private static MongodProcess mongod;

    @BeforeAll
    public static void setup() {
        setupEmbeddedMongo();
        setupLocalSpark();
    }

    @SneakyThrows
    private static void setupEmbeddedMongo() {
        MongodStarter starter = MongodStarter.getDefaultInstance();
        MongodConfig mongodConfig = MongodConfig.builder()
                .version(Version.Main.PRODUCTION)
                .net(new Net(Network.getFreeServerPort(), false))
                .build();
        mongodExe = starter.prepare(mongodConfig);
        mongod = mongodExe.start();
    }

    private static void setupLocalSpark() {
        SparkConf sparkConf = new SparkConf();
        String mongoAddress = String.format("mongodb://localhost:%d/admin.ratings", mongod.getConfig().net().getPort());
        sparkConf.setMaster("local[2]")
                .setAppName("Test App")
                .set("spark.mongodb.input.uri", mongoAddress)
                .set("spark.mongodb.output.uri", mongoAddress)
                .set("spark.sql.streaming.checkpointLocation", CHECKPOINT_DIR)
                .set("spark.sql.streaming.forceDeleteTempCheckpointLocation", "true")
                .set("spark.streaming.avro.path", AVRO_PATH);
        streamingContext = new JavaStreamingContext(sparkConf, Durations.seconds(1));
        streamingContext.checkpoint(CHECKPOINT_DIR);
        sparkContext = streamingContext.sparkContext();
        SparkService.setStreamingContext(streamingContext);
    }

    @AfterAll
    public static void tearDown() throws IOException {
        mongod.stop();
        mongodExe.stop();
        streamingContext.stop();
        FileUtils.deleteDirectory(new File(CHECKPOINT_DIR));
        FileUtils.deleteQuietly(new File(AVRO_PATH));
    }

    @Test()
    public void testStreams() {
        log.info("Start");
        JavaPairDStream<String, Long> ratings = SparkStreamWriter.getRatings(getTestDStream(),
                Durations.seconds(3), Durations.seconds(1),
                2, 5);
        SparkStreamWriter.saveRatingsToMongoDB(ratings);
        streamingContext.start();
        await().until(() -> MongoSpark.load(sparkContext).count() >= 6); // 2 hash tags every second x 3 seconds
        log.info(MongoSpark.load(sparkContext).toDF().collectAsList().toString());
    }

    @Test
    public void testAvro() throws Exception {
        DatumWriter<Message> messageDatumWriter = new SpecificDatumWriter<>(Message.class);
        String pathConfString = SparkService.getAvroPath();
        File file = new File(pathConfString);
        // create
        DataFileWriter<Message> fileWriter = new DataFileWriter<>(messageDatumWriter);
        fileWriter.create(Message.SCHEMA$, file);
        AvroWriterService.setDataFileWriter(fileWriter);
        AvroWriterService.write(Message.builder().id(1L).hashtag("#test").text("text").build());
        fileWriter.close();
        // append
        fileWriter = new DataFileWriter<>(messageDatumWriter);
        fileWriter.appendTo(file);
        AvroWriterService.setDataFileWriter(fileWriter);
        AvroWriterService.write(Message.builder().id(2L).hashtag("#test2").text("text2").build());
        fileWriter.close();
    }

    private JavaDStream<Message> getTestDStream() {

        JavaRDD<Message> messages = sparkContext.parallelize(
                Arrays.asList(
                        new Message("#test"),
                        new Message("#test"),
                        new Message("#test"),
                        new Message("#bar"),
                        new Message("#test5"),
                        new Message("#test5"),
                        new Message("#test5"),
                        new Message("#foo"),
                        new Message("#foo")
                ));

        Queue q = new LinkedList<JavaRDD<Message>>();
        q.add(messages);
        JavaDStream javaDStream = streamingContext.queueStream(q);
        return javaDStream;
    }

}
