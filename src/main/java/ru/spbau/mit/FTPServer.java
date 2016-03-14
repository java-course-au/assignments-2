package ru.spbau.mit;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.logging.Logger;

public class FTPServer {

    public static final int SERVER_DEFAULT_PORT = 8080;
    public static final int LIST = 1;
    public static final int GET = 2;
    private static final int NOT_EXISTS = 0;
    private final ServerSocket serverSocket;
    private int port;
    private Thread workThread;

    public FTPServer (int portNumber) throws IOException {
        port = portNumber;
        serverSocket = new ServerSocket(port);
        workThread = new Thread(() -> {
            while(!Thread.interrupted()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    processClient(clientSocket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        workThread.start();
    }

    private void processClient(Socket clientSocket) throws IOException {
//        Socket clientSocket = serverSocket.accept();

        DataInputStream input = new DataInputStream(clientSocket.getInputStream());
        DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());

        try {
            while (true) {
                int requestType = input.readInt();
                String path = input.readUTF();

                if (requestType == LIST) {
                    list(output, path);
                } else if (requestType == GET) {
                    get(output, path);
                }
            }
        } catch (IOException e) {
            clientSocket.close();
        }
    }

    private static void list(DataOutputStream output, String path) throws IOException {
        Path filePath = Paths.get(path);
        if (Files.notExists(filePath)) {
            output.writeInt(NOT_EXISTS);
            output.flush();
            return;
        }

        File dir = new File(path);
        File[] files = dir.listFiles();

        output.writeInt(files.length);
        for (int i = 0; i < files.length; i++) {
            output.writeUTF(files[i].getPath());
            output.writeBoolean(files[i].isDirectory());
        }
        output.flush();
    }

    private static void get(DataOutputStream output, String path) throws IOException {
        Path filePath = Paths.get(path);
        if (Files.notExists(filePath)) {
            output.writeInt(NOT_EXISTS);
            output.flush();
            return;
        }

        int a = (int)Files.size(filePath);
        output.writeInt(a);
        output.write(Files.readAllBytes(filePath));
        output.flush();
    }

    public synchronized void tearDown () throws InterruptedException, IOException {
        workThread.interrupt();
        serverSocket.close();
        workThread.join();
    }
}

