package ru.spbau.mit;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public abstract class NonBlockingServer {
    private static final int SERVER_PORT = 8081;

    public static void main(String[] args) throws IOException {
        Selector selector = Selector.open();
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(SERVER_PORT));
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        byte[] file = Files.readAllBytes(Paths.get(args[0]));
        Map<SocketChannel, ByteBuffer> buffers = new HashMap<>();

        while (true) {
            selector.select();
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                if (key.isAcceptable()) {
                    SocketChannel client = serverSocketChannel.accept();
                    if (client != null) {
                        client.configureBlocking(false);
                        client.socket().setTcpNoDelay(true);
                        client.register(selector, SelectionKey.OP_WRITE | SelectionKey.OP_CONNECT);
                        ByteBuffer buffer = ByteBuffer.wrap(file);
                        buffers.put(client, buffer);
                    }
                } else if (key.isWritable()) {
                    SocketChannel client = (SocketChannel) key.channel();
                    ByteBuffer buffer = buffers.get(client);
                    if (buffer.hasRemaining()) {
                        client.write(buffer);
                    } else {
                        client.finishConnect();
                        client.close();
                    }
                } else {
                    SocketChannel client = (SocketChannel) key.channel();
                    client.finishConnect();
                    client.close();
                }
                iterator.remove();
            }
        }

    }
}
