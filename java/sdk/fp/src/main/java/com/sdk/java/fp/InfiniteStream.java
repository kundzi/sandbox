package com.sdk.java.fp;

import java.math.BigInteger;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class InfiniteStream {

  public static void main(String[] args) {
    Stream.iterate(1, e -> e + 1)
        .parallel()
        .map(BigInteger::valueOf)
        .filter(bi -> bi.isProbablePrime(10))
        .peek(System.out::println)
        .collect(toList());
  }

}
