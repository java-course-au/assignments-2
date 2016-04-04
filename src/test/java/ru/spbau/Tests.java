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
import static org.junit.Assert.fail;

/**
 * Created by rebryk on 10/03/16.
 */

public class Tests {
    public static final int PORT = 5001;

    public static final String HOSTNAME = "localhost";

    public static final String DIR_PATH = "src/test/resources/test";
    public static final String FILE_PATH = "src/test/resources/test/1.png";

    public static final int CLIENTS_COUNT = 5;

    @Test
    public void testFtpServer() throws IOException, InterruptedException {
        FtpServer server = new FtpServer(PORT);
        FtpClient client = new FtpClient(HOSTNAME, PORT);

        server.start();
        client.start();

        InputStream file = Files.newInputStream(Paths.get(FILE_PATH));
        InputStream data = client.getFile(FILE_PATH);

        Assert.assertNotNull(data);
        assertArrayEquals(IOUtils.toByteArray(file), IOUtils.toByteArray(data));


        List<String> list = client.getList(DIR_PATH);
        Assert.assertNotNull(list);
        Assert.assertEquals(list.size(), 1);
        Assert.assertEquals(list.get(0), "1.png false");

        List<Thread> clients = new ArrayList<>();
        for (int i = 0; i < CLIENTS_COUNT; ++i) {
            clients.add(new Thread(() -> {
                try {
                    FtpClient newClient = new FtpClient(HOSTNAME, PORT);
                    newClient.start();

                    List<String> newList = newClient.getList(DIR_PATH);
                    Assert.assertNotNull(newList);
                    Assert.assertEquals(newList.size(), 1);
                    Assert.assertEquals(newList.get(0), "1.png false");

                    newClient.stop();
                } catch (IOException e) {
                    fail();
                }
            }));
        }

        client.stop();
        clients.forEach(Thread::start);

        for (Thread thread: clients) {
            thread.join();
        }

        server.stop();
    }
}
