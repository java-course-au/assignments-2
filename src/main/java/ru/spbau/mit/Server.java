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
import java.util.Iterator;

public final class Server {
    public static final int PORT = 17239;
    private static final String FILENAME = "datafile";

    public static void main(String[] args) throws IOException {
        byte[] fileContents = Files.readAllBytes(Paths.get(FILENAME));

        Selector selector = Selector.open();

        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(PORT));
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        while (selector.isOpen() && serverSocketChannel.isOpen()) {
            selector.select();
            for (Iterator<SelectionKey> iterator = selector.selectedKeys().iterator(); iterator.hasNext();
                 iterator.remove()) {
                final SelectionKey selectionKey = iterator.next();
                if (selectionKey.isAcceptable()) {
                    SocketChannel socketChannel = ((ServerSocketChannel) selectionKey.channel()).accept();
                    socketChannel.configureBlocking(false);
                    socketChannel.socket().setTcpNoDelay(true);
                    socketChannel.register(selector, SelectionKey.OP_WRITE, ByteBuffer.wrap(fileContents));
                } else {
                    ByteBuffer byteBuffer = (ByteBuffer) selectionKey.attachment();
                    SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                    if (selectionKey.isWritable()) {
                        socketChannel.write(byteBuffer);
                        if (!byteBuffer.hasRemaining()) {
                            socketChannel.close();
                        }
                    }
                }
            }
        }
    }

    private Server() {
    }
}
