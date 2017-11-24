package com.sdk.java.fp;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class StreamIntermediateOperations {

  public static void main(String[] args) {
    List<String> values =
        Arrays.asList("The quick brown fox jumps over the lazy dog".split(" "));

    // The longest word
    values.stream()
        .max(Comparator.comparing(String::length));

    // Number of upper-case letter
    values.stream()
        .flatMapToInt(String::chars)
        .filter(Character::isUpperCase)
        .count();

    // Max 3 words
    values.stream()
        .limit(3)
        .collect(Collectors.toList());

    // Not first 3 words
    values.stream()
        .skip(3)
        .collect(Collectors.toList());

    // Lexical sort
    values.stream()
        .sorted(String::compareToIgnoreCase)
        .collect(Collectors.toList());

    // Causing some side effect
    values.stream()
        .peek(System.out::println)
        .collect(Collectors.toList());

    // How many different characters?
    values.stream()
        .flatMapToInt(String::chars)
        .map(Character::toLowerCase)
        .distinct()
        .boxed()
        .count();

    // TODO exercise - number of unique words in text
    return;
  }
}
