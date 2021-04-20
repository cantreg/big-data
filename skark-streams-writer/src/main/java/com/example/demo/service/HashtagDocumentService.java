package com.example.demo.service;

import com.example.demo.mongo.domain.HashtagRating;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.spark.MongoSpark;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.bson.Document;

import java.time.LocalDateTime;

@Slf4j
public class HashtagDocumentService {

    private static ObjectMapper mapper = new ObjectMapper();

    public static void save(JavaPairRDD<String, Long> rdd) {
        JavaRDD<Document> documents = convert(rdd);
        log.info("save documents {}", documents);
        MongoSpark.save(documents);
    }

    private static JavaRDD<Document> convert(JavaPairRDD<String, Long> rdd) {
        return rdd.map(pair -> {
            String hashtag = pair._1;
            Long count = pair._2;
            Document document = convert(hashtag, count);
            return document;
        });
    }

    @SneakyThrows
    private static Document convert(String hashtag, Long count) {
        HashtagRating rating = HashtagRating.builder()
                .hashtag(hashtag)
                .rating(count)
                .time(LocalDateTime.now())
                .build();
        Document document = Document.parse(mapper.writeValueAsString(rating));
        return document;
    }
}
