package ru.spbau.mit;

import org.junit.Test;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.assertArrayEquals;

public class ServerTest {
    private static final String PATH = "src/test/resources/a.txt";
    private static final int PORT = 8081;

    @Test
    public void testServer() throws ExecutionException, InterruptedException {
        new Thread(() -> {
            try {
                new NonBlockingServer(PATH).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        Thread.sleep(1000);

        ExecutorService service = Executors.newFixedThreadPool(5);
        ArrayList<Future<Void>> tasks = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            tasks.add(service.submit(() -> {
                Socket socket = new Socket("localhost", PORT);
                DataInputStream in = new DataInputStream(socket.getInputStream());

                byte[] dataFromFile = Files.readAllBytes(Paths.get(PATH));
                byte[] data = new byte[dataFromFile.length];
                in.readFully(data);
                assertArrayEquals(data, dataFromFile);

                return null;
            }));
        }
        for (Future<?> future : tasks) {
            future.get();
        }
    }
}
