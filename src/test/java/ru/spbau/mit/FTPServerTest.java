package ru.spbau.mit;

import org.junit.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class FTPServerTest {

    private static final int TEST_FILES_NUM = 4;
    private static final HashMap<String, Boolean> PATHS = new HashMap<>();

    private static String createTestDir() throws IOException {
        PATHS.clear();

        Path dir;
        try {
            dir = Files.createTempDirectory("TEST_DIR");
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        for (int i = 0; i < TEST_FILES_NUM; i++) {
            Path file;
            try {
                file = Files.createTempFile(dir, Integer.toString(i), "");
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
            byte[] a = {0};
            Files.write(file, a);
            PATHS.put(file.toString(), false);
        }
        return dir.toString();
    }

    private static void deleteTestDir(String path) throws IOException {
        File cur = new File(path);
        if (cur.isDirectory()) {
            for (File file : cur.listFiles()) {
                deleteTestDir(file.getPath());
            }
        }
        Files.delete(Paths.get(path));
    }

    @Test
    public void testMultipleQueries() throws Exception {
        FTPServer server = new FTPServer(FTPServer.SERVER_DEFAULT_PORT);
        FTPClient client = new FTPClient("localhost", FTPServer.SERVER_DEFAULT_PORT);

        assertEquals(0,
                ((FTPClient.ListResponse) client.makeRequest(FTPClient.LIST, "kdjfhgfdjshfg", null)).getSize());

        Path tmp = Files.createTempFile("get_response", "");
        File resFile = tmp.toFile();
        client.makeRequest(FTPClient.GET, "dfkjghfkjghfj", resFile);
        assertEquals(
                0,
                new DataInputStream(new FileInputStream(resFile)).readInt()
        );
        Files.delete(tmp);

        client.stop();
        server.tearDown();
    }

    @Test
    public void testNonEmptyFolderListing() throws IOException, InterruptedException {
        FTPServer server = new FTPServer(FTPServer.SERVER_DEFAULT_PORT);
        FTPClient client = new FTPClient("localhost", FTPServer.SERVER_DEFAULT_PORT);
        String dir = createTestDir();
        assertEquals(PATHS,
                ((FTPClient.ListResponse) client.makeRequest(FTPClient.LIST, dir, null)).getFilesList());
        deleteTestDir(dir);
        client.stop();
        server.tearDown();
    }

    @Test
    public void testGetFileContent() throws IOException, InterruptedException {
        FTPServer server = new FTPServer(FTPServer.SERVER_DEFAULT_PORT);
        FTPClient client = new FTPClient("localhost", FTPServer.SERVER_DEFAULT_PORT);
        String dir = createTestDir();
        for (Map.Entry<String, Boolean> entry : PATHS.entrySet()) {
            File resFile = Files.createTempFile("get_response", "").toFile();
            client.makeRequest(FTPClient.GET, entry.getKey(), resFile);
            DataInputStream input = new DataInputStream(new FileInputStream(resFile));
            input.readInt();
            byte[] response = new byte[1];
            input.read(response);
            assertEquals((byte) 0, response[0]);
            Files.delete(resFile.toPath());
        }
        deleteTestDir(dir);
        client.stop();
        server.tearDown();
    }

    @Test
    public void testTearDown() throws Exception {
        FTPServer server = new FTPServer(FTPServer.SERVER_DEFAULT_PORT);
        server.tearDown();
    }
}
