package ru.spbau.mit;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by Tehnar on 06.03.2016.
 */
public class FTPServer implements Closeable {
    private ServerSocket serverSocket = null;
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final int port;
    private final String rootPath;

    private static final int LIST_QUERY = 1;
    private static final int GET_QUERY = 2;
    private static final int CHUNK_SIZE = 4096;

    private final List<Socket> connectedClients = new ArrayList<>();

    public FTPServer(int port, String rootPath) {
        this.rootPath = rootPath;
        this.port = port;
    }

    public void start() {
        executorService.submit(new ClientAcceptor());
    }

    public void join() throws InterruptedException {
        executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    }

    @Override
    public void close() throws IOException {
        if (serverSocket != null) {
            serverSocket.close();
            executorService.shutdown();
            synchronized (connectedClients) {
                for (Socket socket : connectedClients) {
                    socket.close();
                }
                connectedClients.clear();
            }
        }
        serverSocket = null;
    }

    private class ClientProcessor implements Runnable {
        private final Socket client;

        ClientProcessor(Socket client) {
            this.client = client;
        }

        @Override
        public void run() {
            try (
                    DataInputStream input = new DataInputStream(client.getInputStream());
                    DataOutputStream output = new DataOutputStream(client.getOutputStream())) {
                int type = input.readInt();
                switch (type) {
                    case LIST_QUERY:
                        processListQuery(output, input.readUTF());
                        break;

                    case GET_QUERY:
                        processGetQuery(output, input.readUTF());
                        break;

                    default:
                        throw new IllegalStateException("Unknown query type: " + type);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void processGetQuery(DataOutputStream output, String fileName) throws IOException {
        File file = Paths.get(rootPath, fileName).toFile();
        if (!file.exists() || !file.isFile()) {
            output.writeLong(0);
            output.flush();
            return;
        }

        output.writeLong(file.length());
        BufferedInputStream fileContents = new BufferedInputStream(Files.newInputStream(file.toPath()));
        byte[] buffer = new byte[CHUNK_SIZE];
        while (fileContents.available() > 0) {
            int len = fileContents.read(buffer, 0, CHUNK_SIZE);
            output.write(buffer, 0, len);
            output.flush();
        }

    }

    private void processListQuery(DataOutputStream output, String listPath) throws IOException {
        List<Path> files = Files.walk(Paths.get(rootPath, listPath), 1)
                .collect(Collectors.toList());
        output.writeInt(files.size());
        for (Path path : files) {
            output.writeUTF(path.getFileName().toString());
            output.writeBoolean(path.toFile().isDirectory());
        }
        output.flush();
    }

    private class ClientAcceptor implements Runnable {

        @Override
        public void run() {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                FTPServer.this.serverSocket = serverSocket;
                while (!serverSocket.isClosed()) {
                    Socket client = serverSocket.accept();
                    synchronized (connectedClients) {
                        connectedClients.add(client);
                    }
                    executorService.submit(new ClientProcessor(client));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
