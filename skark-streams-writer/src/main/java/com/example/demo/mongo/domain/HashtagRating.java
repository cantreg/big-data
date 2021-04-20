package com.example.demo.mongo.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY;

@ToString
@EqualsAndHashCode(of = "id")
@Builder
@Getter
public class HashtagRating {

    @JsonProperty(value = "_id", access = WRITE_ONLY)
    private String id;
    private String hashtag;
    private Long rating;
    private LocalDateTime time;
}
