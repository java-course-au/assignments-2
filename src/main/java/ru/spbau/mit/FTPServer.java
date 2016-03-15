package ru.spbau.mit;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;

public class FTPServer {
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;

    private void listOperation(String path) throws IOException {
        final File[] listFiles = new File(path).listFiles();
        if (listFiles == null) {
            dataOutputStream.writeInt(0);
            return;
        }

        dataOutputStream.writeInt(listFiles.length);
        for (final File fileEntry : listFiles) {
            dataOutputStream.writeUTF(fileEntry.getName());
            dataOutputStream.writeBoolean(fileEntry.isDirectory());
        }
    }

    private void getOperation(String path) throws IOException {
        final byte[] byteArray;
        try {
            byteArray = Files.readAllBytes(Paths.get(path));
        } catch (NoSuchFileException e) {
            dataOutputStream.writeLong(0);
            return;
        }

        dataOutputStream.writeLong(byteArray.length);
        dataOutputStream.write(byteArray);
    }

    public FTPServer(int port) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                try (Socket socket = serverSocket.accept()) {
                    final InputStream inputStream = socket.getInputStream();
                    final OutputStream outputStream = socket.getOutputStream();
                    dataInputStream = new DataInputStream(inputStream);
                    dataOutputStream = new DataOutputStream(outputStream);

                    int type = dataInputStream.readInt();
                    String path = dataInputStream.readUTF();
                    switch (type) {
                        case 1:
                            listOperation(path);
                            break;
                        case 2:
                            getOperation(path);
                            break;
                        case -1:
                            return;
                        default:
                            throw new UnsupportedOperationException();
                    }

                    dataOutputStream.flush();
                }
            }
        }
    }
}
