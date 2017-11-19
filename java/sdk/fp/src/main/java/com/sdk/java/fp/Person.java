package com.sdk.java.fp;

public class Person {

  public final String name;
  public final long age;

  public Person(final String name, final long age) {
    this.name = name;
    this.age = age;
  }

  public String getName() {
    return name;
  }

  public long getAge() {
    return age;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("{");
    sb.append("name='").append(name).append('\'');
    sb.append(", age=").append(age);
    sb.append('}');
    return sb.toString();
  }
}
