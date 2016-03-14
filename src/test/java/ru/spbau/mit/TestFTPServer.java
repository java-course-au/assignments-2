package ru.spbau.mit;

import org.junit.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TestFTPServer {
    private static final int PORT = 12345;
    private static final String ROOT_DIRECTORY = "src/test/resources/test/";
    private static final String TEST_DIRECTORY = "folder";
    private static final String TEST_FAKE_DIRECTORY = "fakeFolder";
    private static final String TEST_DOCUMENT = "folder/text1.txt";
    private static final String TEST_DOCUMENT2 = "folder/document2.txt";
    private static final String DOCUMENT_TO_COPY = "document2copy.txt";

    private static final List<FileInfo> EXPECTED_FILE_LIST = Arrays.asList(
            new FileInfo("document1.txt", false),
            new FileInfo("document2.txt", false),
            new FileInfo("folder1", true),
            new FileInfo("text1.txt", false));

    @Test
    public void testGetRequest() {
        Server server = new FTPServer(PORT, ROOT_DIRECTORY);
        server.start();
        Client client = new FTPClient();
        client.connect("localhost", PORT);

        assertEquals(client.executeList(TEST_DIRECTORY), EXPECTED_FILE_LIST);
        assertEquals(client.executeList(TEST_FAKE_DIRECTORY), new ArrayList<>());
        assertEquals(client.executeList(TEST_DOCUMENT), new ArrayList<>());

        OutputStream outputStream = null;
        Path pathToCopy = Paths.get(ROOT_DIRECTORY + DOCUMENT_TO_COPY);
        try {
            outputStream = Files.newOutputStream(pathToCopy);
        } catch (IOException e) {
            e.printStackTrace();
        }

        client.executeGet(TEST_DOCUMENT2, outputStream);
        try {
            assertEquals(Files.readAllLines(Paths.get(ROOT_DIRECTORY + TEST_DOCUMENT2)),
                    Files.readAllLines(pathToCopy));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Files.delete(pathToCopy);
        } catch (IOException e) {
            e.printStackTrace();
        }
        client.disconnect();
        server.stop();
    }

    @Test
    public void testListRequest() {

    }
}
