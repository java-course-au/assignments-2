package ru.spbau.mit;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Random;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

public class TestServer {
    private static final int CLIENT_CNT = 10;
    // CHECKSTYLE.OFF: MagicNumber
    private static final int FILE_SIZE = 1 << 20;
    // CHECKSTYLE.ON: MagicNumber

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testServer() throws IOException, InterruptedException {
        File file = temporaryFolder.newFile();
        byte[] data = new byte[FILE_SIZE];
        new Random().nextBytes(data);
        new FileOutputStream(file).write(data);
        NonBlockingServer server = new NonBlockingServer(file.toPath());
        server.start();

        Thread[] threads = new Thread[CLIENT_CNT];
        for (int i = 0; i < CLIENT_CNT; i++) {
            threads[i] = new Thread(() -> {
                try {
                    Socket socket = new Socket("localhost", NonBlockingServer.SERVER_PORT);
                    byte[] clientData = new byte[FILE_SIZE];
                    int curOffset = 0;
                    InputStream stream = socket.getInputStream();
                    while (!socket.isClosed() && curOffset < FILE_SIZE) {
                        int read = stream.read(clientData, curOffset, FILE_SIZE - curOffset);
                        if (read == -1) {
                            break;
                        }
                        curOffset += read;
                    }
                    assertTrue(stream.read() == -1);
                    assertArrayEquals(data, clientData);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            threads[i].start();
        }
        for (int i = 0; i < CLIENT_CNT; i++) {
            threads[i].join();
        }
    }
}
