package net.kundzi.socket.channels.io.lvmessage;

import net.kundzi.socket.channels.io.MessageReader;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

public class LvMessageReader implements MessageReader<LvMessage> {

  @Override
  public LvMessage read(final ReadableByteChannel readableByteChannel) throws IOException {
    final ByteBuffer sizeBuffer = ByteBuffer.allocate(4);
    int leftToReadSize = 4;

    while (leftToReadSize > 0) {
      int countSizeRead = readableByteChannel.read(sizeBuffer);
      if (countSizeRead == -1) {
        throw new IOException("read return -1");
      } else if (countSizeRead == 0) {
        continue;
      }
      leftToReadSize -= countSizeRead;
    }

    final int size = sizeBuffer.getInt(0);
    final ByteBuffer messageBuffer = ByteBuffer.allocate(size);
    int leftToReadMessage = size;
    while (leftToReadMessage > 0) {
      final int read = readableByteChannel.read(messageBuffer);
      if (read == -1) {
        throw new IOException("read return -1");
      } else if (read == 0) {
        continue;
      }
      leftToReadMessage -= read;
    }
    return new DefaultLvMessage(messageBuffer.array());
  }
}
