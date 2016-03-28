package ru.spbau.mit;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;

import static org.junit.Assert.*;

public class SimpleFtpServerTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testMultipleClients() throws IOException, InterruptedException {
        final byte[] fooContents = "Contents of foo".getBytes();
        Files.write(temporaryFolder.newFile("foo").toPath(), fooContents);

        final int port = 12345;
        SimpleFtpServer server = new SimpleFtpServer(port, temporaryFolder.getRoot().toPath());
        server.start();
        try {
            Socket s0 = new Socket("localhost", port);
            Socket s1 = new Socket("localhost", port);
            Socket s2 = new Socket("localhost", port);

            DataOutputStream out0 = new DataOutputStream(s0.getOutputStream());
            DataOutputStream out1 = new DataOutputStream(s1.getOutputStream());
            DataOutputStream out2 = new DataOutputStream(s2.getOutputStream());

            out0.writeInt(-1);

            out1.writeInt(1);
            out1.writeUTF("");

            out2.writeInt(2);
            out2.writeUTF("foo");

            DataInputStream in1 = new DataInputStream(s1.getInputStream());
            DataInputStream in2 = new DataInputStream(s2.getInputStream());
            assertEquals(1, in1.readInt());
            assertEquals("foo", in1.readUTF());
            assertEquals(false, in1.readBoolean());

            assertEquals(fooContents.length, in2.readInt());
            byte[] received = new byte[fooContents.length];
            assertEquals(fooContents.length, in2.read(received));
            assertArrayEquals(fooContents, received);

            s1.shutdownInput();
            s1.shutdownOutput();
            s1.close();
            s2.shutdownInput();
            s2.shutdownOutput();
            s2.close();
        } finally {
            server.shutdown();
        }
    }
}
