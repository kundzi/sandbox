package com.sdk.java.fp;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class LambdaFunctionalInterfaces {

  void example() {
    final Runnable voidVoid = () -> {};
    final Supplier<String> voidAny = () -> "hello";

    final Predicate<Integer> anyBoolean = number -> false;
    final BiPredicate<String, Integer> anyAnyBoolean = (str, number) -> str.length() == number;

    final Consumer<String> anyVoid = str -> {};
    final BiConsumer<String, Integer> anyAnyVoid  = (str, number) -> {};

    final Function<String, Integer> xToY = str -> 1;
    final UnaryOperator<Integer> xToX = number -> ++number;
    final BiFunction<String, Object, Integer> xyToZ = (str, obj) -> str.length() + obj.hashCode();
    final BinaryOperator<Integer> xxToX = (number1, number2) -> number1 + number2;

    // bunch of primitive functions
    // IntPredicate.class
    // DoubleFunction.class
    // LongSupplier.class
    // etc
    // more https://docs.oracle.com/javase/8/docs/api/java/util/function/package-summary.html
  }


}
