import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by n_buga on 14.05.16.
 */
public class NonBlockingServer {
    private static final int PORT = 8081;
    private static final int TIMEOUT = 100;

    private Path filePath;
    private int fileSize;
    private boolean end = false;
    private Thread serverThread;

    public NonBlockingServer(Path filePath) {
        this.filePath = filePath;
        try {
            fileSize = (int) Files.size(filePath);
        } catch (IOException e) {
            System.out.printf("The file doesn't exist\n");
        }
    }

    public Thread start() {
        serverThread = new Thread(this::startWork);
        serverThread.start();
        return serverThread;
    }

    public void close() {
        end = true;
        try {
            serverThread.join();
        } catch (InterruptedException e) {
            System.out.printf("The ending of programm is not correct\n");
        }
    }

    private ByteBuffer newBuffer() {
        ByteBuffer fileContent = ByteBuffer.allocate(fileSize + 4);
        fileContent.putInt(fileSize);
        ByteChannel fileChannel;
        try {
            fileChannel = Files.newByteChannel(filePath);
        } catch (IOException e) {
            System.out.printf("Can't create a channel from file\n");
            return null;
        }
        int readBytes = 1;
        while (readBytes > 0) {
            try {
                readBytes = fileChannel.read(fileContent);
            } catch (IOException e) {
                System.out.printf("Can't read from file\n");
                return null;
            }
        }
        fileContent.flip();
        return fileContent;
    }

    private void startWork() {
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
                Selector selector = Selector.open()) {
            serverSocketChannel.bind(new InetSocketAddress(PORT));
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            clientsHandler(selector);
        } catch (IOException e) {
            System.out.printf("Can't open ServerSocketChannel or Selector\n");
        }
    }

    private void clientsHandler(Selector selector) {
        while (!end) {
            try {
                int countReady = selector.select(TIMEOUT);
                if (countReady == 0) {
                    continue;
                }
            } catch (IOException e) {
                System.out.printf("The problems with select from Selector.\n");
                return;
            }
            Set<SelectionKey> selectionKeySet = selector.selectedKeys();
            Iterator<SelectionKey> keyIterator = selectionKeySet.iterator();
            while (keyIterator.hasNext()) {
                SelectionKey curSelectionKey = keyIterator.next();
                handlerSelectionKey(selector, curSelectionKey);
                keyIterator.remove();
            }
        }
    }

    private void handlerSelectionKey(Selector selector, SelectionKey selectionKey) {
        if (selectionKey.isAcceptable()) {
            ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
            SocketChannel socketChannel = null;
            try {
                socketChannel = serverSocketChannel.accept();
                socketChannel.configureBlocking(false);
            } catch (IOException e) {
                System.out.printf("The problem with accept a connection\n");
                return;
            }
            try {
                socketChannel.register(selector, SelectionKey.OP_WRITE, newBuffer());
            } catch (ClosedChannelException e) {
                System.out.printf("The problem with register a new client\n");
            }
        } else if (selectionKey.isWritable()) {
            ByteBuffer curByteBuffer = (ByteBuffer) selectionKey.attachment();
            SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
            try {
                int writingBytes = socketChannel.write(curByteBuffer);
                if (!curByteBuffer.hasRemaining()) {
                    socketChannel.close();
                    selectionKey.cancel();
                }
            } catch (IOException e) {
                System.out.printf("Can't write to file\n");
            }
        }

    }
}
