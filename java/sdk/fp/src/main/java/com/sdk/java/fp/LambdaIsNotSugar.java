package com.sdk.java.fp;

public class LambdaIsNotSugar {

  // javap -c -p SugarLambda
  public static void main(String[] args) {
    Thread t2 = new Thread(() -> System.out.println("Lambda"));
    t2 = new Thread(() -> System.out.println("Lambda"));
    t2.start();
  }

}
