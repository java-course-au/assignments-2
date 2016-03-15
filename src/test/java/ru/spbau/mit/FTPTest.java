package ru.spbau.mit;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

public class FTPTest {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 17239;
    private static final int SECOND = 1000;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private File createFileWithContents(String fileName, String content) throws IOException {
        File file = folder.newFile(fileName);
        PrintWriter printWriter = new PrintWriter(new FileWriter(file));
        printWriter.print(content);
        printWriter.close();
        return file;
    }

    @Test
    public void testOperations() throws IOException, InterruptedException {
        File file = createFileWithContents("file.txt", "test\ncontent");
        folder.newFolder("folder");

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    FTPServer ftpServer = new FTPServer(SERVER_PORT);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();

        Thread.sleep(SECOND);

        FTPClient ftpClient = new FTPClient(SERVER_HOST, SERVER_PORT);

        List<FTPClient.FileEntry> list = ftpClient.listOperation(folder.getRoot().getAbsolutePath());
        List<FTPClient.FileEntry> expected = Arrays.asList(
                new FTPClient.FileEntry("folder", true),
                new FTPClient.FileEntry("file.txt", false)
        );
        Assert.assertTrue(Arrays.deepEquals(expected.stream().sorted().toArray(),
                list.stream().sorted().toArray()));

        Assert.assertArrayEquals(
                "test\ncontent".getBytes(),
                ftpClient.getOperation(file.getPath())
        );
        ftpClient.closeConnection();
    }

    @Test
    public void testNoSuchPathes() throws IOException, InterruptedException {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    FTPServer ftpServer = new FTPServer(SERVER_PORT);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();

        Thread.sleep(SECOND);

        FTPClient ftpClient = new FTPClient(SERVER_HOST, SERVER_PORT);

        List<FTPClient.FileEntry> list = ftpClient.listOperation("No/Such/Path");
        Assert.assertTrue(list.isEmpty());

        Assert.assertArrayEquals(
                "".getBytes(),
                ftpClient.getOperation("No/Such/Path.txt")
        );

        ftpClient.closeConnection();
    }
}
