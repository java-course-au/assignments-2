package ru.spbau;

import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * Created by rebryk on 10/03/16.
 */
public class Main {
    private static final int PORT = 1024;

    public static void main(String[] args) {
        FtpServer server = new FtpServer(PORT);
        server.start();

        FtpClient client = new FtpClient("localhost", PORT);
        client.start();

        List<String> dir = client.getList("/Users/rebryk/Desktop");
        dir.forEach(System.out::println);

        ByteArrayOutputStream file = client.getFile("/Users/rebryk/Desktop/1.png");
        System.out.println("File size: " + file.size());

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        client.stop();
        server.stop();
    }
}
