package net.kundzi.socket.channels.client;

import net.kundzi.socket.channels.io.lvmessage.LvMessage;
import net.kundzi.socket.channels.io.lvmessage.LvMessageReader;
import net.kundzi.socket.channels.io.lvmessage.LvMessageWriter;

import java.io.Closeable;
import java.io.IOException;
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

public class NonBlockingClient implements Closeable {

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
