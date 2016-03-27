package ru.spbau.mit;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class Server {
    private ServerSocket serverSocket;

    private static final int LIST_QUERY = 1;
    private static final int GET_QUERY = 2;

    private final int port;

    public Server(int port) {
        this.port = port;
    }

    public Thread start() throws IOException {
        serverSocket = new ServerSocket(port);

        Thread thread = new Thread(() -> {
            try {
                catchSocket();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.start();
        return thread;
    }

    public synchronized void stop() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Socket accept() throws IOException {
        try {
            return serverSocket.accept();
        } catch (SocketException e) {
            return null;
        }

    }

    private void catchSocket() throws IOException {
        while (true) {
            Socket socket = accept();
            if (socket != null) {
                handlingQuery(socket);
            } else {
                return;
            }
        }
    }

    private void handlingQuery(Socket socket) {
        try {
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

            while (!socket.isClosed()) {
                int operation = dis.readInt();
                String path = dis.readUTF();
                if (operation == LIST_QUERY) {
                    handlingListQuery(path, dos);
                } else if (operation == GET_QUERY) {
                    handlingGetQuery(path, dos);
                } else {
                    System.err.printf("Wrong query");
                }
            }
        } catch (IOException ignored) {
        } finally {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }

    private static void handlingListQuery(String path, DataOutputStream dos) throws IOException {
        File f = new File(path);
        if (f.isDirectory()) {
            File[] fileList = f.listFiles();
            if (fileList == null) {
                dos.writeInt(0);
                return;
            }
            dos.writeInt(fileList.length);
            for (File aFileList : fileList) {
                dos.writeUTF(aFileList.getName());
                dos.writeBoolean(aFileList.isDirectory());
            }
        } else {
            dos.write(0);
        }
    }

    private static void handlingGetQuery(String path, DataOutputStream dos) throws IOException {
        File f = new File(path);
        if (f.exists() && !f.isDirectory() && f.canRead()) {
            dos.writeLong(f.length());
            FileInputStream fileReader = new FileInputStream(f);

            IOUtils.copyLarge(fileReader, dos);
        } else {
            dos.writeLong(0L);
        }
        dos.flush();
    }
}
