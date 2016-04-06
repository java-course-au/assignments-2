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
import java.util.Iterator;

/**
 * Created by ldvsoft on 05.04.16.
 */
public abstract class Server {
    private static final int PORT = 8080;

    public static void main(String[] args) throws IOException {
        Path path = Paths.get(args[0]);
        byte[] file = Files.readAllBytes(path);

        Selector selector = Selector.open();

        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(PORT));
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        //noinspection InfiniteLoopStatement
        while (true) {
            selector.select();
            for (Iterator<? extends SelectionKey> it = selector.selectedKeys().iterator(); it.hasNext();) {
                SelectionKey selectionKey = it.next();
                it.remove();

                if (selectionKey.isAcceptable()) {
                    SocketChannel channel = serverSocketChannel.accept();
                    channel.configureBlocking(false);
                    channel.socket().setTcpNoDelay(true);
                    channel.register(
                            selector,
                            SelectionKey.OP_WRITE | SelectionKey.OP_CONNECT,
                            ByteBuffer.wrap(file)
                    );
                } else if (selectionKey.isWritable()) {
                    SocketChannel channel = (SocketChannel) selectionKey.channel();
                    ByteBuffer buffer = (ByteBuffer) selectionKey.attachment();
                    if (buffer == null) {
                        continue;
                    }
                    if (selectionKey.isWritable()) {
                        if (buffer.hasRemaining()) {
                            channel.write(buffer);
                            if (!buffer.hasRemaining()) {
                                selectionKey.attach(null);
                                channel.finishConnect();
                                channel.close();
                                selectionKey.cancel();
                            }
                        }
                    }
                } else {
                    SocketChannel channel = (SocketChannel) selectionKey.channel();
                    channel.finishConnect();
                    channel.close();
                    selectionKey.cancel();
                }
            }
        }
    }
}
