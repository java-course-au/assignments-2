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
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public abstract class Server {
    private static final int PORT = 8080;
    private static final int BUFFER_SIZE = 200000;

    public static void main(String[] args) throws IOException {
        byte[] data;
        Selector selector;
        ServerSocketChannel serverSocketChannel;

        Map<SocketChannel, ByteBuffer> writingBuffer = new HashMap<>();

        Path path = Paths.get(args[0]);
        data = Files.readAllBytes(path);

        selector = Selector.open();
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(PORT));
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);


        while (true) {
            selector.select();
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();
                if (key.isAcceptable()) {
                    SocketChannel socketChannel = serverSocketChannel.accept();

                    if (socketChannel != null) {
                        ByteBuffer buf = ByteBuffer.allocate(BUFFER_SIZE);
                        buf.clear();
                        buf.put(data);
                        buf.flip();

                        writingBuffer.put(socketChannel, buf);

                        socketChannel.configureBlocking(false);
                        socketChannel.socket().setTcpNoDelay(true);
                        socketChannel.register(selector, SelectionKey.OP_WRITE | SelectionKey.OP_CONNECT);
                    }
                } else if (key.isWritable()) {
                    SocketChannel socketChannel = (SocketChannel) key.channel();
                    ByteBuffer buf = writingBuffer.get(socketChannel);

                    if (buf.hasRemaining()) {
                        socketChannel.write(buf);
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
