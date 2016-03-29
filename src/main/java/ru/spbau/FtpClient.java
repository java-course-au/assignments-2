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

    public void start() {
        try {
            socket = new Socket(hostname, port);
            inStream = new DataInputStream(socket.getInputStream());
            outStream = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        try {
            inStream.close();
            outStream.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> getList(final String path) throws IOException {
        List<String> list = null;

        outStream.writeInt(QUERY_GET_LIST);
        outStream.writeUTF(path);
        outStream.flush();

        String status = inStream.readUTF();
        if (status.equals("Ok")) {
            list = new ArrayList<>();
            int filesCount = inStream.readInt();
            for (int i = 0; i < filesCount; ++i) {
                list.add(inStream.readUTF() + " " + inStream.readUTF());
            }
        } else {
            System.out.println(status);
        }

        return list;
    }

    public InputStream getFile(final String path) throws IOException {
        if (outStream == null) {
            System.out.println("outStream = null");
            return null;
        }
        outStream.writeInt(QUERY_GET_FILE);
        outStream.writeUTF(path);
        outStream.flush();

        String status = inStream.readUTF();
        if (status.equals("Ok")) {
            long size = inStream.readLong();
            return new BoundedInputStream(inStream, size);
        } else {
            System.out.println(status);
        }

        return null;
    }
}
