package net.kundzi.socket.channels.message.lvmessage;

import net.kundzi.socket.channels.message.MessageWriter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

public class LvMessageWriter implements MessageWriter<LvMessage> {
  @Override
  public void write(final WritableByteChannel writableByteChannel, final LvMessage message) throws IOException {
    final ByteBuffer sizeBuffer = ByteBuffer.allocate(4);
    sizeBuffer.putInt(message.length());
    sizeBuffer.flip();
    while (sizeBuffer.hasRemaining()) {
      writableByteChannel.write(sizeBuffer);
    }
    final ByteBuffer writeBuffer = ByteBuffer.wrap(message.data());
    while (writeBuffer.hasRemaining()) {
      writableByteChannel.write(writeBuffer);
    }
  }
}
