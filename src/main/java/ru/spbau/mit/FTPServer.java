package ru.spbau.mit;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FTPServer implements Server {
    private ServerSocket serverSocket;
    private int port;
    private String rootPath;
    private ExecutorService taskExecutor;

    public FTPServer(int port, String rootPath) {
        this.port = port;
        this.rootPath = rootPath;
        taskExecutor = Executors.newCachedThreadPool();
    }

    @Override
    public void start() {
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        taskExecutor.submit(() -> {

            while (true) {
                synchronized (this) {
                    if (serverSocket == null || serverSocket.isClosed()) {
                        break;
                    }
                }
                try {
                    Socket clientSocket = serverSocket.accept();
                    taskExecutor.submit(() -> handleClient(clientSocket));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public synchronized void stop() {
        if (serverSocket == null) {
            return;
        }
        try {
            taskExecutor.shutdown();
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        serverSocket = null;
    }

    @Override
    public void join() throws InterruptedException {
        taskExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    }

    private void handleClient(Socket socket) {
        if (socket.isClosed()) {
            return;
        }
        DataInputStream inputStream;
        DataOutputStream outputStream;
        try {
            inputStream = new DataInputStream(socket.getInputStream());
            outputStream = new DataOutputStream(socket.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        try {
            int requestType;
            try {
                requestType = inputStream.readInt();
            } catch (EOFException e) {
                e.printStackTrace();
                return;
            }
            String path = rootPath + inputStream.readUTF();
            if (requestType == Constants.LIST_REQUEST) {
                handleList(outputStream, path);
            } else {
                if (requestType == Constants.GET_REQUEST) {
                    handleGet(outputStream, path);
                } else {
                    throw new UnsupportedOperationException();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeLong(DataOutputStream outputStream, long number) throws IOException {
        outputStream.writeLong(number);
        outputStream.flush();
    }

    private Path checkAndGetPath(DataOutputStream outputStream, String path, Function<Path, Boolean> fileFilter)
            throws IOException {
        Path p;
        try {
            p = Paths.get(path);
        } catch (InvalidPathException e) {
            writeLong(outputStream, 0);
            return null;
        }
        if (!Files.exists(p) || !fileFilter.apply(p)) {
            writeLong(outputStream, 0);
            return null;
        }
        return p;
    }

    private void handleList(DataOutputStream outputStream, String path) throws IOException {
        Path p = checkAndGetPath(outputStream, path, Files::isDirectory);
        if (p == null) {
            return;
        }
        List<FileInfo> filesList = Files.list(p)
                .map(currentPath ->
                        new FileInfo(currentPath.getFileName().toString(), Files.isDirectory(currentPath)))
                .collect(Collectors.toList());
        outputStream.writeLong(filesList.size());
        for (FileInfo fileInfo : filesList) {
            outputStream.writeUTF(fileInfo.getName());
            outputStream.writeBoolean(fileInfo.isDirectory());
        }
        outputStream.flush();
    }

    private void handleGet(DataOutputStream outputStream, String path) throws IOException {
        Path p = checkAndGetPath(outputStream, path, Files::isRegularFile);
        if (p == null) {
            return;
        }
        outputStream.writeLong(Files.size(p));
        Files.copy(p, outputStream);
        outputStream.flush();
    }
}
