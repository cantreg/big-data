package com.example.demo.dto;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;
import org.apache.avro.specific.SpecificRecord;
import org.apache.commons.lang3.NotImplementedException;

import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(of = "id")
@Getter
@Setter
@Slf4j
public class Message implements SpecificRecord, Serializable {

    private Long id;
    private String link;
    private String text;
    private String hashtag;

    private static final String MESSAGE_AVSC_PATH = "/message.avsc";
    public static final Schema SCHEMA$;

    private static Map<String, BiConsumer<Message, Object>> setterMap;
    private static Map<String, Function<Message, Object>> getterMap;

    static {
        try {
            InputStream systemResourceAsStream = Message.class.getResourceAsStream(MESSAGE_AVSC_PATH);
            SCHEMA$ = new Schema.Parser().parse(systemResourceAsStream);
            initMaps();
        } catch (Exception e) {
            log.error("can't get Message avro schema", e);
            throw new RuntimeException(e);
        }
    }

    private static void initMaps() {
        setterMap = new HashMap<>();
        setterMap.put("id", (m, o) -> m.setId((Long) o));
        setterMap.put("link", (m, o) -> m.setLink((String) o));
        setterMap.put("text", (m, o) -> m.setText((String) o));
        setterMap.put("hashtag", (m, o) -> m.setHashtag((String) o));
        getterMap = new HashMap<>();
        getterMap.put("id", (m) -> m.getId());
        getterMap.put("link", (m) -> m.getLink());
        getterMap.put("text", (m) -> m.getText());
        getterMap.put("hashtag", (m) -> m.getHashtag());
    }

    public Message(String hashtag) {
        this.hashtag = hashtag;
    }

    @Override
    public void put(int i, Object v) {
        BiConsumer<Message, Object> s = setterMap.get(getSchema().getFields().get(i).name());
        if(s != null) {
            s.accept(this, v);
        } else {
            getSchema().getFields().get(i).aliases().stream().map(setterMap::get)
                    .findFirst().ifPresent(c -> c.accept(this, v));
        }
    }

    @Override
    public Object get(int i) {
        Function<Message, Object> g = getterMap.get(getSchema().getFields().get(i).name());
        if(g != null) {
            return g.apply(this);
        } else {
            return getSchema().getFields().get(i).aliases().stream().map(getterMap::get)
                    .findFirst().map(f -> f.apply(this)).orElse(null);
        }
    }

    @Override
    public Schema getSchema() {
        return SCHEMA$;
    }
}