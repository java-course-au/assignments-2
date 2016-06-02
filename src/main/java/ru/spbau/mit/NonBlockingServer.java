package ru.spbau.mit;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.channels.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.logging.Logger;

public class NonBlockingServer {

    public static final int PORT = 2007;

    private byte[] src;

    private final Thread workThread = new Thread(() -> {
        try (
                ServerSocketChannel serverChannel = ServerSocketChannel.open();
                Selector selector = Selector.open()
        ) {
            serverChannel.bind(new InetSocketAddress(PORT));
            serverChannel.configureBlocking(false);
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

            while (!Thread.interrupted()) {
                selector.select();
                if (Thread.interrupted()) {
                    break;
                }

                for (Iterator<SelectionKey> selectionKeyIterator = selector.selectedKeys().iterator();
                     selectionKeyIterator.hasNext();) {
                    SelectionKey selectionKey = selectionKeyIterator.next();
                    selectionKeyIterator.remove();

                    if (selectionKey.isAcceptable()) {
                        SocketChannel clientChannel = serverChannel.accept();
                        if (clientChannel != null) {
                            clientChannel.configureBlocking(false);
                            NonBlockingHandler client = new NonBlockingHandler(clientChannel, src);
                            int interest = client.onAcceptable();
                            if (interest != 0) {
                                // use attachment for a convenient further getting of client
                                clientChannel.register(selector, interest, client);
                            }
                        }
                    } else {
                        NonBlockingHandler client = (NonBlockingHandler) selectionKey.attachment();
                        SelectableChannel clientChannel = selectionKey.channel();

                        int interest = 0;
                        if (selectionKey.isReadable()) {
                            interest = client.onReadable();
                        } else if (selectionKey.isWritable()) {
                            interest = client.onWritable();
                        }
                        if (interest != 0) {
                            clientChannel.register(selector, interest, client);
                        } else {
                            selectionKey.cancel();
                        }
                    }
                }
            }
        } catch (SocketException e) {
            Logger.getAnonymousLogger().warning("ServerChannel closed or thread interrupted");
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    });

    public NonBlockingServer(Path p) {
        try {
            src = Files.readAllBytes(p);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void start() {
        workThread.start();
    }

    public void stop() throws InterruptedException {
        workThread.interrupt();
        workThread.join();
    }
}
