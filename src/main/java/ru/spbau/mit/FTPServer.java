package ru.spbau.mit;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;

public class FTPServer {
    public enum Command {LIST, GET}

    private final int port;
    private ServerSocket serverSocket;

    public FTPServer(int port) {
        this.port = port;
    }

    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            while (true) {
                Socket socket;
                try {
                    socket = serverSocket.accept();
                } catch (SocketException e) {
                    return;
                }

                new Thread(() -> {
                    try (DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                         DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream())) {
                        Command command = Command.values()[dataInputStream.readInt()];
                        String path = dataInputStream.readUTF();

                        if (command == Command.LIST) {
                            File[] listFiles = new File(path).listFiles();
                            if (listFiles == null) {
                                dataOutputStream.writeInt(0);
                            } else {
                                dataOutputStream.writeInt(listFiles.length);
                                for (File fileEntry : listFiles) {
                                    dataOutputStream.writeUTF(fileEntry.getName());
                                    dataOutputStream.writeBoolean(fileEntry.isDirectory());
                                }
                            }
                        } else {
                            byte[] byteArray;
                            try {
                                byteArray = Files.readAllBytes(Paths.get(path));
                                dataOutputStream.writeLong(byteArray.length);
                                dataOutputStream.write(byteArray);
                            } catch (NoSuchFileException e) {
                                dataOutputStream.writeLong(0);
                            }
                        }
                        dataOutputStream.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
