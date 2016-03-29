package ru.spbau;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.apache.commons.io.IOUtils;
import static org.junit.Assert.assertArrayEquals;

/**
 * Created by rebryk on 10/03/16.
 */
public class Tests {
    public static final int PORT = 1024;
    public static final String HOSTNAME = "localhost";

    public static final String DIR_PATH = "src/test/resources/test";
    public static final String FILE_PATH = "src/test/resources/test/1.png";

    public static final int CLIENTS_COUNT = 5;

    @Test
    public void testGetListSingleThread() throws IOException {
        FtpServer server = new FtpServer(PORT);
        FtpClient client = new FtpClient(HOSTNAME, PORT);

        server.start();
        client.start();

        List<String> list = client.getList(DIR_PATH);
        Assert.assertNotNull(list);

        server.stop();
        client.stop();
    }


    @Test
    public void testGetFileSingleThread() throws IOException {
        FtpServer server = new FtpServer(PORT);
        FtpClient client = new FtpClient(HOSTNAME, PORT);

        server.start();
        client.start();

        InputStream file = Files.newInputStream(Paths.get(FILE_PATH));
        InputStream data = client.getFile(FILE_PATH);

        Assert.assertNotNull(data);
        assertArrayEquals(IOUtils.toByteArray(file), IOUtils.toByteArray(data));

        server.stop();
        client.stop();
    }

    @Test
    public void testGetListMultiThread() {
        FtpServer server = new FtpServer(PORT);

        server.start();

        List<Thread> clients = new ArrayList<>();
        for (int i = 0; i < CLIENTS_COUNT; ++i) {
            clients.add(new Thread(() -> {
                    FtpClient client = new FtpClient(HOSTNAME, PORT);
                    client.start();

                    List<String> list = null;
                    try {
                        list = client.getList(DIR_PATH);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Assert.assertNotNull(list);
                    client.stop();
                }));
        }

        clients.forEach(Thread::start);

        try {
            for (Thread thread: clients) {
                thread.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        server.stop();
    }
}
