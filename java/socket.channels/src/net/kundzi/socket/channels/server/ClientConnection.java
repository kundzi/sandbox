package net.kundzi.socket.channels.server;

import net.kundzi.socket.channels.message.Message;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Deque;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClientConnection<M extends Message> {

  private final SocketChannel socketChannel;
  private final ConcurrentLinkedDeque<M> outgoingMessages = new ConcurrentLinkedDeque<>();
  private SelectionKey key;
  private AtomicBoolean isMarkedDead = new AtomicBoolean(false);

  ClientConnection(final SocketChannel socketChannel) {
    this.socketChannel = Objects.requireNonNull(socketChannel);
  }

  public void send(M message) {
    outgoingMessages.add(message);
  }

  public SocketAddress getRemoteAddress() throws IOException {
    return socketChannel.getRemoteAddress();
  }

  SocketChannel getSocketChannel() {
    return socketChannel;
  }

  Deque<M> getOutgoingMessages() {
    return outgoingMessages;
  }

  int getNumberOfOutgoingMessages() {
    return outgoingMessages.size();
  }

  void register(Selector selector) throws ClosedChannelException {
    if (key != null) {
      throw new IllegalStateException();
    }
    key = getSocketChannel().register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, this);
  }

  void unregister() {
    if (key.isValid()) {
      key.cancel();
    }
  }

  boolean isMarkedDead() {
    return isMarkedDead.get();
  }

  void markDead() {
    isMarkedDead.set(true);
  }
}
