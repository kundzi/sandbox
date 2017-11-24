package com.sdk.java.fp;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class StreamTerminalOperations {

  public static void main(String[] args) {
    List<String> values =
        Arrays.asList("The quick brown fox jumps over the lazy dog".split(" "));

    // None of the words starts with 'z'
    values.stream()
        .noneMatch(word -> word.startsWith("z"));

    // Something starts with 'bro'
    values.stream()
        .anyMatch(word -> word.startsWith("bro"));

    // Join words on '-'
    values.stream()
        .filter(word -> word.length() > 4)
        .collect(Collectors.joining("-"));

    // Map length to strings
    values.stream()
        .collect(Collectors.toMap(
            String::length,
            Function.identity(),
            (oldVal, newVal) -> newVal
        ));

    // Take only first letters of if the words
    values.stream()
        .reduce("", (carry, x) -> carry += x.substring(0, 1));

    // What is summary statistics?
    IntStream.range(0, 1_000_000)
        .summaryStatistics();

    // TODO exercise - number of unique words in text
    return;
  }

}
