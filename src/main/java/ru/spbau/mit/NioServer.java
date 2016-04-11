package ru.spbau.mit;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.file.Path;

public class NioServer implements Runnable {

    private class HelperContext {
        private FileChannel savedFileChannel;
        private ByteBuffer savedByteBuffer;

        HelperContext(FileChannel savedFileChannel, ByteBuffer savedByteBuffer) {
            this.savedFileChannel = savedFileChannel;
            this.savedByteBuffer = savedByteBuffer;
        }

        FileChannel getSavedFileChannel() {
            return savedFileChannel;
        }

        ByteBuffer getSavedByteBuffer() {
            return savedByteBuffer;
        }
    }

    private static final int DEFAULT_BUF_SIZE = 8192;

    private PrintStream log;
    private Path filePath;
    private Selector selector;
    private int port;

    public NioServer(Path filePath, PrintStream log, int port) {
        this.filePath = filePath;
        this.log = log;
        this.port = port;
    }

    private void preRunningInit() throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(port));
        serverSocketChannel.configureBlocking(false);
        selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    private void closeAllChannels(SocketChannel savedSocketChanel, HelperContext context) {
        if (context != null) {
            try {
                context.savedFileChannel.close();
            } catch (IOException e) {
                e.printStackTrace(log);
            }
        }
        try {
            savedSocketChanel.close();
        } catch (IOException e) {
            e.printStackTrace(log);
        }
    }

    private boolean sendFile(SocketChannel savedSocketChanel, HelperContext context) {
        try {
            if (!context.savedByteBuffer.hasRemaining()) {
                context.savedByteBuffer.clear();
                int status = context.savedFileChannel.read(context.getSavedByteBuffer());
                if (status == -1) {
                    closeAllChannels(savedSocketChanel, context);
                    return true;
                }
                context.savedByteBuffer.flip();
            }
            savedSocketChanel.write(context.getSavedByteBuffer());
        } catch (IOException e) {
            e.printStackTrace(log);
            closeAllChannels(savedSocketChanel, context);
            return true;
        }
        return false;
    }

    private HelperContext newHelperContext() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(DEFAULT_BUF_SIZE);
        buffer.flip();
        FileChannel fileChannel = FileChannel.open(filePath);
        return new HelperContext(fileChannel, buffer);
    }

    private void newClientConnected(ServerSocketChannel serverSocketChannel) {
        SocketChannel newChannel;
        try {
            newChannel = serverSocketChannel.accept();
        } catch (IOException e) {
            e.printStackTrace(log);
            return;
        }

        if (newChannel != null) {
            try {
                newChannel.socket().setTcpNoDelay(true);
                newChannel.configureBlocking(false);
                HelperContext content = newHelperContext();
                newChannel.register(selector, SelectionKey.OP_WRITE, content);
            } catch (IOException e) {
                e.printStackTrace(log);
                closeAllChannels(newChannel, null);
            }
        }
    }

    private void dispatch(SelectionKey key) {
        if (key.isAcceptable()) {
            newClientConnected((ServerSocketChannel) key.channel());
        } else if (key.isWritable()) {
            if (sendFile((SocketChannel) key.channel(), (HelperContext) key.attachment())) {
                key.cancel();
            }
        }
    }

    private void runningLoop() {
        while (true) {
            int ready = 0;
            try {
                ready = selector.select();
            } catch (IOException e) {
                e.printStackTrace(log);
                continue;
            }

            if (ready > 0) {
                selector.selectedKeys().forEach(this::dispatch);
                selector.selectedKeys().clear();
            }

        }
    }

    @Override
    public void run() {
        try {
            preRunningInit();
        } catch (IOException e) {
            e.printStackTrace(log);
            return;
        }
        runningLoop();
    }
}
