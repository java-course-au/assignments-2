package ru.spbau.mit;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * Created by olga on 13.03.16.
 */
public class Server {
    private ServerSocket serverSocket;

    private static final int LIST_QUERY = 1;
    private static final int GET_QUERY = 2;

    private static final int BUFFER_SIZE = 2 * 1024 * 1024;

    public Server(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }

    public void start() {
        Thread thread = new Thread(() -> {
            try {
                catchSocket();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    public void stop() {
        try {
            serverSocket.close();
        } catch (IOException ignored) {
        }
    }

    private synchronized Socket accept() throws IOException {
        if (serverSocket.isClosed()) {
            return null;
        }
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
                Thread thread = new Thread(() -> {
                    decodeSocket(socket);
                });
                thread.start();
            } else {
                return;
            }
        }
    }

    private void decodeSocket(Socket socket) {
        try {
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

            while (!socket.isClosed()) {
                int operation = dis.readInt();
                String path = dis.readUTF();
                if (operation == LIST_QUERY) {
                    listQuery(path, dos);
                } else {
                    getQuery(path, dos);
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

    private void listQuery(String path, DataOutputStream dos) throws IOException {
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

    private void getQuery(String path, DataOutputStream dos) throws IOException {
        File f = new File(path);
        if (f.exists() && !f.isDirectory() && f.canRead()) {
            dos.writeLong(f.length());
            FileInputStream fileReader = new FileInputStream(f);
            byte[] buffer = new byte[(int) Math.min(f.length(), BUFFER_SIZE)];
            int countReadByte = fileReader.read(buffer);
            while (countReadByte != -1) {
                dos.write(buffer, 0, countReadByte);
                countReadByte = fileReader.read(buffer);
            }
        } else {
            dos.writeLong(0L);
        }
        dos.flush();
    }
}
