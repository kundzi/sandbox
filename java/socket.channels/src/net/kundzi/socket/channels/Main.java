package net.kundzi.socket.channels;

import net.kundzi.socket.channels.io.MessageReader;
import net.kundzi.socket.channels.io.MessageWriter;
import net.kundzi.socket.channels.io.lvmessage.DefaultLvMessage;
import net.kundzi.socket.channels.io.lvmessage.LvMessage;
import net.kundzi.socket.channels.io.lvmessage.LvMessageReader;
import net.kundzi.socket.channels.io.lvmessage.LvMessageWriter;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.System.out;
import static net.kundzi.socket.channels.RandomString.randomString;

public class Main {

  static SecureRandom rnd = new SecureRandom();

  public static void main(String[] args) throws Exception {
    nonblockingServer();
  }

  static class SimpleReactorServer {

    static class ClientConnection {

      private final SocketChannel socketChannel;
      private final ConcurrentLinkedDeque<LvMessage> outgoingMessages = new ConcurrentLinkedDeque<>();

      ClientConnection(final SocketChannel socketChannel) {
        this.socketChannel = Objects.requireNonNull(socketChannel);
      }

      SocketChannel getSocketChannel() {
        return socketChannel;
      }

      void send(LvMessage message) {
        outgoingMessages.add(message);
      }

      Deque<LvMessage> getOutgoingMessages() {
        return outgoingMessages;
      }

      int getNumberOfOutgoingMessages() {
        return outgoingMessages.size();
      }
    }

    class MessageEvent {
      final LvMessage message;
      final ClientConnection from;

      MessageEvent(final LvMessage message, final ClientConnection from) {
        this.message = message;
        this.from = from;
      }
    }

    public SimpleReactorServer(final InetSocketAddress bindAddress,
                               final MessagesListener messagesListener) {
      this.bindAddress = Objects.requireNonNull(bindAddress);
      this.messagesListener = Objects.requireNonNull(messagesListener);
    }

    enum State {
      NOT_STARTED,
      STARTED,
      STOPPED
    }

    public interface MessagesListener {
      void onMessage(LvMessage message, ClientConnection from);
    }

    private final InetSocketAddress bindAddress;
    private final ExecutorService selectExecutor = Executors.newSingleThreadExecutor();
    private final ExecutorService incomingMessagesDeliveryExecutor = Executors.newSingleThreadExecutor();
    private final ExecutorService outgoingMessagesDeliveryExecutor = Executors.newSingleThreadExecutor();

    private final AtomicReference<State> state = new AtomicReference<>(State.NOT_STARTED);
    private final CopyOnWriteArrayList<ClientConnection> clients = new CopyOnWriteArrayList<>();
    private Selector selector;
    private ServerSocketChannel boundServerChannel;

    private final MessageReader<LvMessage> messageReader = new LvMessageReader();
    private final MessageWriter<LvMessage> messageWriter = new LvMessageWriter();
    private final MessagesListener messagesListener;

    public void start() throws IOException {
      selector = Selector.open();
      boundServerChannel = ServerSocketChannel.open().bind(bindAddress);
      boundServerChannel.configureBlocking(false);
      boundServerChannel.register(selector, SelectionKey.OP_ACCEPT, null);

      selectExecutor.execute(this::loop);
      state.set(State.STARTED);
    }

