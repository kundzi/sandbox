package net.kundzi.socket.channels.message.lvmessage;

import java.util.Arrays;

public class DefaultLvMessage implements LvMessage {
  private final byte[] data;

  public DefaultLvMessage(final byte[] data) {
    // Copy for safety, might be wasteful
    this.data = Arrays.copyOf(data, data.length);
  }

  @Override
  public byte[] data() {
    return data;
  }

  @Override
  public int length() {
    return data.length;
  }
}
