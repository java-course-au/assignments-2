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

public class NonBlockingServer {
    private static final int PORT = 8081;

    private String path;

    public NonBlockingServer(String path) throws IOException {
        this.path = path;
    }

    public static void main(String[] args) throws IOException {
        new NonBlockingServer(args[0]).start();
    }

    public void start() throws IOException {
        byte[] data = Files.readAllBytes(Paths.get(path));
        Map<SocketChannel, ByteBuffer> buffers = new HashMap<>();

        Selector selector = Selector.open();
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(PORT));
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        while (true) {
            selector.select();
            Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();
                if (key.isAcceptable()) {
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    if (socketChannel != null) {
                        buffers.put(socketChannel, ByteBuffer.wrap(data));
                        socketChannel.configureBlocking(false);
                        socketChannel.socket().setTcpNoDelay(true);
                        socketChannel.register(selector, SelectionKey.OP_WRITE | SelectionKey.OP_CONNECT);
                    }
                } else if (key.isWritable()) {
                    SocketChannel socketChannel = (SocketChannel) key.channel();
                    ByteBuffer buffer = buffers.get(socketChannel);
                    if (buffer.hasRemaining()) {
                        socketChannel.write(buffer);
                    } else {
                        socketChannel.finishConnect();
                        socketChannel.close();
                    }
                } else {
                    SocketChannel socketChannel = (SocketChannel) key.channel();
                    socketChannel.finishConnect();
                    socketChannel.close();
                }
                keyIterator.remove();
            }
        }
    }
}
