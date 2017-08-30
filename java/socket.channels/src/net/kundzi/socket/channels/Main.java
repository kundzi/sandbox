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
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
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

    static class Client {

      private final SocketChannel socketChannel;

      Client(final SocketChannel socketChannel) {
        this.socketChannel = Objects.requireNonNull(socketChannel);
      }

      SocketChannel getSocketChannel() {
        return socketChannel;
      }
    }

    class MessageEvent {
      final LvMessage message;
      final Client from;

      MessageEvent(final LvMessage message, final Client from) {
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
      void onMessage(LvMessage message, Client from);
    }

    private final InetSocketAddress bindAddress;
    private final ExecutorService selectExecutor = Executors.newSingleThreadExecutor();
    private final ExecutorService messageDeliveryExecutor = Executors.newSingleThreadExecutor();

    private final AtomicReference<State> state = new AtomicReference<>(State.NOT_STARTED);
    private final CopyOnWriteArrayList<Client> clients = new CopyOnWriteArrayList<>();
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
          client.getSocketChannel().close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      });
      selectExecutor.shutdown();
      messageDeliveryExecutor.shutdown();
    }

    public void join() {
      if (state.get() != State.STARTED) {
        return;
      }
      try {
        selectExecutor.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS);
        messageDeliveryExecutor.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS);
      } catch (InterruptedException e) {
        e.printStackTrace();
        e.printStackTrace();
      }
    }

    public List<Client> getClients() {
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
              final Client newClient = new Client(socketChannel);
              onAccepting(newClient);
            }

            if (key.isReadable()) {
              final Client client = (Client) key.attachment();
              final LvMessage newMessage = onReading(client);
              newMessages.add(new MessageEvent(newMessage, client));
            }

            if (key.isWritable()) {
              onWriting((Client) key.attachment());
            }

            deliverNewMessages(newMessages);
          } finally {
            iterator.remove();
          }
        }
      }

    }

    private boolean isNotStopped() {
      return state.get() != State.STOPPED;
    }

    void onAccepting(Client client) throws IOException {
      client.getSocketChannel().configureBlocking(false);
      clients.add(client);
      client.getSocketChannel().register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, client);
    }

    LvMessage onReading(Client client) throws IOException {
      final LvMessage newMessage = messageReader.read(client.getSocketChannel());
      return Objects.requireNonNull(newMessage);
    }

    void onWriting(Client client) {
      // TODO deliver pending messages
    }

    private void deliverNewMessages(final ArrayList<MessageEvent> newMessages) {
      messageDeliveryExecutor.execute(() -> {
        for (final MessageEvent newMessage : newMessages) {
          // TODO should we fail if listener is dump and fails?
          messagesListener.onMessage(newMessage.message, newMessage.from);
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
        out.println(from.getSocketChannel().getRemoteAddress() + " " + message.length() + " " + new String(message.data()));
      } catch (IOException e) {
      }
    });
    simpleReactorServer.start();
    createClientThread(serverSockAddress, 1, 100);
    createClientThread(serverSockAddress, 1000, 1000);
    createClientThread(serverSockAddress, 10000, 100000);
    Thread.sleep(1000);
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
          messageWriter.write(client, new DefaultLvMessage(randomString));
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    });
    clientThread.start();
  }

}
