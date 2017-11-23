package com.sdk.java.fp;

public class LambdaIsNotSugar {

  // javap -c -p LambdaIsNotSugar
  // TODO add 'final' change
  public static void main(String[] args) {
    Thread t2 = new Thread(() -> System.out.println("Less code"));
    t2 = new Thread(() -> System.out.println("Much less code"));
    t2.start();
  }

}
