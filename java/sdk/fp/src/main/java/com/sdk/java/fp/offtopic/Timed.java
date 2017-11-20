package com.sdk.java.fp.offtopic;

import java.util.concurrent.TimeUnit;

public class Timed {

  public static long timed(Runnable r) {
    // execute once for warm-up
    r.run();
    final long start = System.nanoTime();
    r.run();
    final long delta = System.nanoTime() - start;
    long ms = TimeUnit.NANOSECONDS.toMillis(delta);
    System.out.println(String.format("took:%dms", ms));
    return ms;
  }


}
