package net.kundzi.socket.channels.server;

import net.kundzi.socket.channels.message.Message;
import net.kundzi.socket.channels.message.MessageReader;
import net.kundzi.socket.channels.message.MessageWriter;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.System.out;
import static java.util.stream.Collectors.toList;

public class SimpleReactorServer<M extends Message> {

  public interface IncomingMessageListener<M extends Message> {
    void onMessage(M message, ClientConnection from);
  }

  enum State {
    NOT_STARTED,
    STARTED,
    STOPPED
  }

  private static class MessageEvent<M> {
    final M message;
    final ClientConnection from;

    MessageEvent(final M message, final ClientConnection from) {
      this.message = message;
      this.from = from;
    }
  }

  public static <M extends Message> SimpleReactorServer<M> start(final InetSocketAddress bindAddress,
                                                                 final IncomingMessageListener<M> incomingMessageListener,
                                                                 final MessageReader<M> messageReader,
                                                                 final MessageWriter<M> messageWriter) throws IOException {

    final ServerSocketChannel socketChannel = ServerSocketChannel.open().bind(bindAddress);
    socketChannel.configureBlocking(false);
    final Selector selector = Selector.open();
    socketChannel.register(selector, SelectionKey.OP_ACCEPT, null);

    return new SimpleReactorServer(selector,
                                   socketChannel,
                                   incomingMessageListener,
                                   messageReader,
                                   messageWriter,
                                   Executors.newSingleThreadExecutor(),
                                   Executors.newSingleThreadExecutor(),
                                   Executors.newSingleThreadExecutor(),
                                   Executors.newSingleThreadScheduledExecutor());

  }

  public SimpleReactorServer(final Selector selector,
                             final ServerSocketChannel boundServerChannel,
                             final IncomingMessageListener incomingMessageListener,
                             final MessageReader<M> messageReader,
                             final MessageWriter<M> messageWriter,
                             final ExecutorService selectExecutor,
                             final ExecutorService incomingMessagesDeliveryExecutor,
                             final ExecutorService outgoingMessagesDeliveryExecutor,
                             final ScheduledExecutorService reaperExecutor) {
    this.selector = selector;
    this.boundServerChannel = boundServerChannel;
    this.incomingMessageListener = Objects.requireNonNull(incomingMessageListener);
    this.messageReader = messageReader;
    this.messageWriter = messageWriter;
    this.selectExecutor = selectExecutor;
    this.incomingMessagesDeliveryExecutor = incomingMessagesDeliveryExecutor;
    this.outgoingMessagesDeliveryExecutor = outgoingMessagesDeliveryExecutor;
    this.reaperExecutor = reaperExecutor;

    state.set(State.STARTED);
    selectExecutor.execute(this::loop);
    reaperExecutor.scheduleAtFixedRate(this::harvestDeadConnections, 100, 100, TimeUnit.MILLISECONDS);
  }

  private final ExecutorService selectExecutor;
  private final ExecutorService incomingMessagesDeliveryExecutor;
  private final ExecutorService outgoingMessagesDeliveryExecutor;
  private final ScheduledExecutorService reaperExecutor;

  private final Selector selector;
  private final ServerSocketChannel boundServerChannel;

  private final MessageReader<M> messageReader;
  private final MessageWriter<M> messageWriter;
  private final IncomingMessageListener<M> incomingMessageListener;

  private final AtomicReference<State> state = new AtomicReference<>(State.NOT_STARTED);
  private final CopyOnWriteArrayList<ClientConnection> clients = new CopyOnWriteArrayList<>();

  private void harvestDeadConnections() {
    if (isNotStopped()) {
      final List<ClientConnection> deadConnections = clients.stream()
          .filter(ClientConnection::isMarkedDead)
          .collect(toList());

      if (deadConnections.isEmpty()) {
        return;
      }

      out.println("Harvested clients: " + deadConnections.size());
      deadConnections.stream().forEach(clientConnection -> {
        try {
          out.println("removing: " + clientConnection.getSocketChannel().getRemoteAddress());
          clientConnection.unregister();
          clientConnection.getSocketChannel().close();
        } catch (IOException e) {
        }
      });
      clients.removeAll(deadConnections);
    }
  }

