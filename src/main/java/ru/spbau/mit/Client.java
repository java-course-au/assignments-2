package ru.spbau.mit;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class Client {
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;

    private static final int LIST_QUERY = 1;
    private static final int GET_QUERY = 2;

    public Client(String host, int port) throws IOException {
        socket = new Socket(host, port);
        dis = new DataInputStream(socket.getInputStream());
        dos = new DataOutputStream(socket.getOutputStream());
    }

    public void close() {
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }

    public ArrayList<FileEntry> list(String path) throws IOException {
        dos.writeInt(LIST_QUERY);
        dos.writeUTF(path);

        dos.flush();

        int size = dis.readInt();
        ArrayList<FileEntry> listOfFile = new ArrayList<>();
        for (int i = 0; i < size; ++i) {
            FileEntry file = new FileEntry();
            file.name = dis.readUTF();
            file.isDir = dis.readBoolean();
            listOfFile.add(file);
        }
        return listOfFile;
    }

    public InputStream get(String path) throws IOException {
        dos.writeInt(GET_QUERY);
        dos.writeUTF(path);

        dos.flush();

        long size = dis.readLong();

        return new ServerInputStream(dis, size);
    }

    public class FileEntry {
        private String name;
        private Boolean isDir;

        public Boolean getDir() {
            return isDir;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setDir(Boolean dir) {
            isDir = dir;
        }
    }

    public class ServerInputStream extends InputStream {
        private DataInputStream dis;
        private long fileSize;

        ServerInputStream(DataInputStream dis, long fileSize) {
            this.dis = dis;
            this.fileSize = fileSize;
        }

        @Override
        public int read() throws IOException {
            if (fileSize == 0) {
                return -1;
            } else {
                --fileSize;
                return dis.read();
            }
        }
    }
}
