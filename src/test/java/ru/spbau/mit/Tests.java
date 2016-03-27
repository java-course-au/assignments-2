package ru.spbau.mit;

import junit.framework.TestCase;
import org.junit.Test;

import java.io.*;
import java.net.BindException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class Tests extends TestCase {
    private static final int MAX_PORT = 20000;
    private static final int MIN_PORT = 10000;
    private static final int CNT_DIR = 4;

    private Server server;
    private Client client;

    @Override
    public void setUp() throws IOException {
        int port = 0;
        Random rnd = new Random();
        port = rnd.nextInt(MAX_PORT - MIN_PORT) + MIN_PORT;

        while (true) {
            try {
                server = new Server(port);
                server.start();
                break;
            } catch (BindException e) {
                port = rnd.nextInt(MAX_PORT - MIN_PORT) + MIN_PORT;
            }
        }

        client = new Client("localhost", port);
    }

    @Override
    public void tearDown() {
        server.stop();
        client.close();
    }

    String readFromStream(InputStream is) throws IOException {
        String val = "";
        int currentVal = is.read();
        while (currentVal != -1) {
            val += ((char) currentVal);
            currentVal = is.read();
        }
        return val;
    }

    @Test
    public void testGet() throws IOException {
        Path path = Files.createTempDirectory("FTP");
        File file = new File(path.toString() + File.separator + "tmp");

        PrintWriter writer = new PrintWriter(file);

        String fileString = "test   @";
        writer.print(fileString);

        writer.close();

        InputStream is = client.sendGetQuery(path.toString() + File.separator + "tmp");
        assertEquals(readFromStream(is), fileString);
    }

    @Test
    public void testGetEmptyFile() throws IOException {
        Path path = Files.createTempDirectory("FTP");
        File file = new File(path.toString() + File.separator + "tmp");

        InputStream is = client.sendGetQuery(path.toString() + File.separator + "tmp");
        assertEquals(readFromStream(is), "");
    }

    @Test
    public void testGetFileDoesNotExists() throws IOException {
        Path path = Files.createTempDirectory("FTP");

        InputStream is = client.sendGetQuery(path.toString() + File.separator + "tmp");
        assertEquals(readFromStream(is), "");
    }


    @Test
    public void testList() throws IOException {
        String[] filesNames = new String[]{"dir1", "dir2", "dir3", "dir4", "file1", "file2", "file3", "file4"};

        Set<Client.FileEntry> setOfFiles = new HashSet<Client.FileEntry>();

        Path path = Files.createTempDirectory("FTP");
        for (int i = 0; i < CNT_DIR; ++i) {
            (new File(path.toString() + File.separator + filesNames[i])).mkdir();
            setOfFiles.add(new Client.FileEntry(filesNames[i], true));
        }
        for (int i = CNT_DIR; i < filesNames.length; ++i) {
            (new File(path.toString() + File.separator + filesNames[i])).createNewFile();
            setOfFiles.add(new Client.FileEntry(filesNames[i], false));
        }

        ArrayList<Client.FileEntry> ls = client.sendListQuery(path.toString());
        assertEquals(ls.size(), filesNames.length);

        Set<Client.FileEntry> lsSet = new HashSet<>();
        lsSet.addAll(ls);

        assertTrue(lsSet.containsAll(setOfFiles));
        new File(path.toString()).delete();
    }

    @Test
    public void testListEmpty() throws IOException {
        Path path = Files.createTempDirectory("FTP");

        ArrayList<Client.FileEntry> ls = client.sendListQuery(path.toString());
        assertEquals(ls.size(), 0);

        new File(path.toString()).delete();
    }

    @Test
    public void testListFileDoesNotExists() throws IOException {
        Path path = Files.createTempDirectory("FTP");

        ArrayList<Client.FileEntry> ls = client.sendListQuery(path.toString());
        assertEquals(ls.size(), 0);

        new File(path.toString()).delete();
    }
}
