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
    private ServerSocket serverSocket;
    private ExecutorService executorService;

    public FTPServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        executorService = Executors.newCachedThreadPool();
        executorService.submit((Runnable) this::work);
    }

    public synchronized void close() {
        try {
            serverSocket.close();
        } catch (IOException ignored) {
        }
    }

    private Socket accept() throws IOException {
        synchronized (this) {
            if (serverSocket.isClosed()) {
                return null;
            }
        }
        return serverSocket.accept();
    }

    private void work() {
        while (true) {
            try {
                Socket socket = accept();
                if (socket == null) {
                    return;
                }
                executorService.submit(() -> handleConnection(socket));
            } catch (IOException e) {
                break;
            }
        }
    }

    private void handleConnection(Socket socket) {
        try (FTPConnection connection = new FTPConnection(socket)) {
            int request = connection.readRequest();
            switch (request) {
                case FTPConnection.FTP_REQUEST_LIST:
                    doList(connection);
                    break;
                case FTPConnection.FTP_REQUEST_GET:
                    doGet(connection);
                    break;
                default:
                    System.err.printf("Wrong request from client: %d\n", request);
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void doList(FTPConnection connection) throws IOException {
        String stringPath = connection.readListRequest();
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
        connection.writeListResponse(contents);
    }

    private void doGet(FTPConnection connection) throws IOException {
        String stringPath = connection.readGetRequest();
        Path path = Paths.get(stringPath);
        if (!Files.exists(path)) {
            connection.writeGetResponse(0, null);
            return;
        }
        connection.writeGetResponse(Files.size(path), Files.newInputStream(path));
    }
}
