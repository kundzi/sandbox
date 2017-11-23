package com.sdk.java.fp;

public class LambdaRunnable {

  // javap -c -p javap -c -p LambdaRunnable LambdaRunnable\$1 LambdaRunnable\$2
  public static void main(String[] args) {
    Thread t1 = new Thread(new Runnable() {
      @Override
      public void run() {
        System.out.println("Much code");
      }
    });
    t1 = new Thread(new Runnable() {
      @Override
      public void run() {
        System.out.println("Even more code ");
      }
    });
    t1.start();
  }

}
