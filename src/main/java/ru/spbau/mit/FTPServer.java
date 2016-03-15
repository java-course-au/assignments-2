package ru.spbau.mit;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FTPServer {
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;

    private void listOperation(String path) throws IOException {
        final File[] listFiles = new File(path).listFiles();
        dataOutputStream.writeInt(listFiles.length);
        for (final File fileEntry : listFiles) {
            dataOutputStream.writeUTF(fileEntry.getName());
            dataOutputStream.writeBoolean(fileEntry.isDirectory());
        }
    }

    private void getOperation(String path) throws IOException {
        final byte[] byteArray = Files.readAllBytes(Paths.get(path));
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
                        default:
                            throw new UnsupportedOperationException();
                    }

                    dataOutputStream.flush();
                }
            }
        }
    }
}
