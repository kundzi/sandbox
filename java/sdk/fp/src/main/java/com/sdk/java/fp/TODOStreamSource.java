package com.sdk.java.fp;

import java.util.Arrays;
import java.util.stream.Stream;

public class TODOStreamSource {

  public static void main(String[] args) {
    // TODO
    final Stream<Integer> stream = Arrays.asList(1, 2).stream();
    stream.forEach(integer -> {});
    stream.forEach(integer -> {});
  }
}
