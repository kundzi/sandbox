package net.kundzi.socket.channels;

import net.kundzi.socket.channels.io.lvmessage.DefaultLvMessage;
import net.kundzi.socket.channels.io.lvmessage.LvMessage;
import net.kundzi.socket.channels.io.lvmessage.LvMessageReader;
import net.kundzi.socket.channels.io.lvmessage.LvMessageWriter;
import net.kundzi.socket.channels.server.SimpleReactorServer;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.System.out;

public class Main {

  public static void main(String[] args) throws Exception {
    nonblockingServer();
  }

  private static void nonblockingServer() throws IOException, InterruptedException {
    final String host = "localhost";
    final int port = 6677;
    final InetSocketAddress serverSockAddress = new InetSocketAddress(host, port);

    final SimpleReactorServer simpleReactorServer = new SimpleReactorServer(serverSockAddress, (message, from) -> {
      try {
        final String inMessage = new String(message.data());
        out.println(from.getRemoteAddress() + " " + message.length() + " " + inMessage);
        final String outMessage = inMessage + " pong";
        out.println("responding to " + from.getRemoteAddress());
        from.send(new DefaultLvMessage(outMessage.getBytes()));
      } catch (IOException e) {
      }
    });
    simpleReactorServer.start();

    final ArrayList<Closeable> closeables = new ArrayList<>();
    closeables.add(startClient(serverSockAddress));
    closeables.add(startClient(serverSockAddress));
    closeables.add(startClient(serverSockAddress));
    closeables.add(startClient(serverSockAddress));
    closeables.add(startClient(serverSockAddress));
    closeables.add(startClient(serverSockAddress));
    closeables.add(startClient(serverSockAddress));
    closeables.add(startClient(serverSockAddress));
    closeables.add(startClient(serverSockAddress));
    closeables.add(startClient(serverSockAddress));

    Thread.sleep(10000);
    out.println("stopping ...");
    for (Closeable closeable : closeables) {
      closeable.close();
    }
    simpleReactorServer.stop();
    simpleReactorServer.join();
    out.println("stopped");
  }

  private static NonBlockingClient startClient(final InetSocketAddress serverSockAddress) throws IOException {
    final NonBlockingClient nonBlockingClient = NonBlockingClient.open(serverSockAddress);
    nonBlockingClient.setIncomingMessageListener((client, message) -> {
      final String respondWith = new String(message.data()) + " ping";
      try {
        Thread.sleep(300);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      client.send(new DefaultLvMessage(respondWith.getBytes()));
    });
    nonBlockingClient.send(new DefaultLvMessage("ping".getBytes()));
    return nonBlockingClient;
  }

  static class NonBlockingClient implements Closeable {

    interface IncomingMessageListener {
      void onMessage(NonBlockingClient client, LvMessage message);
    }

    public static NonBlockingClient open(SocketAddress serverAddress) throws IOException {
      final SocketChannel socketChannel = SocketChannel.open(serverAddress);
      socketChannel.configureBlocking(false);
      final Selector selector = Selector.open();
      socketChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
      return new NonBlockingClient(new LvMessageReader(),
                                   new LvMessageWriter(),
                                   Executors.newSingleThreadExecutor(),
                                   Executors.newSingleThreadExecutor(),
                                   selector,
                                   socketChannel);
    }

    final LvMessageReader messageReader;
    final LvMessageWriter messageWriter;
    final ExecutorService selectExecutor;
    final ExecutorService deliveryExecutor;
    final Selector selector;
    final SocketChannel socketChannel;
    final ConcurrentLinkedDeque<LvMessage> outgoingMessages = new ConcurrentLinkedDeque<>();
    final AtomicBoolean isActive = new AtomicBoolean(true);
    final AtomicReference<IncomingMessageListener> incomingMessageListener = new AtomicReference<>();

    NonBlockingClient(final LvMessageReader messageReader, final LvMessageWriter messageWriter, final ExecutorService selectExecutor,
                      final ExecutorService deliveryExecutor,
                      final Selector selector,
                      final SocketChannel socketChannel) {
      this.messageReader = messageReader;
      this.messageWriter = messageWriter;
      this.selectExecutor = selectExecutor;
      this.deliveryExecutor = deliveryExecutor;
      this.selector = selector;
      this.socketChannel = socketChannel;
      selectExecutor.execute(this::loop);
    }

    public void send(LvMessage message) {
      outgoingMessages.add(message);
    }

    public void setIncomingMessageListener(IncomingMessageListener incomingMessageListener) {
      this.incomingMessageListener.set(incomingMessageListener);
    }

    void loop() {
      while (isActive.get()) {
        try {
          final int numSelected = selector.select();
          if (0 == numSelected) continue;

          final Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
          while (isActive.get() && iterator.hasNext()) {
            final SelectionKey key = iterator.next();
            final ArrayList<LvMessage> newMessages = new ArrayList<>(numSelected);
            try {
              if (key.isReadable()) {
                final Optional<LvMessage> newMessage = onReading();
                newMessage.ifPresent(newMessages::add);
              }
              if (key.isWritable()) {
                onWriting();
              }
            } finally {
              iterator.remove();
            }
            deliverMessages(newMessages);
          }

        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }

    private void deliverMessages(final List<LvMessage> newMessages) {
      final IncomingMessageListener messageListener = this.incomingMessageListener.get();
      if (newMessages.isEmpty() || null == messageListener) {
        return;
      }
      deliveryExecutor.execute(() -> newMessages.forEach(message -> {
        try {
          messageListener.onMessage(this, message);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }));
    }

    private void onWriting() {
      while (!outgoingMessages.isEmpty()) {
        final LvMessage message = outgoingMessages.poll();
        try {
          messageWriter.write(socketChannel, message);
        } catch (IOException e) {
          e.printStackTrace();
          return;
        }
      }
    }

    private Optional<LvMessage> onReading() {
      try {
        final LvMessage newMessage = messageReader.read(socketChannel);
        return Optional.of(newMessage);
      } catch (IOException e) {
        e.printStackTrace();
        return Optional.empty();
      }
    }

    @Override
    public void close() throws IOException {
      isActive.set(false);
      try {
        selectExecutor.shutdown();
        selectExecutor.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      try {
        deliveryExecutor.shutdown();
        deliveryExecutor.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      selector.close();
      socketChannel.close();
    }

  }
}
