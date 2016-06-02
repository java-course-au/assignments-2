package ru.spbau.mit;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.*;
import java.net.Socket;

import static org.junit.Assert.assertEquals;


public class NonBlockingServerTest {


    private static final String TEXT = "Still Alive";
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private File file;
    private NonBlockingServer server;

    @Before
    public void setUp() throws IOException {
        file = folder.newFile();
        DataOutputStream output = new DataOutputStream(new FileOutputStream(file));
        output.writeUTF(TEXT);
        server = new NonBlockingServer(file.toPath());
        server.start();
    }

    @After
    public void tearDown() throws IOException, InterruptedException {
        server.stop();
    }

    @Test
    public void testIdle() throws Exception {
    }

    @Test
    public void testLocalDownload() throws Exception {
        Socket client = new Socket("localhost", NonBlockingServer.PORT);
        DataInputStream input = new DataInputStream(client.getInputStream());

        String response = input.readUTF();
        assertEquals(response, TEXT);
    }
}
