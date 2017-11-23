package com.sdk.java.fp;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

public class LambdaWhereDoIGetLambda {

  void example() {
    List<Integer> ints = Arrays.asList(3, 2, 1);

    // Inline lambda
    Collections.sort(ints, (left, right) -> left - right);

    // From a variable
    final Comparator<Integer> comparator = (left, right) -> left - right;
    Collections.sort(ints, comparator);
    // Or as a result of a function call
    Collections.sort(ints, Comparator.naturalOrder());

    // Reference to a method of an arbitrary object
    Collections.sort(ints, Integer::compareTo);

    // Reference to a static method
    Collections.sort(ints, LambdaWhereDoIGetLambda::compareInts);

    // Reference to a constructor
    Supplier<String> stringMaker = String::new;
  }

  public static int compareInts(int left, int right) {
    return left - right;
  }
}
