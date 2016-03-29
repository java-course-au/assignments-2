package ru.spbau.mit;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class TestFTP {
    private static final String TEST_FOLDER = "src/test/resources/testFolder";
    private static final int TEST_PORT = 4444;

    @Test
    public void simpleTest() throws IOException, InterruptedException {
        FTPServer server = new FTPServer(TEST_PORT, TEST_FOLDER);
        server.start();

        FTPClient client;

        client = new FTPClient("localhost", TEST_PORT);
        assertEquals(Arrays.asList("./", "123.txt", "innerFolder/"), client.listFiles("."));
        client.close();

        client = new FTPClient("localhost", TEST_PORT);
        assertEquals(Arrays.asList("innerInnerFolder/", "234.txt"),
                client.listFiles("innerFolder/innerInnerFolder"));
        client.close();

        client = new FTPClient("localhost", TEST_PORT);
        assertArrayEquals(IOUtils.toByteArray(Files.newInputStream(Paths.get(TEST_FOLDER + "/123.txt"))),
                IOUtils.toByteArray(client.getFile("123.txt")));
        client.close();

        client = new FTPClient("localhost", TEST_PORT);
        assertArrayEquals(new byte[0],
                IOUtils.toByteArray(client.getFile("noSuchFile.txt")));
        client.close();
        server.close();
        server.join();
    }
}
