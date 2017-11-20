package com.sdk.java.fp;

import java.util.Arrays;
import java.util.List;
import java.util.function.IntPredicate;
import java.util.function.Predicate;

public class LambdaAndFunctionalInterfaceVsAnonymous {
  // TODO why did I call it this way?

  public static void main(String[] args) {
    final List<Integer> integers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    System.out.println(sumOdlStyle(integers, new EvenSelector()));
    System.out.println(sumPredicate(integers, integer -> integer % 2 == 0));
    System.out.println(sumMapToInt(integers, integer -> integer % 2 == 0));
    System.out.println(sumIntPredicate(integers, integer -> integer % 2 == 0));
  }

  // old style
  interface Selector {
    boolean pick(int value);
  }

  static class EvenSelector implements Selector {

    @Override
    public boolean pick(final int value) {
      return value % 2 == 0; // the only meaningful line among many
    }
  }

  static int sumOdlStyle(List<Integer> values, Selector selector) {
    int result = 0;
    for (Integer i : values) {
      if (selector.pick(i)) {
        result += i;
      }
    }
    return result;
  }

  //or
  static int sumPredicate(List<Integer> numbers, Predicate<Integer> selector) {
    return numbers.stream()
        .filter(selector)
        .reduce(0, Math::addExact);
  }

  // or
  static int sumMapToInt(List<Integer> numbers, Predicate<Integer> selector) {
    return numbers.stream()
        .filter(selector)
        .mapToInt(Integer::intValue)
        .sum();
  }

  // or
  static int sumIntPredicate(List<Integer> numbers, IntPredicate selector) {
    return numbers.stream()
        .mapToInt(Integer::intValue)
        .filter(selector)
        .sum();
  }

}
