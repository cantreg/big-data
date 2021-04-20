package com.example.demo;

import com.example.demo.dto.Message;
import com.example.demo.service.AvroWriterService;
import com.example.demo.service.HashtagDocumentService;
import com.example.demo.service.KafkaStreamerService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;

import static com.example.demo.service.SparkService.*;

@Slf4j
public class SparkStreamWriter {

    @SneakyThrows
    public static void main(String[] args) {
        try {
            log.info("getMessageJavaDStream");
            JavaDStream<Message> messagesDS = KafkaStreamerService.getMessageJavaDStream();

            log.info("writeToAvro");
            writeToAvro(messagesDS);

            log.info("getRatings");
            JavaPairDStream<String, Long> ratings = getRatings(
                    messagesDS, getWindowWidth(), getWindowRate(),
                    getHashtagCountMin(),
                    getHashtagLengthMax());

            log.info("saveRatingsToMongoDB");
            saveRatingsToMongoDB(ratings);

            getStreamingContext().start();
            log.info("await termination");
            getStreamingContext().awaitTerminationOrTimeout(30000);
            log.info("terminated");
        } catch (Exception e) {
            log.error("end.", e);
            throw e;
        } finally {
            AvroWriterService.getDataFileWriter().close();
        }
    }
    /**
     * Filters the most popular short hashtags in recent data from stream.
     * Repeats every couple of seconds.
     *
     * @param messagesDS       stream of messages
     * @param windowWidth      amount of data to analyze (in time units before the last message)
     * @param windowRate       how often to repeat calculation
     * @param hashtagMinCount  minimum count of the same tag
     * @param hashtagMaxLength maximum length of tag in characters
     * @return stream of pair (tag, count)
     */
    public static JavaPairDStream<String, Long> getRatings(JavaDStream<Message> messagesDS,
                                                           Duration windowWidth, Duration windowRate,
                                                           int hashtagMinCount, int hashtagMaxLength) {
        return messagesDS
                .map(Message::getHashtag)
                .filter(hashtag -> hashtag.length() <= hashtagMaxLength)
                .countByValueAndWindow(windowWidth, windowRate)
                .filter(hashtagsCount -> hashtagsCount._2 >= hashtagMinCount)
                .reduceByKey(Long::sum);
    }

    private static void writeToAvro(JavaDStream<Message> messagesDS) {
        messagesDS.foreachRDD((rdd) -> rdd.foreach(AvroWriterService::write));
    }

    public static void saveRatingsToMongoDB(JavaPairDStream<String, Long> ratings) {
        ratings.foreachRDD(rdd -> {
            if (!rdd.isEmpty()) {
                HashtagDocumentService.save(rdd);
            } else {
                log.warn("no new messages for mongo");
            }
        });
    }


}
