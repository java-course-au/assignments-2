package ru.spbau.mit;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.*;
import java.net.Socket;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.*;

import static org.junit.Assert.assertEquals;

public class FileServingServerTest {
    private static final int DATA_SIZE = 1024 * 1024;
    private static final int SERVER_PORT = 12345;

    private static final byte[] DATA = new byte[DATA_SIZE];

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    private Path dataFile;

    static {
        final long randomSeed = 123456789123456L;
        new Random(randomSeed).nextBytes(DATA);
    }

    @Before
    public void createDataFile() throws IOException {
        File f = folder.newFile();
        dataFile = f.toPath();
        try (FileOutputStream s = new FileOutputStream(f)) {
            s.write(DATA);
        }
    }

    @Test
    public void testServerStartStop() throws IOException, InterruptedException {
        FileServingServer server = new FileServingServer(SERVER_PORT, dataFile);
        Thread serverThread = new Thread(server);
        serverThread.start();
        serverThread.interrupt();
        serverThread.join();
    }

    @Test
    public void testServer() throws IOException, ExecutionException, InterruptedException {
        FileServingServer server = new FileServingServer(SERVER_PORT, dataFile);
        Thread serverThread = new Thread(server);
        serverThread.start();

        try {
            final int consecutiveRuns = 5;
            final int parallelRuns = 5;
            for (int run = 0; run < consecutiveRuns; run++) {
                ExecutorService service = Executors.newFixedThreadPool(parallelRuns);
                ArrayList<Future<Void>> tasks = new ArrayList<>();
                for (int runp = 0; runp < parallelRuns; runp++) {
                    tasks.add(service.submit(new ConnectAndReadTask()));
                }
                for (Future<?> task : tasks) {
                    task.get();
                }
            }
        } finally {
            serverThread.interrupt();
            serverThread.join();
        }
    }

    private class ConnectAndReadTask implements Callable<Void> {
        @Override
        public Void call() throws Exception {
            try (Socket s = new Socket("localhost", SERVER_PORT);
                 InputStream in = s.getInputStream()) {
                for (int i = 0; i < DATA_SIZE; i++) {
                    assertEquals(Byte.toUnsignedInt(DATA[i]), in.read());
                }
                assertEquals(-1, in.read());
            }
            return null;
        }
    }
}
