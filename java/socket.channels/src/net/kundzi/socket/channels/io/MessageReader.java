package net.kundzi.socket.channels.io;

import net.kundzi.socket.channels.io.lvmessage.LvMessage;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;

public interface MessageReader<T extends Message> {
  LvMessage read(ReadableByteChannel readableByteChannel) throws IOException;
}
