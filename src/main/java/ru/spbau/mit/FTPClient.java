package ru.spbau.mit;

import org.apache.commons.io.input.BoundedInputStream;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tehnar on 07.03.2016.
 */
public class FTPClient implements Closeable {
    private final int port;
    private final String host;
    private Socket connection = null;
    private DataInputStream input;
    private DataOutputStream output;

    private static final int LIST_QUERY = 1;
    private static final int GET_QUERY = 2;
    private static final int CHUNK_SIZE = 4096;

    public FTPClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    private void connect() throws IOException {
        connection = new Socket(host, port);
        input = new DataInputStream(connection.getInputStream());
        output = new DataOutputStream(connection.getOutputStream());
    }

    public List<String> listFiles(String path) throws IOException {
        if (connection == null) {
            connect();
        }
        output.writeInt(LIST_QUERY);
        output.writeUTF(path);
        output.flush();
        int size = input.readInt();
        List<String> files = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            String filename = input.readUTF();
            if (input.readBoolean()) {
                files.add(filename + "/");
            } else {
                files.add(filename);
            }
        }
        return files;
    }

    public InputStream getFile(String filename) throws IOException {
        if (connection == null) {
            connect();
        }
        output.writeInt(GET_QUERY);
        output.writeUTF(filename);
        output.flush();

        long size = input.readLong();
        return new BoundedInputStream(input, size);
    }

    @Override
    public void close() throws IOException {
        if (connection == null) {
            return;
        }
        connection.close();
        connection = null;
        input = null;
        output = null;
    }
}
