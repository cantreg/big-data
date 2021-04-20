package com.example.demo.service;

import com.example.demo.dto.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.example.demo.service.SparkService.getStreamingContext;
import static org.apache.kafka.clients.consumer.ConsumerConfig.*;

@Slf4j
public class KafkaStreamerService {

    private static final String GROUP_ID_STREAM = "use_a_separate_group_id_for_each_stream";
    private static final String OFFSET_LATEST = "latest";

    public static JavaDStream<Message> getMessageJavaDStream() {

        String kafkaAddress = SparkService.getKafkaAddress();
        String topicName = SparkService.getKafkaTopicName();
        JavaInputDStream<ConsumerRecord<String, String>> messagesStream =
                getDirectStream(getKafkaProperties(kafkaAddress), Arrays.asList(topicName));

        ObjectMapper mapper = new ObjectMapper();
        JavaDStream<String> json = messagesStream.map(ConsumerRecord::value);
        return json.map(value -> mapper.readValue(value, Message.class));
    }

    private static Map<String, Object> getKafkaProperties(String kafkaAddress) {
        Map<String, Object> kafkaParams = new HashMap<>();
        kafkaParams.put(BOOTSTRAP_SERVERS_CONFIG, kafkaAddress);
        kafkaParams.put(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        kafkaParams.put(VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        kafkaParams.put(GROUP_ID_CONFIG, GROUP_ID_STREAM);
        kafkaParams.put(AUTO_OFFSET_RESET_CONFIG, OFFSET_LATEST);
        kafkaParams.put(ENABLE_AUTO_COMMIT_CONFIG, false);
        return kafkaParams;
    }

    private static JavaInputDStream<ConsumerRecord<String, String>> getDirectStream(
            Map<String, Object> kafkaParams, Collection<String> topics) {
        return KafkaUtils.createDirectStream(
                getStreamingContext(),
                LocationStrategies.PreferConsistent(),
                ConsumerStrategies.Subscribe(topics, kafkaParams));
    }
}
