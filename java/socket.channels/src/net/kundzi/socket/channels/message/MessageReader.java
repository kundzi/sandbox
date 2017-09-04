package net.kundzi.socket.channels.message;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;

public interface MessageReader<T extends Message> {
  T read(ReadableByteChannel readableByteChannel) throws IOException;
}
