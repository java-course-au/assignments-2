package ru.spbau.mit;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by olga on 13.03.16.
 */
public class Client {
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;

    private static final int LIST_QUERY = 1;
    private static final int GET_QUERY = 2;
    private static final int BUFFER_SIZE = 2 * 1024 * 1024;

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

    public long get(String path, OutputStream outputStream) throws IOException {
        dos.writeInt(GET_QUERY);
        dos.writeUTF(path);

        dos.flush();

        long size = dis.readLong();
        byte[] buffer = new byte[BUFFER_SIZE];

        for (long i = 0; i < size;) {
            int cntReadByte = dis.read(buffer, 0, (int) Math.min(BUFFER_SIZE, size - i));
            outputStream.write(buffer, 0, cntReadByte);
            i += cntReadByte;
        }

        return size;
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
}
