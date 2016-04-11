package ru.spbau.mit;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

public class TestNioServer {
    private static final Path GET_PATH = Paths.get("src/test/resources/file");
    private static final int SERVER_READY_TIMEOUT = 1000;
    private static final int SERVER_PORT = 8888;
    private static final int BUFFER_SIZE = 4096;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    void checkWithFile(File file) throws IOException {
        SocketChannel channel = SocketChannel.open(new InetSocketAddress(SERVER_PORT));

        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        FileChannel fileChannel = new FileOutputStream(file).getChannel();
        int status = channel.read(buffer);
        while (status != -1) {
            buffer.flip();
            while (buffer.hasRemaining()) {
                fileChannel.write(buffer);
            }
            buffer.clear();
            status = channel.read(buffer);
        }
        assertTrue(FileUtils.contentEquals(GET_PATH.toFile(), file));
    }

    @Test
    public void test() throws IOException, InterruptedException {
        new Thread(new NioServer(GET_PATH, System.err, SERVER_PORT)).start();
        Thread.sleep(SERVER_READY_TIMEOUT);
        final int iterations = 100;
        for (int i = 0; i < iterations; i++) {
            checkWithFile(temporaryFolder.newFile());
        }

    }
}
