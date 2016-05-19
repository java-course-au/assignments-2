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
 * Created by Сева on 19.05.2016.
 */
public class NonBlockingServer {
    public static final int SERVER_PORT = 44444;
    private final byte[] data;
    private Thread serverThread;

    public NonBlockingServer(Path filePath) throws IOException {
        data = Files.readAllBytes(filePath);
    }

    public void start() {
        serverThread = new Thread(this::runServer);
        serverThread.start();
    }

    private void runServer() {

        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            serverSocketChannel.bind(new InetSocketAddress(SERVER_PORT));
            serverSocketChannel.configureBlocking(false);
            Selector selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            while (true) {
                selector.select();
                for (Iterator<SelectionKey> it = selector.selectedKeys().iterator(); it.hasNext();) {
                    SelectionKey selectionKey = it.next();
                    it.remove();
                    if (selectionKey.isAcceptable()) {
                        SocketChannel socketChannel = serverSocketChannel.accept();
                        ByteBuffer buffer = ByteBuffer.wrap(data);
                        socketChannel.configureBlocking(false);
                        socketChannel.socket().setTcpNoDelay(true);
                        socketChannel.register(selector, SelectionKey.OP_WRITE, buffer);
                    } else if (selectionKey.isWritable()) {
                        ByteBuffer buffer = (ByteBuffer) selectionKey.attachment();
                        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                        if (buffer.hasRemaining()) {
                            socketChannel.write(buffer);
                            if (!buffer.hasRemaining()) {
                                selectionKey.cancel();
                                socketChannel.finishConnect();
                                socketChannel.close();
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
