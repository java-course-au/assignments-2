package ru.spbau.mit;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class NonBlockingHandler
        implements NonBlockingBase {
    private final SocketChannel client;
    private final ByteBuffer clientBuffer;


    public NonBlockingHandler(SocketChannel client, byte[] src) {
        this.client = client;
        clientBuffer = ByteBuffer.wrap(src);
    }

    @Override
    public int onAcceptable() {
        return SelectionKey.OP_WRITE;

    }

    @Override
    public int onReadable() {
        return 0;
    }

    @Override
    public int onWritable() throws IOException {
        client.write(clientBuffer);
        if (clientBuffer.remaining() > 0) {
            return SelectionKey.OP_WRITE;
        }
        client.close();
        return 0;
    }
}
