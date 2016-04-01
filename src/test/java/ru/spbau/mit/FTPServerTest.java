package ru.spbau.mit;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class FTPServerTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private static final int TEST_FILES_NUM = 4;
    private FTPServer server;
    private FTPClient client;

    private void fillTempDir(HashMap<String, Boolean> paths) throws IOException {
        for (int i = 0; i < TEST_FILES_NUM; i++) {
            File file;
            try {
                file = folder.newFile();
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
            byte[] a = {0};
            Files.write(file.toPath(), a);
            paths.put(file.getPath(), false);
        }
    }

    @Before
    public void initServerClient() throws IOException {
        server = new FTPServer(FTPServer.SERVER_DEFAULT_PORT);
        server.startUp();
        client = new FTPClient("localhost", FTPServer.SERVER_DEFAULT_PORT);
    }

    @After
    public void deinitServerClient() throws IOException, InterruptedException {
        client.stop();
        server.tearDown();
    }

    @Test
    public void testListRequest() throws Exception {
        assertEquals(0,
                (client.wrapListRequest("kdjfhgfdjshfg")).getSize());
    }

    @Test
    public void testGetRequest() throws Exception {
        assertEquals(
                0,
                client.wrapGetRequest("dfkjghfkjghfj").readInt()
        );
    }

    @Test
    public void testNonEmptyFolderListing() throws IOException, InterruptedException {
        HashMap<String, Boolean> paths = new HashMap<>();
        fillTempDir(paths);
        assertEquals(paths,
                (client.wrapListRequest(folder.getRoot().getPath())).getFilesList());
    }

    @Test
    public void testGetFileContent() throws IOException, InterruptedException {
        HashMap<String, Boolean> paths = new HashMap<>();
        fillTempDir(paths);
        Set<Map.Entry<String, Boolean>> entries = paths.entrySet();
        if (entries.size() > 0) {
            DataInputStream input = client.wrapGetRequest(entries.iterator().next().getKey());
            input.readInt();
            byte[] response = new byte[1];
            input.read(response);
            assertEquals((byte) 0, response[0]);
        }
    }

    @Test
    public void testTearDown() throws Exception {
    }
}
