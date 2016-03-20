package ru.spbau.mit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class FTPServerTest {

    private static final int TEST_FILES_NUM = 4;
    private static final HashMap<String, Boolean> PATHS = new HashMap<>();
    private FTPServer server;
    private FTPClient client;

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

    @Before
    public void initServerClient() throws IOException {
        server = new FTPServer(FTPServer.SERVER_DEFAULT_PORT);
        client = new FTPClient("localhost", FTPServer.SERVER_DEFAULT_PORT);
    }

    @After
    public void deinitServerClient() throws IOException, InterruptedException {
        client.stop();
        server.tearDown();
    }

    @Test
    public void testMultipleQueries() throws Exception {
        assertEquals(0,
                (client.listRequestWrapper(FTPClient.LIST, "kdjfhgfdjshfg")).getSize());

        File resFile = client.getRequestWrapper(FTPClient.GET, "dfkjghfkjghfj");
        assertEquals(
                0,
                new DataInputStream(new FileInputStream(resFile)).readInt()
        );
        Files.delete(resFile.toPath());
    }

    @Test
    public void testNonEmptyFolderListing() throws IOException, InterruptedException {
        String dir = createTestDir();
        assertEquals(PATHS,
                (client.listRequestWrapper(FTPClient.LIST, dir)).getFilesList());
        deleteTestDir(dir);
    }

    @Test
    public void testGetFileContent() throws IOException, InterruptedException {
        String dir = createTestDir();
        for (Map.Entry<String, Boolean> entry : PATHS.entrySet()) {
            File resFile = client.getRequestWrapper(FTPClient.GET, entry.getKey());
            DataInputStream input = new DataInputStream(new FileInputStream(resFile));
            input.readInt();
            byte[] response = new byte[1];
            input.read(response);
            assertEquals((byte) 0, response[0]);
            Files.delete(resFile.toPath());
        }
        deleteTestDir(dir);
    }

    @Test
    public void testTearDown() throws Exception {
    }
}
