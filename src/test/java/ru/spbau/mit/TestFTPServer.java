package ru.spbau.mit;

import org.apache.commons.io.IOUtils;
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
    private static final int CLIENTS_NUMBER = 3;
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

    private Server initializeServer() {
        Server server = new FTPServer(PORT, ROOT_DIRECTORY);
        server.start();
        return server;
    }

    private Client initializeClient(int port) {
        Client client = new FTPClient();
        client.connect("localhost", port);
        return client;
    }

    @Test
    public void testGetRequest() {
        Server server = initializeServer();
        Client client = initializeClient(PORT);

        try {
            Path pathToCopy = Paths.get(ROOT_DIRECTORY + DOCUMENT_TO_COPY);
            OutputStream outputStream = Files.newOutputStream(pathToCopy);
            IOUtils.copy(client.executeGet(TEST_DOCUMENT2, outputStream), outputStream);
            assertEquals(Files.readAllLines(Paths.get(ROOT_DIRECTORY + TEST_DOCUMENT2)),
                    Files.readAllLines(pathToCopy));
            Files.delete(pathToCopy);
        } catch (IOException e) {
            e.printStackTrace();
        }

        client.disconnect();
        server.stop();
    }

    @Test
    public void testListRequest() {
        Server server = initializeServer();

        List<Client> clients = new ArrayList<>();
        for (int i = 0; i < CLIENTS_NUMBER; i++) {
            clients.add(initializeClient(PORT));
        }
        assertEquals(clients.get(0).executeList(TEST_DIRECTORY), EXPECTED_FILE_LIST);
        assertEquals(clients.get(1).executeList(TEST_FAKE_DIRECTORY), new ArrayList<>());
        assertEquals(clients.get(2).executeList(TEST_DOCUMENT), new ArrayList<>());
        for (int i = 0; i < CLIENTS_NUMBER; i++) {
            clients.get(i).disconnect();
        }

        server.stop();
    }
}
