package net.kundzi.socket.channels.message.lvmessage;

import net.kundzi.socket.channels.message.Message;

/**
 * Length Value Message
 * A message with length
 */
public interface LvMessage extends Message {
  int length();
}
