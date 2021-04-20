package com.example.demo;

import java.util.Collections;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TextGen {

    private static ThreadLocalRandom rnd = ThreadLocalRandom.current();

    // 10-20 words
    public static String getRandomText() {
        String text = Collections.nCopies(rnd.nextInt(10, 21),
                (Supplier<String>) () -> getRandomWord())
                .stream().map(Supplier::get)
                .collect(Collectors.joining(" "));
        return text;
    }

    public static String getRandomWord() {
        String word = getRndCharCodeStream().collect(
                StringBuilder::new,
                StringBuilder::appendCodePoint,
                StringBuilder::append).toString();
        return word;
    }

    // 2-9 lowercase chars
    static IntStream getRndCharCodeStream() {
        return rnd.ints(rnd.nextInt(2, 10), 97, 123);
    }
}