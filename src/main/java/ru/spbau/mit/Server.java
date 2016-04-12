package ru.spbau.mit;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

/**
 * Created by rebryk on 12/04/16.
 */

public class Server implements Runnable {
    private final Path filePath;
    private final int port;

    private Selector selector;

    public Server(final Path filePath, final int port) {
        this.filePath = filePath;
        this.port = port;
    }

    @Override
    public void run() {
        try {
            setupServer();
            startListening();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startListening() throws IOException {
        byte[] file = Files.readAllBytes(filePath);
        while (true) {
            selector.select();
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                if (key.isAcceptable()) {
                    SocketChannel client = ((ServerSocketChannel) key.channel()).accept();
                    if (client != null) {
                        client.configureBlocking(false);
                        client.socket().setTcpNoDelay(true);
                        final int keys = SelectionKey.OP_CONNECT | SelectionKey.OP_WRITE;
                        client.register(selector, keys, ByteBuffer.wrap(file));
                    }
                } else {
                    SocketChannel client = (SocketChannel) key.channel();
                    ByteBuffer buffer = (ByteBuffer) key.attachment();
                    if (key.isWritable() && buffer.hasRemaining()) {
                        client.write(buffer);
                    } else {
                        client.finishConnect();
                        client.close();
                    }
                }
                iterator.remove();
            }
        }
    }

    private void setupServer() throws IOException {
        selector = Selector.open();
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(port));
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }
}
