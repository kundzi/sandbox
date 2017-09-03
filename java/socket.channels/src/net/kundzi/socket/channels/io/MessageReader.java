package net.kundzi.socket.channels.io;

import net.kundzi.socket.channels.io.lvmessage.LvMessage;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.util.Optional;

public interface MessageReader<T extends Message> {
  Optional<LvMessage> read(ReadableByteChannel readableByteChannel) throws IOException;
}
