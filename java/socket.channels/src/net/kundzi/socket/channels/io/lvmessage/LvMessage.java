package net.kundzi.socket.channels.io.lvmessage;

import net.kundzi.socket.channels.io.Message;

/**
 * Length Value Message
 * A message with length
 */
public interface LvMessage extends Message {
  int length();
}
