package com.sdk.java.fp;

public class LambdaAnonymousClasses {

  public static void main(String[] args) {

    Thread thread = new Thread(new Runnable() {
      @Override
      public void run() {
        System.out.println("Much code");
      }
    });

    thread.start();
  }

}
