package net.kundzi.socket.channels;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.security.SecureRandom;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import static java.lang.System.out;

public class Main {

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
          clientChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, "idClient" + clientChannels.size());
        }

        if (key.isConnectable()) {
          out.println("isConnectable");
        }

        if (key.isReadable()) {
          out.println("isReadable");
          final String message = readMessage((SocketChannel) key.channel());
          numMessages++;
          out.println(key.isWritable() + "#" + numMessages + " " + message);
          if (numMessages >= maxMessages) {
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

  private static void blockingServer() throws IOException, InterruptedException {
    final String host = "localhost";
    final int port = 6677;

    final ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
    final ServerSocketChannel boundServerChannel = serverSocketChannel.bind(new InetSocketAddress(host, port));
    boundServerChannel.configureBlocking(false);

    final CopyOnWriteArraySet<SocketChannel> clientChannels = new CopyOnWriteArraySet<>();
    final Thread serverThread = new Thread(() -> {
      try {
        while (true) {
          {
            final SocketChannel clientChannel = boundServerChannel.accept();
            if (clientChannel != null) {
              clientChannel.configureBlocking(false);
              clientChannels.add((clientChannel));
            }
          }

          for (final SocketChannel clientChannel : clientChannels) {
            final ByteBuffer sizeBuffer = ByteBuffer.allocate(4);
            sizeBuffer.clear();

            int leftToReadSize = 4;
            int countSizeRead = 0;
            while (leftToReadSize > 0) {
              countSizeRead = clientChannel.read(sizeBuffer);
              if (countSizeRead == -1) {
                break;
              } else if (countSizeRead == 0) {
                continue;
              }
              leftToReadSize -= countSizeRead;
              out.println("left to read size: " + leftToReadSize);
            }
            final int size = sizeBuffer.getInt(0);
            out.println("size: " + size);

            final ByteBuffer messageBuffer = ByteBuffer.allocate(size);
            int leftToReadMessage = size;
            if (leftToReadMessage > 0) {
              final int read = clientChannel.read(messageBuffer);
              if (read == -1) {
                break;
              } else if (read == 0) {
                continue;
              }
              leftToReadMessage -= read;
              out.println("left to read message: " + leftToReadMessage);
            }
            final String message = new String(messageBuffer.array()).trim();
            out.println(String.format("Msg{%d}:{%s}", message.length(), message));
          }
        }

      } catch (IOException e) {
        e.printStackTrace();
      }
    });

    createClientThread(boundServerChannel.getLocalAddress(), 1, 1000);
    createClientThread(boundServerChannel.getLocalAddress(), 2000, 10000);
    serverThread.start();
    serverThread.join();
  }

  private static void createClientThread(SocketAddress socketAddress, int from, int to) throws IOException {
    final SocketChannel client = SocketChannel.open(socketAddress);
    final Thread clientThread = new Thread(() -> {
      try {
        for (int size = from; size <= to; size *= 2) {
          final byte[] randomString = (size + "<>" + randomString(size)).getBytes();
          final ByteBuffer sizeBuffer = ByteBuffer.allocate(4);
          out.println("Sending size: " + randomString.length);
          sizeBuffer.putInt(randomString.length);
          sizeBuffer.flip();
          while (sizeBuffer.hasRemaining()) {
            client.write(sizeBuffer);
          }
          final ByteBuffer writeBuffer = ByteBuffer.wrap(randomString);
          while (writeBuffer.hasRemaining()) {
            client.write(writeBuffer);
          }
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    });
    clientThread.start();
  }

  static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
  static SecureRandom rnd = new SecureRandom();

  static String randomString(int len) {
    StringBuilder sb = new StringBuilder(len);
    for (int i = 0; i < len; i++)
      sb.append(AB.charAt(rnd.nextInt(AB.length())));
    return sb.toString();
  }

  static String readMessage(SocketChannel socketChannel) throws IOException {
    final ByteBuffer sizeBuffer = ByteBuffer.allocate(4);
    sizeBuffer.clear();

    int leftToReadSize = 4;
    int countSizeRead = 0;
    while (leftToReadSize > 0) {
      countSizeRead = socketChannel.read(sizeBuffer);
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
      final int read = socketChannel.read(messageBuffer);
      if (read == -1) {
        break;
      } else if (read == 0) {
        continue;
      }
      leftToReadMessage -= read;
    }
    final String message = new String(messageBuffer.array()).trim();
    return message;
  }
}
