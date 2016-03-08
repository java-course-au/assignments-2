package ru.spbau.mit;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Created by ldvsoft on 08.03.16.
 */
public class FTPServer implements AutoCloseable {
    private static final int THREADS = 4;

    private ServerSocket serverSocket;
    private ExecutorService executor;
    private volatile boolean toWork = true;

    public FTPServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        executor = Executors.newFixedThreadPool(THREADS);
        for (int i = 0; i != THREADS; i++) {
            executor.execute(this::work);
        }
    }

    public void close() {
        if (!toWork) {
            return;
        }

        synchronized (this) {
            toWork = false;
            try {
                serverSocket.close();
            } catch (IOException ignored) {
            }
        }
        executor.shutdown();
    }

    private Socket accept() throws IOException {
        synchronized (this) {
            if (!toWork) {
                return null;
            }
        }
        return serverSocket.accept();
    }

    private void work() {
        while (true) {
            try (Socket socket = accept()) {
                if (socket == null) {
                    return;
                }

                try (FTPConnection connection = new FTPConnection(socket)) {
                    String path;
                    int action = connection.readAction();
                    switch (action) {
                        case FTPConnection.FTP_ACTION_LIST:
                            path = connection.readActionList();
                            doList(path, connection);
                            break;
                        case FTPConnection.FTP_ACTION_GET:
                            path = connection.readActionGet();
                            doGet(path, connection);
                        default:
                            System.err.printf("Wrong action from client: %d\n", action);
                            break;
                    }
                }
            } catch (IOException ignored) {
            }
        }
    }

    private void doList(String stringPath, FTPConnection connection) throws IOException {
        Path path = Paths.get(stringPath);
        List<FTPFileEntry> contents;
        try {
            contents = Files
                    .list(path)
                    .map(p -> new FTPFileEntry(p.toString(), Files.isDirectory(p)))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            contents = Collections.emptyList();
        }
        connection.writeList(contents);
    }

    private void doGet(String stringPath, FTPConnection connection) throws IOException {
        Path path = Paths.get(stringPath);
        if (!Files.exists(path)) {
            connection.writeGet(0, null);
            return;
        }
        connection.writeGet(Files.size(path), Files.newInputStream(path));
    }
}
