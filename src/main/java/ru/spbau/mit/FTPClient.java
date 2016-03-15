package ru.spbau.mit;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class FTPClient {
    private String host;
    private int port;

    public static class FileEntry implements Comparable<FileEntry> {
        private String name;
        private boolean isDirectory;

        public FileEntry(String name, boolean isDirectory) {
            this.name = name;
            this.isDirectory = isDirectory;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean isDirectory() {
            return isDirectory;
        }

        public void setDirectory(boolean isDirectory) {
            this.isDirectory = isDirectory;
        }

        @Override
        public int compareTo(FileEntry o) {
            int a = name.compareTo(o.name);
            if (a != 0) {
                return a;
            }
            return new Boolean(isDirectory).compareTo(o.isDirectory);
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof FileEntry)) {
                return false;
            }
            return compareTo((FileEntry) o) == 0;
        }

        @Override
        public int hashCode() {
            int hashCode = name.hashCode() * 2;
            if (isDirectory) {
                hashCode += 1;
            }
            return hashCode;
        }
    }


    public List<FileEntry> listOperation(String path) throws IOException {
        try (Socket socket = new Socket(host, port)) {
            final InputStream inputStream = socket.getInputStream();
            final OutputStream outputStream = socket.getOutputStream();
            final DataInputStream dataInputStream = new DataInputStream(inputStream);
            final DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

            dataOutputStream.writeInt(1);
            dataOutputStream.writeUTF(path);
            dataOutputStream.flush();

            int count = dataInputStream.readInt();
            ArrayList<FileEntry> list = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                list.add(new FileEntry(dataInputStream.readUTF(), dataInputStream.readBoolean()));
            }
            return list;
        }
    }

    public byte[] getOperation(String path) throws IOException {
        try (Socket socket = new Socket(host, port)) {
            final InputStream inputStream = socket.getInputStream();
            final OutputStream outputStream = socket.getOutputStream();
            final DataInputStream dataInputStream = new DataInputStream(inputStream);
            final DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

            dataOutputStream.writeInt(2);
            dataOutputStream.writeUTF(path);
            dataOutputStream.flush();

            long sizeLong = dataInputStream.readLong();
            int sizeInt;
            try {
                sizeInt = Math.toIntExact(sizeLong);
            } catch (ArithmeticException e) {
                throw new UnsupportedOperationException("Too large file");
            }
            byte[] byteArray = new byte[sizeInt];
            dataInputStream.read(byteArray);
            return byteArray;
        }
    }

    public void closeConnection() throws IOException {
        try (Socket socket = new Socket(host, port)) {
            final OutputStream outputStream = socket.getOutputStream();
            final DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

            dataOutputStream.writeInt(-1);
            dataOutputStream.writeUTF("");
            dataOutputStream.flush();
            return;
        }
    }

    public FTPClient(String host, int port) throws IOException {
        this.host = host;
        this.port = port;
    }
}
