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
import java.util.function.Function;
import java.util.stream.Collectors;

public class FTPServer implements Server {
    private ServerSocket serverSocket;
    private String rootPath;

    public FTPServer(int port, String rootPath) {
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.rootPath = rootPath;
    }

    @Override
    public void start() {
        new Thread(() -> {
            while (true) {
                if (serverSocket == null || serverSocket.isClosed()) {
                    break;
                }
                try {
                    Socket clientSocket = serverSocket.accept();
                    new Thread(() -> handleClient(clientSocket)).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void stop() {
        if (serverSocket == null) {
            return;
        }
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        serverSocket = null;
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
            }
            if (requestType == Constants.GET_REQUEST) {
                handleGet(outputStream, path);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeLong(DataOutputStream outputStream, long number) {
        try {
            outputStream.writeLong(number);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Path checkAndGetPath(DataOutputStream outputStream, String path, Function<Path, Boolean> fileFilter) {
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

    private void handleList(DataOutputStream outputStream, String path) {
        Path p = checkAndGetPath(outputStream, path, Files::isDirectory);
        if (p == null) {
            return;
        }
        try {
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleGet(DataOutputStream outputStream, String path) {
        Path p = checkAndGetPath(outputStream, path, Files::isRegularFile);
        if (p == null) {
            return;
        }
        try {
            outputStream.writeLong(Files.size(p));
            Files.copy(p, outputStream);
            outputStream.flush();
        } catch (IOException e) {
            writeLong(outputStream, 0);
            e.printStackTrace();
        }
    }
}
