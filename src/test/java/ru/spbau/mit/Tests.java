package ru.spbau.mit;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.BindException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;

import static org.junit.Assert.assertEquals;

public class Tests {
    private static final int MAX_PORT = 20000;
    private static final int MIN_PORT = 10000;
    private static final int CNT_DIR = 4;

    @Test
    public void testGet() throws IOException {
        Path path = Files.createTempDirectory("FTP");
        File file = new File(path.toString() + File.separator + "tmp");

        PrintWriter writer = new PrintWriter(file);

        String fileString = "test   @";
        writer.print(fileString);

        writer.close();

        OutputStream output = new OutputStream() {
            private StringBuilder string = new StringBuilder();

            @Override
            public void write(int b) throws IOException {
                this.string.append((char) b);
            }

            public String toString() {
                return this.string.toString();
            }
        };

        int port = 0;
        Random rnd = new Random();
        port = rnd.nextInt(MAX_PORT - MIN_PORT) + MIN_PORT;

        Server server;
        while (true) {
            try {
                server = new Server(port);
                break;
            } catch (BindException e) {
                port = rnd.nextInt(MAX_PORT - MIN_PORT) + MIN_PORT;
            }
        }

        server.start();

        Client client = new Client("localhost", port);

        try {
            assertEquals(client.get(path.toString() + File.separator + "tmp", output), fileString.length());

            assertEquals(output.toString(), fileString);
        } finally {
            server.stop();
            client.close();
        }
    }

    @Test
    public void testList() throws IOException {
        String[] fileName = new String[]{"dir1", "dir2", "dir3", "dir4", "file1", "file2", "file3", "file4"};

        Path path = Files.createTempDirectory("FTP");
        for (int i = 0; i < CNT_DIR; ++i) {
            (new File(path.toString() + File.separator + fileName[i])).mkdir();
        }
        for (int i = CNT_DIR; i < fileName.length; ++i) {
            (new File(path.toString() + File.separator + fileName[i])).createNewFile();
        }

        int port = 0;
        Random rnd = new Random();
        port = rnd.nextInt(MAX_PORT - MIN_PORT) + MIN_PORT;

        Server server;
        while (true) {
            try {
                server = new Server(port);
                break;
            } catch (BindException e) {
                port = rnd.nextInt(MAX_PORT - MIN_PORT) + MIN_PORT;
            }
        }

        server.start();

        Client client = new Client("localhost", port);

        try {
            ArrayList<Client.FileEntry> ls = client.list(path.toString());
            assertEquals(ls.size(), fileName.length);
            ls.sort(new Comparator<Client.FileEntry>() {
                @Override
                public int compare(Client.FileEntry o1, Client.FileEntry o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            });

            for (int i = 0; i < CNT_DIR; ++i) {
                assertEquals(ls.get(i).getDir(), true);
            }


            for (int i = CNT_DIR; i < fileName.length; ++i) {
                assertEquals(ls.get(i).getDir(), false);
            }

            for (int i = 0; i < fileName.length; ++i) {
                assertEquals(ls.get(i).getName(), fileName[i]);
            }

            new File(path.toString()).delete();
        } finally {
            server.stop();
            client.close();
        }
    }
}
