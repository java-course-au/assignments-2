package ru.spbau.mit;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Client {
    private final String host;
    private final int port;

    public Client(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public List<FileInfo> sendListQuery(String path) throws IOException {
        Socket socket = new Socket(host, port);
        DataInputStream inputStream = new DataInputStream(socket.getInputStream());
        DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());

        outputStream.writeInt(1);
        outputStream.writeUTF(path);

        List<FileInfo> list = new ArrayList<>();
        int size = inputStream.readInt();
        for (int i = 0; i < size; i++) {
            String name = inputStream.readUTF();
            boolean isDir = inputStream.readBoolean();
            list.add(new FileInfo(name, isDir));
        }

        socket.close();
        return list;
    }

    public InputStream sendGetQuery(String path) throws IOException {
        Socket socket = new Socket(host, port);
        DataInputStream inputStream = new DataInputStream(socket.getInputStream());
        DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());

        outputStream.writeInt(2);
        outputStream.writeUTF(path);

        long size = inputStream.readLong();
        return inputStream;
    }
}
