package com.sdk.java.fp;

import java.util.Arrays;
import java.util.List;
import java.util.function.IntPredicate;
import java.util.function.Predicate;

public class LambdaFunctionalDesign {

  public static void main(String[] args) {
    final List<Integer> integers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

    System.out.println(sumOldStyle(integers, new EvenSelector()));

    System.out.println(sumJava8(integers, integer -> integer % 2 == 0));

    System.out.println(sumJava8Primitive(integers, integer -> integer % 2 == 0));
  }


  // before Java 8
  interface Selector {
    boolean pick(int value);
  }

  static class EvenSelector implements Selector {
    @Override
    public boolean pick(final int value) {
      return value % 2 == 0; // the only meaningful line among many
    }
  }

  static int sumOldStyle(List<Integer> values, Selector selector) {
    int result = 0;
    for (final Integer value : values) {
      if (selector.pick(value)) {
        result += value;
      }
    }
    return result;
  }
  // !before Java 8


  // Java 8
  static int sumJava8(List<Integer> values, Predicate<Integer> predicate) {
    int result = 0;
    for (final Integer value : values) {
      if (predicate.test(value)) {
        result += value;
      }
    }
    return result;
  }
  // !Java 8


  // Java 8 with primitive functions
  static int sumJava8Primitive(List<Integer> values, IntPredicate predicate) {
    int result = 0;
    for (final int value : values) {
      if (predicate.test(value)) {
        result += value;
      }
    }
    return result;
  }
  // !Java 8 Java 8 with primitive functions

}
