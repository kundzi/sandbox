package net.kundzi.socket.channels;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import static java.lang.System.out;
import static net.kundzi.socket.channels.RandomString.randomString;

public class Main {

  static SecureRandom rnd = new SecureRandom();

  public static void main(String[] args) throws Exception {
    //blockingServer();
    nonblockingServer();
  }

  private static void nonblockingServer() throws IOException {
    final String host = "localhost";
    final int port = 6677;
    final InetSocketAddress serverSockAddress = new InetSocketAddress(host, port);
    final CopyOnWriteArraySet<SocketChannel> clientChannels = new CopyOnWriteArraySet<>();

    final ServerSocketChannel boundServerChannel = ServerSocketChannel.open().bind(serverSockAddress);
    boundServerChannel.configureBlocking(false);
    final Selector selector = Selector.open();
    boundServerChannel.register(selector, SelectionKey.OP_ACCEPT, "idServer0");

    createClientThread(boundServerChannel.getLocalAddress(), 1, 100);
    createClientThread(boundServerChannel.getLocalAddress(), 1000, 10000);

    final LvMessageReader messageReader = new LvMessageReader();
    final LvMessageWriter messageWriter = new LvMessageWriter();

    int numMessages = 0;
    final int maxMessages = 10;
    while (true) {
      final int numReady = selector.select();
      out.println("numReady " + numReady);
      if (numReady == 0) {
        continue;
      }

      final Set<SelectionKey> selectionKeys = selector.selectedKeys();
      final Iterator<SelectionKey> keyIterator = selectionKeys.iterator();
      while (keyIterator.hasNext()) {
        final SelectionKey key = keyIterator.next();
        out.println("processing " + key.attachment());

        if (key.isAcceptable()) {
          out.println("isAcceptable");
          final ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
          final SocketChannel clientChannel = serverSocketChannel.accept();
          clientChannel.configureBlocking(false);
          clientChannels.add(clientChannel);
          clientChannel.register(selector, SelectionKey.OP_READ, "idClient" + clientChannels.size());
        }

        if (key.isConnectable()) {
          out.println("isConnectable");
        }

        if (key.isReadable()) {
          out.println("isReadable");
          final LvMessage message = messageReader.read((SocketChannel) key.channel());
          out.println("#" + numMessages +
                          " " + message.length() +
                          " " + new String(message.data()));
          if (++numMessages >= maxMessages) {
            return;
          }
        }

        if (key.isWritable()) {
          out.println("isWritable");
        }

        keyIterator.remove();
      }
    }
  }


  private static void createClientThread(SocketAddress socketAddress, int from, int to) throws IOException {
    final SocketChannel client = SocketChannel.open(socketAddress);
    final Thread clientThread = new Thread(() -> {
      try {
        final LvMessageWriter messageWriter = new LvMessageWriter();
        for (int size = from; size <= to; size *= 2) {
          final byte[] randomString = (randomString(size, rnd)).getBytes();
          messageWriter.write(client, new DefaultLvMessage(randomString));
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    });
    clientThread.start();
  }

  public interface Message {
    byte[] data();
  }

  public interface MessageReader<T extends Message> {
    T read(ReadableByteChannel readableByteChannel) throws IOException;
  }

  public static class LvMessageReader implements MessageReader<LvMessage> {

    @Override
    public LvMessage read(final ReadableByteChannel readableByteChannel) throws IOException {
      final ByteBuffer sizeBuffer = ByteBuffer.allocate(4);
      int leftToReadSize = 4;

      while (leftToReadSize > 0) {
        int countSizeRead = readableByteChannel.read(sizeBuffer);
        if (countSizeRead == -1) {
          break;
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
          break;
        } else if (read == 0) {
          continue;
        }
        leftToReadMessage -= read;
      }
      return new DefaultLvMessage(messageBuffer.array());
    }
  }

  public interface MessageWriter<T extends Message> {
    void write(WritableByteChannel writableByteChannel, T message) throws IOException;
  }

  public static class LvMessageWriter implements MessageWriter<LvMessage> {
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

  /**
   * LengthValue Message
   * simply a message with length
   */
  public interface LvMessage extends Message {
    int length();
  }

  public static class DefaultLvMessage implements LvMessage {
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
}
