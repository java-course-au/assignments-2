package ru.spbau;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.input.BoundedInputStream;

/**
 * Created by rebryk on 10/03/16.
 */

public class FtpClient {
    private static final int QUERY_GET_LIST = 1;
    private static final int QUERY_GET_FILE = 2;

    private final String hostname;
    private final int port;

    private Socket socket;
    private DataInputStream inStream;
    private DataOutputStream outStream;

    public FtpClient(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public void start() throws IOException {
        socket = new Socket(hostname, port);
        inStream = new DataInputStream(socket.getInputStream());
        outStream = new DataOutputStream(socket.getOutputStream());
    }

    public void stop() throws IOException {
        inStream.close();
        outStream.close();
        socket.close();
    }

    public List<String> getList(final String path) throws IOException {
        outStream.writeInt(QUERY_GET_LIST);
        outStream.writeUTF(path);
        outStream.flush();

        List<String> list = new ArrayList<>();
        int filesCount = inStream.readInt();
        for (int i = 0; i < filesCount; ++i) {
            list.add(inStream.readUTF() + " " + inStream.readUTF());
        }

        return list;
    }

    public InputStream getFile(final String path) throws IOException {
        if (outStream == null) {
            return null;
        }

        outStream.writeInt(QUERY_GET_FILE);
        outStream.writeUTF(path);
        outStream.flush();

        long size = inStream.readLong();
        return new BoundedInputStream(inStream, size);
    }
}
