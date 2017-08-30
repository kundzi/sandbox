package net.kundzi.socket.channels;

import java.util.Random;

public class RandomString {

  private RandomString() {}

  private static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

  public static String randomString(int len, Random random) {
    StringBuilder sb = new StringBuilder(len);
    for (int i = 0; i < len; i++)
      sb.append(AB.charAt(random.nextInt(AB.length())));
    return sb.toString();
  }
}
