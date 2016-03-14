package ru.spbau;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadFactory;

/**
 * Created by rebryk on 10/03/16.
 */
public class Tests {
    public static final int PORT = 1024;
    public static final String HOSTNAME = "localhost";

    public static final String DIR_PATH = "/Users/rebryk/Desktop/";
    public static final String FILE_PATH = "/Users/rebryk/Desktop/1.png";

    public static final int CLIENTS_COUNT = 5;
    @Test
    public void testGetListSingleThread() {
        FtpServer server = new FtpServer(PORT);
        FtpClient client = new FtpClient(HOSTNAME, PORT);

        server.start();
        client.start();

        List<String> list = client.getList(DIR_PATH);
        Assert.assertNotNull(list);

        list = client.getList(FILE_PATH);
        Assert.assertNull(list);

        server.stop();
        client.stop();
    }

    @Test
    public void testGetFileSingleThread() {
        FtpServer server = new FtpServer(PORT);
        FtpClient client = new FtpClient(HOSTNAME, PORT);

        server.start();
        client.start();

        ByteArrayOutputStream data = client.getFile(DIR_PATH);
        Assert.assertNull(data);

        File file = new File(FILE_PATH);
        data = client.getFile(FILE_PATH);
        Assert.assertNotNull(data);
        Assert.assertEquals((long) data.size(), file.length());

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

                    List<String> list = client.getList(DIR_PATH);
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