  public void stop() {
    state.set(State.STOPPED);
    try {
      selector.close();
    } catch (IOException e) {
    }
    getClients().forEach(client -> {
      try {
        out.println("server closing :" + client.getRemoteAddress());
        client.unregister();
        client.getSocketChannel().close();
        out.println("server closed :" + client.getRemoteAddress());
      } catch (IOException e) {
        e.printStackTrace();
      }
    });
    selectExecutor.shutdown();
    incomingMessagesDeliveryExecutor.shutdown();
    outgoingMessagesDeliveryExecutor.shutdown();
    reaperExecutor.shutdown();
  }

  public void join() {
    if (state.get() != State.STARTED) {
      return;
    }
    try {
      selectExecutor.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS);
      incomingMessagesDeliveryExecutor.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS);
      outgoingMessagesDeliveryExecutor.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS);
      reaperExecutor.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  List<ClientConnection> getClients() {
    return Collections.unmodifiableList(clients);
  }

  private void loop() {
    try {
      _loop();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void _loop() throws IOException {
    while (isNotStopped()) {
      final int numSelected = selector.select();
      if (0 == numSelected) {
        continue;
      }

      final Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
      while (isNotStopped() && iterator.hasNext()) {
        final SelectionKey key = iterator.next();
        final ArrayList<MessageEvent<M>> newMessages = new ArrayList<>(numSelected);

        try {
          if (key.isAcceptable()) {
            final SocketChannel socketChannel = boundServerChannel.accept();
            final ClientConnection newClient = new ClientConnection(socketChannel);
            onAccepting(newClient);
          }

          if (key.isReadable()) {
            final ClientConnection client = (ClientConnection) key.attachment();
            onReading(client).ifPresent(message -> newMessages.add(new MessageEvent<>(message, client)));
          }

          if (key.isWritable()) {
            onWriting((ClientConnection) key.attachment());
          }

          deliverNewMessages(newMessages);
        } catch (CancelledKeyException e) {
          e.printStackTrace();
          // carry on
        } finally {
          iterator.remove();
        }
      }
    }

  }

  private void sendOutgoingMessages(ClientConnection<M> clientConnection) {
    if (clientConnection.getNumberOfOutgoingMessages() == 0) {
      return;
    }
    outgoingMessagesDeliveryExecutor.execute(() -> {
      try {
        int numSent = 0;
        int pending = clientConnection.getNumberOfOutgoingMessages();
        if (clientConnection.getSocketChannel().isConnected()) {
          final M outgoingMessage = clientConnection.getOutgoingMessages().poll();
          if (outgoingMessage != null) {
            try {
              messageWriter.write(clientConnection.getSocketChannel(), outgoingMessage);
              numSent++;
            } catch (IOException e) {
            }
          }
        }
        if (numSent + pending != 0) {
          out.println("Sent=" + numSent +
                          " pending=" + pending +
                          " addr=" + clientConnection.getRemoteAddress());
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    });
  }

  private boolean isNotStopped() {
    return state.get() != State.STOPPED;
  }

  void onAccepting(ClientConnection client) throws IOException {
    client.getSocketChannel().configureBlocking(false);
    client.register(selector);
    clients.add(client);
  }

  Optional<M> onReading(ClientConnection client) {
    try {
      final M message = messageReader.read(client.getSocketChannel());
      return Optional.of(message);
    } catch (IOException e) {
      client.markDead();
      return Optional.empty();
    }
  }

  void onWriting(ClientConnection client) {
    sendOutgoingMessages(client);
  }

  private void deliverNewMessages(final List<MessageEvent<M>> newMessages) {
    incomingMessagesDeliveryExecutor.execute(() -> {
      for (final MessageEvent<M> newMessage : newMessages) {
        try {
          incomingMessageListener.onMessage(newMessage.message, newMessage.from);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }

}
