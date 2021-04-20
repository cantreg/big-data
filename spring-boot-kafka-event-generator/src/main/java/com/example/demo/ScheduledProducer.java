package com.example.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;

@Slf4j
@Service
public class ScheduledProducer {

    private static final int BATCH_SIZE = 5;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Value("${spring.kafka.topic.demo.name}")
    private String topicName;

    private static ThreadLocalRandom rnd = ThreadLocalRandom.current();

    @Scheduled(fixedRate = 1000)
    public void sendMessage() {
        ObjectMapper objectMapper = new ObjectMapper();
        // make a batch of some texts
        rnd.ints(BATCH_SIZE, 0, Integer.MAX_VALUE).forEach(n -> {
            Message msg = Message.builder()
                    .id(n)
                    .text(TextGen.getRandomText())
                    .hashtag("#" + TextGen.getRandomWord())
                    .link(String.format("https://messages.demo/%s/%d", TextGen.getRandomWord(), n))
                    .build();
            try {
                kafkaTemplate.send(topicName, objectMapper.writeValueAsString(msg))
                        .completable()
                        .thenAccept(result -> log.debug(String.valueOf(result.getRecordMetadata().offset())));
            } catch (Exception e) {
                log.error(String.format("can't send %s", msg), e);
            }

        });
    }

}
