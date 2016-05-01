package ru.spbau.mit;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static final int LIST = 1;
    private static final int GET = 2;

    private final int port;
    private ServerSocket serverSocket;

    public Server(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        new Thread(this::handleConnections).start();
    }

    private void handleConnections() {
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                processQuery(socket);
                socket.close();
            } catch (IOException e) {
                System.err.println("Error in handling connection in server");
                break;
            }
        }
    }

    private void processQuery(Socket socket) {
        try (DataInputStream inputStream = new DataInputStream(socket.getInputStream());
             DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream())) {
            int request = inputStream.readInt();
            switch (request) {
                case LIST:
                    processListQuery(inputStream, outputStream);
                    break;
                case GET:
                    processGetQuery(inputStream, outputStream);
                    break;
                default:
                    System.err.println("Incorrect query to server: " + request);
            }
        } catch (IOException e) {
            System.err.println("Error in processing query in server");
        }
    }

    private void processListQuery(DataInputStream inputStream, DataOutputStream outputStream)
            throws IOException {
        String path = inputStream.readUTF();
        File directoryToList = new File(path);
        if (directoryToList.exists() && directoryToList.isDirectory()) {
            File[] files = directoryToList.listFiles();
            if (files == null) {
                outputStream.writeInt(0);
                return;
            }
            outputStream.writeInt(files.length);
            for (File file : files) {
                outputStream.writeUTF(file.getName());
                outputStream.writeBoolean(file.isDirectory());
            }
        } else {
            outputStream.writeInt(0);
        }
    }

    private void processGetQuery(DataInputStream inputStream, DataOutputStream outputStream)
            throws IOException {
        String path = inputStream.readUTF();
        File file = new File(path);
        if (file.exists() && !file.isDirectory()) {
            outputStream.writeLong(file.length());
            IOUtils.copyLarge(new FileInputStream(file), outputStream);
        } else {
            outputStream.writeLong(0);
        }
    }
}
