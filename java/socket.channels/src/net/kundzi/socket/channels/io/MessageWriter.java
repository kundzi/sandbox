package net.kundzi.socket.channels.io;

import java.io.IOException;
import java.nio.channels.WritableByteChannel;

public interface MessageWriter<T extends Message> {
  void write(WritableByteChannel writableByteChannel, T message) throws IOException;
}
