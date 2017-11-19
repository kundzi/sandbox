package com.sdk.java.fp;


import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.*;

public class MapGroupBy {

  static List<Person> makeSomePeople() {
    return Arrays.asList(
        new Person("Jack", 21),
        new Person("Bo", 21),
        new Person("Bo", 21),
        new Person("Dima", 27),
        new Person("Dima", 72),
        new Person("Jack", 50),
        new Person("John", 42)
    );
  }

  public static void main(String[] args) {
    System.out.println(
        makeSomePeople().stream()
            .collect(groupingBy(Person::getName, mapping(Person::getAge, toList()))));

    System.out.println(
        makeSomePeople().stream()
            .collect(groupingBy(Person::getAge)));
  }

}
