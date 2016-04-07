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

    private void sendFile(SocketChannel savedSocketChanel, HelperContext context) {
        int status = 0;
        while (true) {
            try {
                context.savedByteBuffer.clear();
                status = context.savedFileChannel.read(context.savedByteBuffer);
                if (status != -1) {
                    context.savedByteBuffer.flip();
                    int num = savedSocketChanel.write(context.savedByteBuffer);
                } else {
                    closeAllChannels(savedSocketChanel, context);
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace(log);
                closeAllChannels(savedSocketChanel, context);
                break;
            }
        }
    }

    private HelperContext newHelperContext() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(DEFAULT_BUF_SIZE);
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
            sendFile((SocketChannel) key.channel(), (HelperContext) key.attachment());
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
