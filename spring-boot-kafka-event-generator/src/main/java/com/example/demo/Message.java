package com.example.demo;

import lombok.*;
import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(of = "id")
@Getter
public class Message {

    private long id;
    private String link;
    private String text;
    private String hashtag;

}