package ru.spbau.mit;

import org.junit.*;
import org.junit.rules.TemporaryFolder;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

public class FTPTest {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 17239;
    private static final int DELAY_SECONDS = 1;
    private static final String FILE_CONTENT = "test\ncontent";
    private static final int CLIENTS_COUNT = 5;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private FTPServer ftpServer;

    @Before
    public void setUp() throws InterruptedException, IOException {
        ftpServer = runServer();
    }

    @After
    public void tearDown() throws IOException, InterruptedException {
        ftpServer.stop();
        TimeUnit.SECONDS.sleep(DELAY_SECONDS);
    }

    @Test
    public void testFilesList() throws InterruptedException, IOException {
        FTPClient ftpClient = runClient();
        createFileWithContents("file.txt", FILE_CONTENT);
        folder.newFolder("folder");

        List<FTPClient.FileEntry> list = ftpClient.getFilesList(folder.getRoot().getAbsolutePath());
        List<FTPClient.FileEntry> expected = Arrays.asList(
                new FTPClient.FileEntry("folder", true),
                new FTPClient.FileEntry("file.txt", false)
        );
        Assert.assertTrue(Arrays.deepEquals(
                expected.stream().sorted().toArray(),
                list.stream().sorted().toArray()));
        ftpClient.stop();
    }

    @Test
    public void testFileContents() throws InterruptedException, IOException {
        FTPClient ftpClient = runClient();
        File file = createFileWithContents("file.txt", FILE_CONTENT);
        folder.newFolder("folder");

        byte[] fileBytes = new byte[FILE_CONTENT.length()];
        InputStream fileStream = ftpClient.getFileStream(file.getPath());
        Assert.assertEquals(FILE_CONTENT.length(), fileStream.read(fileBytes));
        Assert.assertArrayEquals(FILE_CONTENT.getBytes(), fileBytes);
        Assert.assertEquals(0, fileStream.available());
        ftpClient.stop();
    }

    @Test
    public void testNoSuchDir() throws InterruptedException, IOException {
        FTPClient ftpClient = runClient();
        Assert.assertTrue(ftpClient.getFilesList("No/Such/Path").isEmpty());
        ftpClient.stop();
    }

    @Test
    public void testNoSuchFile() throws InterruptedException, IOException {
        FTPClient ftpClient = runClient();
        Assert.assertEquals(0, ftpClient.getFileStream("No/Such/Path.txt").available());
        ftpClient.stop();
    }

    @Test
    public void testNClients() throws InterruptedException, IOException, ExecutionException {
        ExecutorService executorService = Executors.newCachedThreadPool();
        ArrayList<Future<Void>> results = new ArrayList<>();
        for (int i = 0; i < CLIENTS_COUNT; i++) {
            results.add(executorService.submit(() -> {
                FTPClient ftpClient = runClient();
                Assert.assertEquals(0, ftpClient.getFileStream("No/Such/Path.txt").available());
                ftpClient.stop();
                return null;
            }));
        }
        for (Future result : results) {
            result.get();
        }
    }

    private File createFileWithContents(String fileName, String contents) throws IOException {
        File file = folder.newFile(fileName);
        PrintWriter printWriter = new PrintWriter(new FileWriter(file));
        printWriter.print(contents);
        printWriter.close();
        return file;
    }

    private FTPServer runServer() throws InterruptedException {
        FTPServer ftpServer = new FTPServer(SERVER_PORT);
        new Thread(ftpServer::run).start();
        TimeUnit.SECONDS.sleep(DELAY_SECONDS);
        return ftpServer;
    }

    private FTPClient runClient() throws IOException {
        FTPClient ftpClient = new FTPClient(SERVER_HOST, SERVER_PORT);
        ftpClient.run();
        return ftpClient;
    }
}
