package ru.spbau.mit;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class SimpleFtpClientTest {
    @Test
    public void testList() throws IOException {
        SocketTestUtils.checkSocketIO(answerWith -> {
            // CHECKSTYLE.OFF: MagicNumber
            answerWith.writeInt(3);
            // CHECKSTYLE.ON: MagicNumber
            answerWith.writeUTF("file2");
            answerWith.writeBoolean(false);
            answerWith.writeUTF("dir");
            answerWith.writeBoolean(true);
            answerWith.writeUTF("file3");
            answerWith.writeBoolean(false);
        }, socket -> {
            try (SimpleFtpClient client = new SimpleFtpClient(socket)) {
                assertArrayEquals(new DirectoryItem[]{
                        new DirectoryItem("file2", false),
                        new DirectoryItem("dir", true),
                        new DirectoryItem("file3", false)
                }, client.list("some-dir"));
            }
        }, expectedCommand -> {
            expectedCommand.writeInt(1);
            expectedCommand.writeUTF("some-dir");
        });
    }

    @Test
    public void testGet() throws IOException {
        // CHECKSTYLE.OFF: MagicNumber
        final byte[] contents = new byte[1024 * 1024 + 100];
        // CHECKSTYLE.ON: MagicNumber
        for (int i = 0; i < contents.length; i++) {
            contents[i] = (byte) (i * i * i);
        }
        SocketTestUtils.checkSocketIO(answerWith -> {
            answerWith.writeInt(contents.length);
            answerWith.write(contents, 0, contents.length);
        }, socket -> {
            try (SimpleFtpClient client = new SimpleFtpClient(socket)) {
                assertArrayEquals(contents, IOUtils.toByteArray(client.get("some-file")));
            }
        }, expectedCommand -> {
            expectedCommand.writeInt(2);
            expectedCommand.writeUTF("some-file");
        });
    }
}
