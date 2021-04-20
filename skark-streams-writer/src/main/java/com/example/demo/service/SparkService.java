package com.example.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.conf.Configuration;
import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaStreamingContext;

@Slf4j
public class SparkService {

    private static final String APP_NAME = "Spark Streams Writer";

    private static final String CONF_RATE = "spark.streaming.rate";
    private static final String CONF_CHECKPOINT_DIR = "spark.streaming.checkpoint.dir";

    private static final String CONF_WINDOW_WIDTH = "spark.streaming.window.width";
    private static final String CONF_WINDOW_RATE = "spark.streaming.window.rate";

    private static final String CONF_AVRO_PATH = "spark.streaming.avro.path";

    private static final String CONF_HASHTAG_LENGTH_MAX = "spark.app.hashtag.length.max";
    private static final String CONF_HASHTAG_COUNT_MIN = "spark.app.hashtag.count.min";

    private static final String CONF_KAFKA_TOPIC_NAME = "spark.streaming.kafka.topic.name";
    private static final String CONF_KAFKA_ADDRESS = "spark.streaming.kafka.address";

    private static volatile JavaStreamingContext streamingContext;

    public static JavaStreamingContext getStreamingContext() {
        if (streamingContext == null) {
            synchronized (SparkService.class) {
                if (streamingContext == null) {
                    // don't mess with order of init
                    SparkConf sparkConf = new SparkConf();
                    sparkConf.setAppName(APP_NAME);
                    Duration seconds = Durations.seconds(Integer.valueOf(sparkConf.get(CONF_RATE)));
                    streamingContext = new JavaStreamingContext(sparkConf, seconds);
                    streamingContext.checkpoint(getCheckpointDir());
                }
            }
        }
        return streamingContext;
    }

    public static void setStreamingContext(JavaStreamingContext streamingContext) {
        synchronized (SparkService.class) {
            SparkService.streamingContext = streamingContext;
        }
    }

    public static String getCheckpointDir() {
        return getConf(CONF_CHECKPOINT_DIR);
    }

    public static Duration getStreamingRate() {
        return Durations.seconds(SparkService.getConfInt(CONF_RATE));
    }

    public static Duration getWindowWidth() {
        return Durations.seconds(SparkService.getConfInt(CONF_WINDOW_WIDTH));
    }

    public static Duration getWindowRate() {
        return Durations.seconds(SparkService.getConfInt(CONF_WINDOW_RATE));
    }

    public static String getAvroPath() {
        return getConf(CONF_AVRO_PATH);
    }

    public static Integer getHashtagLengthMax() {
        return getConfInt(CONF_HASHTAG_LENGTH_MAX);
    }

    public static Integer getHashtagCountMin() {
        return getConfInt(CONF_HASHTAG_COUNT_MIN);
    }

    public static String getKafkaTopicName() {
        return getConf(CONF_KAFKA_TOPIC_NAME);
    }

    public static String getKafkaAddress() {
        return getConf(CONF_KAFKA_ADDRESS);
    }

    public static Integer getConfInt(String name) {
        return Integer.valueOf(getConf(name));
    }

    public static String getConf(String name) {
        return getConf(getStreamingContext().sparkContext().getConf(), name);
    }

    public static String getConf(SparkConf sparkConf, String name) {
        return sparkConf.get(name);
    }
}