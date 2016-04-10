package ru.spbau.mit;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.file.Path;
import java.util.Iterator;

public class FileServingServer implements Runnable {
    private final int port;
    private final ByteBuffer fileData;

    public FileServingServer(int port, Path file) throws IOException {
        this.port = port;
        try (FileChannel channel = FileChannel.open(file)) {
            fileData = ByteBuffer.allocate(Math.toIntExact(channel.size()));
            channel.read(fileData);
            fileData.rewind();
        }
    }

    @Override
    public void run() {
        try (Selector selector = Selector.open();
             ServerSocketChannel server = ServerSocketChannel.open()) {
            server.bind(new InetSocketAddress(port));
            server.configureBlocking(false);
            server.register(selector, SelectionKey.OP_ACCEPT);
            work(selector);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void work(Selector selector) {
        while (!Thread.interrupted()) {
            try {
                selector.select();
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }

            Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();

            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();
                if (key.isAcceptable()) {
                    SocketChannel client = null;
                    try {
                        client = ((ServerSocketChannel) key.channel()).accept();
                        client.socket().setTcpNoDelay(true);
                        client.configureBlocking(false);
                    } catch (IOException e) {
                        e.printStackTrace();
                        if (client != null) {
                            try {
                                client.close();
                            } catch (IOException ignored) {
                            }
                        }
                        continue;
                    }
                    try {
                        client.register(selector, SelectionKey.OP_WRITE, fileData.asReadOnlyBuffer());
                    } catch (ClosedChannelException ignored) {
                    }
                } else if (key.isWritable()) {
                    SocketChannel client = (SocketChannel) key.channel();
                    ByteBuffer buffer = (ByteBuffer) key.attachment();
                    if (buffer.hasRemaining()) {
                        try {
                            client.write(buffer);
                        } catch (IOException e) {
                            e.printStackTrace();
                            key.cancel();
                            try {
                                client.close();
                            } catch (IOException ignored) {
                            }
                        }
                    } else {
                        try {
                            client.close();
                        } catch (IOException ignored) {
                        }
                        key.cancel();
                    }
                }
                keyIterator.remove();
            }
        }
    }
}
