package net.kundzi.socket.channels.client;

import net.kundzi.socket.channels.message.Message;
import net.kundzi.socket.channels.message.MessageReader;
import net.kundzi.socket.channels.message.MessageWriter;

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

public class NonBlockingClient<M extends Message> implements Closeable {

  @FunctionalInterface
  public interface IncomingMessageHandler<M extends Message> {
    void handle(NonBlockingClient client, M message);
  }

  public static <M extends Message> NonBlockingClient<M> open(SocketAddress serverAddress,
                                                              MessageReader<M> messageReader,
                                                              MessageWriter<M> messageWriter) throws IOException {
    final SocketChannel socketChannel = SocketChannel.open(serverAddress);
    socketChannel.configureBlocking(false);
    final Selector selector = Selector.open();
    socketChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
    return new NonBlockingClient<>(messageReader,
                                   messageWriter,
                                   Executors.newSingleThreadExecutor(),
                                   Executors.newSingleThreadExecutor(),
                                   selector,
                                   socketChannel);
  }

  final MessageReader<M> messageReader;
  final MessageWriter<M> messageWriter;
  final ExecutorService selectExecutor;
  final ExecutorService deliveryExecutor;
  final Selector selector;
  final SocketChannel socketChannel;
  final ConcurrentLinkedDeque<M> outgoingMessages = new ConcurrentLinkedDeque<>();
  final AtomicBoolean isActive = new AtomicBoolean(true);
  final AtomicReference<IncomingMessageHandler<M>> incomingMessageListener = new AtomicReference<>();

  NonBlockingClient(final MessageReader<M> messageReader,
                    final MessageWriter<M> messageWriter,
                    final ExecutorService selectExecutor,
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

  public void send(M message) {
    outgoingMessages.add(message);
  }

  public void setIncomingMessageListener(IncomingMessageHandler<M> incomingMessageHandler) {
    this.incomingMessageListener.set(incomingMessageHandler);
  }

  void loop() {
    while (isActive.get()) {
      try {
        final int numSelected = selector.select();
        if (0 == numSelected) continue;

        final Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
        while (isActive.get() && iterator.hasNext()) {
          final SelectionKey key = iterator.next();
          final ArrayList<M> newMessages = new ArrayList<>(numSelected);
          try {
            if (key.isReadable()) {
              final Optional<M> newMessage = onReading();
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

  private void deliverMessages(final List<M> newMessages) {
    final IncomingMessageHandler<M> messageListener = this.incomingMessageListener.get();
    if (newMessages.isEmpty() || null == messageListener) {
      return;
    }
    deliveryExecutor.execute(() -> newMessages.forEach(message -> {
      try {
        messageListener.handle(this, message);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }));
  }

  private void onWriting() {
    while (!outgoingMessages.isEmpty()) {
      final M message = outgoingMessages.poll();
      try {
        messageWriter.write(socketChannel, message);
      } catch (IOException e) {
        e.printStackTrace();
        return;
      }
    }
  }

  private Optional<M> onReading() {
    try {
      final M newMessage = messageReader.read(socketChannel);
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
