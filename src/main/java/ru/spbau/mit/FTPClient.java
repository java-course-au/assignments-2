package ru.spbau.mit;

import org.apache.commons.io.input.BoundedInputStream;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class FTPClient {
    private final String host;
    private final int port;

    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;

    public FTPClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public static class FileEntry implements Comparable<FileEntry> {
        private static final int HASH_BASE = 31;

        private final String name;
        private final boolean isDirectory;

        public FileEntry(String name, boolean isDirectory) {
            this.name = name;
            this.isDirectory = isDirectory;
        }

        @Override
        public int compareTo(FileEntry fileEntry) {
            int a = name.compareTo(fileEntry.name);
            if (a != 0) {
                return a;
            }
            return Boolean.compare(isDirectory, fileEntry.isDirectory);
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof FileEntry && compareTo((FileEntry) o) == 0;
        }

        @Override
        public int hashCode() {
            return name.hashCode() * HASH_BASE + Boolean.hashCode(isDirectory);
        }
    }

    public void run() throws IOException {
        socket = new Socket(host, port);
        dataInputStream = new DataInputStream(socket.getInputStream());
        dataOutputStream = new DataOutputStream(socket.getOutputStream());
    }

    public void stop() throws IOException {
        socket.close();
    }

    public List<FileEntry> getFilesList(String path) throws IOException {
        dataOutputStream.writeInt(FTPServer.Command.LIST.ordinal());
        dataOutputStream.writeUTF(path);
        dataOutputStream.flush();

        int count = dataInputStream.readInt();
        List<FileEntry> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(new FileEntry(dataInputStream.readUTF(), dataInputStream.readBoolean()));
        }
        return list;
    }

    public InputStream getFileStream(String path) throws IOException {
        dataOutputStream.writeInt(FTPServer.Command.GET.ordinal());
        dataOutputStream.writeUTF(path);
        dataOutputStream.flush();

        return new BoundedInputStream(dataInputStream, dataInputStream.readLong());
    }
}
