package com.sdk.java.fp;

import java.util.Arrays;
import java.util.List;

public class StreamVsIterative {

  public static void main(String[] args) {
    List<Integer> values = Arrays.asList(1, 2, 3, 5, 4, 6, 7, 8);
    System.out.println(findFirstEvenNumberGraterThan3AndMultipleBy2(values));
    System.out.println(findFirstEvenNumberGraterThan3AndMultipleBy2WithStream(values));
  }

  static int findFirstEvenNumberGraterThan3AndMultipleBy2(final List<Integer> values) {
    int result = 0;
    for (int e : values) {
      if (e > 3 && e % 2 == 0) {
        result = e * 2;
        break;
      }
    }
    return result;
  }

  static int findFirstEvenNumberGraterThan3AndMultipleBy2WithStream(final List<Integer> values) {
    return values.stream()
        .filter(e -> e > 3)
        .filter(e -> e % 2 == 0)
        .map(e -> 2 * e)
        .findFirst()
        .get();
  }





//  static int functionStream() {
//    // TODO examples of findAny and findFirst
//    List<Integer> values = Arrays.asList(1, 2, 3, 5, 4, 6, 7, 8);
//    return values.stream()
//        .parallel()
//        .sorted()
////        .parallel()
//        .filter(e -> e > 3)
//        .filter(e -> e % 2 == 0)
////        .map(e -> e * 2)
//        .findAny()
////        .findFirst()
//        .get();
//  }

}
