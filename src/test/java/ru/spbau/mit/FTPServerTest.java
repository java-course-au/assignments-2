package ru.spbau.mit;

import com.sun.org.apache.xerces.internal.impl.dv.xs.BooleanDV;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.apache.commons.*;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class FTPServerTest {

    private static final String BASE_FOLDER_NAME = "testFolder";
    private static final String TEST_FOLDER_PREF = "./" + BASE_FOLDER_NAME + "/" + BASE_FOLDER_NAME;
    private static final String TEST_FILE_PREF = "testFile";
    private static final int TEST_FILES_NUM = 4;
    private static final String[] TEST_TEXT = {
        "But",  "there's", "no", "sense", "crying",
        "over", "every", "mistake.",
        "You", "just", "keep on", "trying",
        "till", "you", "run out of", "cake"
    };
    private static final HashMap<String, Boolean> PATHS = new HashMap<>();

    private static String createTestDir () throws IOException {
        PATHS.clear();

        Path dir;
        try {
            dir = Files.createTempDirectory("TEST_DIR");
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        //PATHS.put(dir.toString(), true);
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

    @Test
    public void testForFun() throws IOException {
        Socket clientSocket = new Socket("vk.com", 80);
        DataInputStream input = new DataInputStream(clientSocket.getInputStream());
        DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());
        output.writeChars("GET\n\n");
        clientSocket.close();
    }

    @Test
    public void testMultipleQueries() throws Exception {
        FTPServer server = new FTPServer(FTPServer.SERVER_DEFAULT_PORT);
        FTPClient client = new FTPClient("localhost", FTPServer.SERVER_DEFAULT_PORT);

        assertEquals(0,
                ((FTPClient.ListResponse)client.makeRequest(FTPClient.LIST, "kdjfhgfdjshfg", null)).size);

        Path tmp = Files.createTempFile("get_response", "");
        File resFile = tmp.toFile();
        client.makeRequest(FTPClient.GET, "dfkjghfkjghfj", resFile);
        assertEquals(
                0,
                new DataInputStream(new FileInputStream(resFile)).readInt()
        );

        client.stop();
        server.tearDown();
    }

    @Test
    public void testNonEmptyFolderListing () throws IOException, InterruptedException {
        FTPServer server = new FTPServer(FTPServer.SERVER_DEFAULT_PORT);
        FTPClient client = new FTPClient("localhost", FTPServer.SERVER_DEFAULT_PORT);

        assertEquals(PATHS,
                ((FTPClient.ListResponse)client.makeRequest(FTPClient.LIST, createTestDir(), null)).filesList);
        client.stop();
        server.tearDown();
    }

    @Test
    public void testGetFileContent () throws IOException, InterruptedException {
        FTPServer server = new FTPServer(FTPServer.SERVER_DEFAULT_PORT);
        FTPClient client = new FTPClient("localhost", FTPServer.SERVER_DEFAULT_PORT);
        createTestDir();
        for(Map.Entry<String, Boolean> entry: PATHS.entrySet()) {
            File resFile = Files.createTempFile("get_response", "").toFile();
            client.makeRequest(FTPClient.GET, entry.getKey(), resFile);
            DataInputStream input = new DataInputStream(new FileInputStream(resFile));
            input.readInt();
            byte[] response = new byte[1];
            input.read(response);
            assertEquals((byte)0, response[0]);
        }
        client.stop();
        server.tearDown();
    }

    @Test
    public void testTearDown() throws Exception {
        FTPServer server = new FTPServer(FTPServer.SERVER_DEFAULT_PORT);
        //server.setUp();
        server.tearDown();
    }

    private void checkSendingIncorrectSruff(int i, DataOutputStream output) throws IOException {
        output.writeInt(i + 1);
        output.writeUTF("dksjfhgw;234738dsfhb   888***");
        output.flush();
    }
}