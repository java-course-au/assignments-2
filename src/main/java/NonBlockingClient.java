import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created by n_buga on 14.05.16.
 */
public class NonBlockingClient {
    public static final int MAX_SIZE = 1000000;

    public static final int PORT = 8081;
    private final String ip;
    private FileChannel fileChannel;

    public NonBlockingClient(String ip, Path filePath) {
        this.ip = ip;
        try {
            Files.deleteIfExists(filePath);
            Files.createFile(filePath);
        } catch (IOException e) {
            System.out.printf("We actualy have some problem with your path. Maybe the directory isn't exist?\n");
            return;
        }
        try {
            this.fileChannel = (new RandomAccessFile(filePath.toString(), "rw")).getChannel();
        } catch (IOException e) {
            System.out.printf("Can't open file on writing.\n");
        }
    }

    public void request() {
        ByteBuffer byteSize = ByteBuffer.allocate(4);
        ByteBuffer byteBuffer = ByteBuffer.allocate(MAX_SIZE);
        ByteBuffer[] buffers = {byteSize, byteBuffer};
        int size = 0;
        try (SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress(ip, PORT))) {
            int writtenBytes = 0;
            while (true) {
                writtenBytes += (int) socketChannel.read(buffers);
                if (writtenBytes >= 4) {
                    byteSize.flip();
                    size = byteSize.getInt();
                    break;
                }
            }
            writtenBytes -= 4;
            while (writtenBytes < size) {
                writtenBytes += (int) socketChannel.write(byteBuffer);
            }
        } catch (IOException e) {
            System.out.printf("Can't get the connection\n");
            return;
        }

        byteBuffer.flip();
        int writtenBytes = 0;
        while (writtenBytes < size) {
            try {
                writtenBytes += (int) fileChannel.write(byteBuffer);
            } catch (IOException e) {
                System.out.printf("Can't write to file\n");
                break;
            }
        }
    }
}
