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
import java.util.HashMap;
import java.util.Iterator;

public class nonBlockingServer {
    private Selector selector = null;
    private ServerSocketChannel server = null;
    private byte[] src;

    {
        try {
            selector = Selector.open();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        try {
            server = ServerSocketChannel.open();
            server.socket().bind(new InetSocketAddress(2007));
            server.configureBlocking(false);
            server.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public nonBlockingServer(Path p) {
        try {
            src = Files.readAllBytes(p);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void run() throws IOException {
        while(true) {
            selector.select();
            for(Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                    keyIterator.hasNext();) {
                SelectionKey key = keyIterator.next();
                if(key.isAcceptable()) {
                    SocketChannel client = server.accept();
                    client.configureBlocking(false);
                    client.socket().setTcpNoDelay(false);
                    client.register(
                            selector,
                            SelectionKey.OP_READ | SelectionKey.OP_CONNECT
                    );
                }
                keyIterator.remove();
            }
        }
    }
}
