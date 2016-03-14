package ru.spbau.mit;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class FTPClient implements Client {
    private static final int BUFFER_SIZE = 4096;

    private Socket socket;
    private DataInputStream socketInputStream;
    private DataOutputStream socketOutputStream;

    @Override
    public void connect(String host, int port) {
        try {
            socket = new Socket(host, port);
            socketInputStream = new DataInputStream(socket.getInputStream());
            socketOutputStream = new DataOutputStream(socket.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void disconnect() {
        try {
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<FileInfo> executeList(String path) {
        try {
            socketOutputStream.writeInt(Constants.LIST_REQUEST);
            socketOutputStream.writeUTF(path);
            socketOutputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<FileInfo> fileList = new ArrayList<>();
        try {
            long size = socketInputStream.readLong();
            for (int i = 0; i < size; i++) {
                String name = socketInputStream.readUTF();
                boolean isDirectory = socketInputStream.readBoolean();
                fileList.add(new FileInfo(name, isDirectory));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileList;
    }

    @Override
    public void executeGet(String path, OutputStream outputStream) {
        try {
            socketOutputStream.writeInt(Constants.GET_REQUEST);
            socketOutputStream.writeUTF(path);
            socketOutputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
        byte[] buffer = new byte[BUFFER_SIZE];
        try {
            long size = socketInputStream.readLong();
            while (size > 0) {
                int currentSize = socketInputStream.read(buffer);
                outputStream.write(buffer, 0, currentSize);
                size -= currentSize;
            }
            outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
