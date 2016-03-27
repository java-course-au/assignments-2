package ru.spbau.mit;

import org.apache.commons.io.input.BoundedInputStream;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class FTPClient implements Client {
    private Socket socket;
    private DataInputStream socketInputStream;
    private DataOutputStream socketOutputStream;

    @Override
    public void connect(String host, int port) throws IOException {
        socket = new Socket(host, port);
        socketInputStream = new DataInputStream(socket.getInputStream());
        socketOutputStream = new DataOutputStream(socket.getOutputStream());
    }

    @Override
    public void disconnect() throws IOException {
        socket.close();
    }

    @Override
    public List<FileInfo> executeList(String path) throws IOException {
        List<FileInfo> fileList = new ArrayList<>();
        socketOutputStream.writeInt(Constants.LIST_REQUEST);
        socketOutputStream.writeUTF(path);
        socketOutputStream.flush();
        long size = socketInputStream.readLong();
        for (int i = 0; i < size; i++) {
            String name = socketInputStream.readUTF();
            boolean isDirectory = socketInputStream.readBoolean();
            fileList.add(new FileInfo(name, isDirectory));
        }
        return fileList;
    }

    @Override
    public InputStream executeGet(String path) throws IOException {
        socketOutputStream.writeInt(Constants.GET_REQUEST);
        socketOutputStream.writeUTF(path);
        socketOutputStream.flush();
        long size = socketInputStream.readLong();
        return new BoundedInputStream(socketInputStream, size);
    }
}
