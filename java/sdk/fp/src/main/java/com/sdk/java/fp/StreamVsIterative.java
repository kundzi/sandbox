package com.sdk.java.fp;

import java.util.Arrays;
import java.util.List;

public class StreamVsIterative {

  public static void main(String[] args) {
    System.out.println(functionIterative());
    System.out.println(functionStream());
  }

  static int functionIterative() {
    List<Integer> values = Arrays.asList(1, 2, 3, 5, 4, 6, 7, 8);
    int result = 0;
    for (int e : values) {
      if (e > 3 && e % 2 == 0) {
        result = e * 2;
        break;
      }
    }
    return result;
  }

  static int functionStream() {
    List<Integer> values = Arrays.asList(1, 2, 3, 5, 4, 6, 7, 8);
    return values.stream()
        .filter(e -> e > 3)
        .filter(e -> e % 2 == 0)
        .map(e -> e * 2)
        .findFirst()
        .get();
  }

}
