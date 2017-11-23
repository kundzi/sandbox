package com.sdk.java.fp;

import java.util.function.DoubleSupplier;
import java.util.function.Function;
import java.util.function.IntBinaryOperator;
import java.util.function.IntFunction;

public class LambdaSyntax {

  void examples() {

    final DoubleSupplier doubleSupplier = () -> 42;

    final IntFunction intFunction = x -> x + 42;

    final IntBinaryOperator intBinaryOperator = (x, y) -> x + y + 42;

    Function<Integer, Function<Integer, Integer>> badass = x -> y -> x + y;

    final IntFunction ugly =
        param -> {
          param *= 2;
          return param + 42;
        };

  }
}
