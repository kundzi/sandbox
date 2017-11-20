package com.sdk.java.fp;

import java.util.Arrays;
import java.util.List;

public class StreamPerformance {

  public static void main(String[] args) {
    System.out.println(functionLazy());
  }

  static int functionLazy() {
    List<Integer> values = Arrays.asList(1, 2, 3, 5, 4, 6, 7, 8, 9, 10);
    return values.stream()
        .filter(StreamPerformance::isGreaterThan3)
        .filter(StreamPerformance::isEven)
        .map(StreamPerformance::doubleIt)
        .findFirst()
        .get();
  }

  static boolean isGreaterThan3(int number) {
    System.out.println("isGreaterThan3 " + number);
    return number > 3;
  }

  static boolean isEven(int number) {
    System.out.println("isEven " + number);
    return number % 2 == 0;
  }

  static int doubleIt(int number) {
    System.out.println("doubleIt " + number);
    return 2 * number;
  }

}
