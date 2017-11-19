package com.sdk.java.fp;

public class SugarRunnable {

  // javap -c -p SugarRunnable SugarRunnable\$1
  public static void main(String[] args) {
    Thread t1 = new Thread(new Runnable() {
      @Override
      public void run() {
        System.out.println("Runnable");
      }
    });
    t1 = new Thread(new Runnable() {
      @Override
      public void run() {
        System.out.println("Runnable");
      }
    });
    t1.start();
  }

}
