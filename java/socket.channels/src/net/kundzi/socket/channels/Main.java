package net.kundzi.socket.channels;

import net.kundzi.socket.channels.io.lvmessage.DefaultLvMessage;
import net.kundzi.socket.channels.io.lvmessage.LvMessageWriter;
import net.kundzi.socket.channels.server.SimpleReactorServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.security.SecureRandom;

import static java.lang.System.out;

public class Main {

  static SecureRandom   rnd = new SecureRandom();

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
        final String outMessage = "got it" + inMessage;
        out.println("responding to " + from.getRemoteAddress());
        from.send(new DefaultLvMessage(outMessage.getBytes()));
      } catch (IOException e) {
      }
    });
    simpleReactorServer.start();
    int numberOfClients = 30;
    for (int i = 0; i < numberOfClients; i++) {
      createClientThread(serverSockAddress, i, i * 100);
    }
    Thread.sleep(10000);
    out.println("stopping ...");
    simpleReactorServer.stop();
    simpleReactorServer.join();
  }

  private static void createClientThread(SocketAddress socketAddress, int from, int to) throws IOException {
    final SocketChannel client = SocketChannel.open(socketAddress);
    final Thread clientThread = new Thread(() -> {
      String localAddress = null;
      try {
        localAddress = client.getLocalAddress().toString();
        final LvMessageWriter messageWriter = new LvMessageWriter();
        for (int size = from; size <= to; size++) {
          final byte[] randomString = (RandomString.randomString(size, rnd)).getBytes();
          if (!client.isConnected()) {
            return;
          }
          out.println("sending from: " + localAddress);
          messageWriter.write(client, new DefaultLvMessage(randomString));
          Thread.sleep(1);
        }
      } catch (InterruptedException | IOException e) {
        out.println("sad thread: " + localAddress);
        e.printStackTrace();
      } finally {
        try {
          out.println("client closing: " + localAddress);
          client.close();
          out.println("client closed: " + localAddress);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    });
    clientThread.start();
  }

}