    public void stop() {
      state.set(State.STOPPED);
      getClients().forEach(client -> {
        try {
          out.println("server closing :" + client.getSocketChannel().getRemoteAddress());
          client.getSocketChannel().close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      });
      selectExecutor.shutdown();
      incomingMessagesDeliveryExecutor.shutdown();
      outgoingMessagesDeliveryExecutor.shutdown();
    }

    public void join() {
      if (state.get() != State.STARTED) {
        return;
      }
      try {
        selectExecutor.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS);
        incomingMessagesDeliveryExecutor.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS);
        outgoingMessagesDeliveryExecutor.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS);
      } catch (InterruptedException e) {
        e.printStackTrace();
        e.printStackTrace();
      }
    }

    public List<ClientConnection> getClients() {
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
          final ArrayList<MessageEvent> newMessages = new ArrayList<>(numSelected);

          try {
            if (key.isAcceptable()) {
              final SocketChannel socketChannel = boundServerChannel.accept();
              final ClientConnection newClient = new ClientConnection(socketChannel);
              onAccepting(newClient);
            }

            if (key.isReadable()) {
              final ClientConnection client = (ClientConnection) key.attachment();
              onReading(client).ifPresent(message -> newMessages.add(new MessageEvent(message, client)));
            }

            if (key.isWritable()) {
              onWriting((ClientConnection) key.attachment());
            }

            deliverNewMessages(newMessages);
          } finally {
            iterator.remove();
          }
        }
      }

    }

    private void sendOutgoingMessages(ClientConnection clientConnection) {
      if (clientConnection.getNumberOfOutgoingMessages() == 0) {
        return;
      }
      outgoingMessagesDeliveryExecutor.execute(() -> {
        try {
          int numSent = 0;
          int pending = clientConnection.getNumberOfOutgoingMessages();
          if (clientConnection.getSocketChannel().isConnected()) {
            final LvMessage outgoingMessage = clientConnection.getOutgoingMessages().poll();
            if (outgoingMessage != null) {
              try {
                messageWriter.write(clientConnection.getSocketChannel(), outgoingMessage);
                numSent++;
              } catch (IOException e) {
              }
            }
          }
          if (numSent + pending != 0) {
            out.println("Sent: " + numSent +
                            " pending: " + pending +
                            " connected: " + clientConnection.getSocketChannel().isConnected() +
                            " addr: " + clientConnection.getSocketChannel().getRemoteAddress());
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
      clients.add(client);
      client.getSocketChannel().register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, client);
    }

    Optional<LvMessage> onReading(ClientConnection client) throws IOException {
      return messageReader.read(client.getSocketChannel());
    }

    void onWriting(ClientConnection client) {
      sendOutgoingMessages(client);
    }

    private void deliverNewMessages(final ArrayList<MessageEvent> newMessages) {
      incomingMessagesDeliveryExecutor.execute(() -> {
        for (final MessageEvent newMessage : newMessages) {
          try {
            messagesListener.onMessage(newMessage.message, newMessage.from);
          } catch (Exception e) {
            // TODO better logging here
            e.printStackTrace();
          }
        }
      });
    }

  }

  private static void nonblockingServer() throws IOException, InterruptedException {
    final String host = "localhost";
    final int port = 6677;
    final InetSocketAddress serverSockAddress = new InetSocketAddress(host, port);

    final SimpleReactorServer simpleReactorServer = new SimpleReactorServer(serverSockAddress, (message, from) -> {
      try {
        final String inMessage = new String(message.data());
        out.println(from.getSocketChannel().getRemoteAddress() + " " + message.length() + " " + inMessage);
        final String outMessage = "got it" + inMessage;
        out.println("responding to " + from.getSocketChannel().getRemoteAddress());
        from.send(new DefaultLvMessage(outMessage.getBytes()));
      } catch (IOException e) {
      }
    });
    simpleReactorServer.start();
    createClientThread(serverSockAddress, 1, 100);
    createClientThread(serverSockAddress, 1000, 1000);
    createClientThread(serverSockAddress, 10000, 100000);
    Thread.sleep(5000);
    out.println("stopping ...");
    simpleReactorServer.stop();
    simpleReactorServer.join();
  }

  private static void createClientThread(SocketAddress socketAddress, int from, int to) throws IOException {
    final SocketChannel client = SocketChannel.open(socketAddress);
    final Thread clientThread = new Thread(() -> {
      try {
        final LvMessageWriter messageWriter = new LvMessageWriter();
        for (int size = from; size <= to; size++) {
          final byte[] randomString = (randomString(size, rnd)).getBytes();
          if (!client.isConnected()) {
            return;
          }
          out.println("sending from: " + client.getLocalAddress());
          messageWriter.write(client, new DefaultLvMessage(randomString));
          Thread.sleep(100);
        }
      } catch (InterruptedException | IOException e) {
        e.printStackTrace();
      } finally {
        try {
          out.println("closing: " + client.getLocalAddress());
          client.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    });
    clientThread.start();
  }

}
