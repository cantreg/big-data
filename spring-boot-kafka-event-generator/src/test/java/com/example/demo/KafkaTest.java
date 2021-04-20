package com.example.demo;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.awaitility.Awaitility.await;

@SpringBootTest
@DirtiesContext
@EmbeddedKafka(partitions = 1, brokerProperties = {
        "listeners=PLAINTEXT://localhost:9094", "port=9094"
})
@TestConfiguration
@TestPropertySource(
        properties = {
                "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
                "spring.kafka.consumer.auto-offset-reset=earliest"
        })
@EnableKafka
public class KafkaTest {

    private List<String> messages = Lists.newArrayList();

    @KafkaListener(topics = "demo-topic", groupId = "test-messages")
    private void getSomeMessages(String message) {
        messages.add(message);
    }

    @Test
    public void testKafkaScheduled() {
        await().until(() ->
                !messages.isEmpty());
    }

}