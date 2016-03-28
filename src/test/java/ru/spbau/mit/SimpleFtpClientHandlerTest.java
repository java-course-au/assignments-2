package ru.spbau.mit;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.*;
import java.nio.file.Files;

public class SimpleFtpClientHandlerTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private void runTest(
            ThrowingConsumer<DataOutputStream, IOException> commandsWriter,
            ThrowingConsumer<DataOutputStream, IOException> expectedAnswersWriter
            ) throws IOException {
        SocketTestUtils.checkSocketIO(
                commandsWriter,
                socket -> {
                    new SimpleFtpClientHandler(temporaryFolder.getRoot().toPath(), socket).run();
                },
                expectedAnswersWriter
        );
    }

    @Before
    public void createDirectory() throws IOException {
        temporaryFolder.newFolder("afolder");
        Files.write(temporaryFolder.newFile("test1.txt").toPath(), "Contents of test1".getBytes());
        Files.write(temporaryFolder.newFile("test2.txt").toPath(), "Contents of test2".getBytes());
        temporaryFolder.newFolder("zfolder");
        Files.write(temporaryFolder.newFile("zfolder/test3.txt").toPath(), "Contents of test3".getBytes());
    }

    @Test
    public void testBadCommand() throws IOException {
        // CHECKSTYLE.OFF: MagicNumber
        runTest(commands -> commands.writeInt(3), expected -> { });
        // CHECKSTYLE.ON: MagicNumber
    }

    @Test
    public void testDirectoryList() throws IOException {
        runTest(commands -> {
            commands.writeInt(1);
            commands.writeUTF(".");
        }, expected -> {
            // CHECKSTYLE.OFF: MagicNumber
            expected.writeInt(4);
            // CHECKSTYLE.ON: MagicNumber
            expected.writeUTF("afolder");
            expected.writeBoolean(true);
            expected.writeUTF("test1.txt");
            expected.writeBoolean(false);
            expected.writeUTF("test2.txt");
            expected.writeBoolean(false);
            expected.writeUTF("zfolder");
            expected.writeBoolean(true);
        });
    }

    @Test
    public void testSubdirectoryList() throws IOException {
        runTest(commands -> {
            commands.writeInt(1);
            commands.writeUTF("afolder/../zfolder");
        }, expected -> {
            expected.writeInt(1);
            expected.writeUTF("test3.txt");
            expected.writeBoolean(false);
        });
    }

    @Test
    public void testFileContents() throws IOException {
        runTest(commands -> {
            commands.writeInt(2);
            commands.writeUTF("zfolder/test3.txt");
        }, expected -> {
            final byte[] expectedStr = "Contents of test3".getBytes();
            expected.writeInt(expectedStr.length);
            expected.write(expectedStr);
        });
    }

    @Test
    public void testListNonExistingDirectory() throws IOException {
        runTest(commands -> {
            commands.writeInt(1);
            commands.writeUTF("badfolder");
        }, expected -> {
            expected.writeInt(0);
        });
    }

    @Test
    public void testListFile() throws IOException {
        runTest(commands -> {
            commands.writeInt(1);
            commands.writeUTF("test1.txt");
        }, expected -> {
            expected.writeInt(0);
        });
    }

    @Test
    public void testGetNonExistingFile() throws IOException {
        runTest(commands -> {
            commands.writeInt(2);
            commands.writeUTF("badfile");
        }, expected -> {
            expected.writeInt(0);
        });
    }

    @Test
    public void testGetDirectory() throws IOException {
        runTest(commands -> {
            commands.writeInt(2);
            commands.writeUTF("zfolder");
        }, expected -> {
            expected.writeInt(0);
        });
    }
}
