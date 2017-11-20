package com.sdk.java.fp;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class StreamUnbounded {

  public static void main(String[] args) {
    final long from = 1_000_000;
    final long n = 1_000;
    System.out.println(primesIterative(from, n));
    System.out.println(primesStream(from, n));
  }

  static List<Long> primesIterative(long from, long n) {
    long current = from;
    int count = 0;
    List<Long> primes = new ArrayList<>();

    while (count < n) {
      if (BigInteger.valueOf(current).isProbablePrime(100)) {
        primes.add(current);
        count++;
      }
      current++;
    }
    return primes;
  }

  static List<Long> primesStream(long from, long n) {
    return Stream.iterate(BigInteger.valueOf(from), e -> e.add(BigInteger.ONE))
        .filter(bi -> bi.isProbablePrime(100))
        .limit(n)
        .map(BigInteger::longValue)
        .collect(toList());
  } // try add parallel as well
}
