package ru.spbau;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rebryk on 10/03/16.
 */
public class FtpClient {
    private static final int BUFFER_SIZE = 4096;

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

    public List<String> getList(final String path) {
        List<String> list = null;

        try {
            outStream.writeInt(1);
            outStream.writeUTF(path);

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
        } catch (IOException e) {
            e.printStackTrace();
        }

        return list;
    }

    public ByteArrayOutputStream getFile(final String path) {
        ByteArrayOutputStream file = null;
        try {
            outStream.writeInt(2);
            outStream.writeUTF(path);

            String status = inStream.readUTF();
            if (status.equals("Ok")) {
                file = new ByteArrayOutputStream();
                long packetsCount = inStream.readLong();
                byte[] data = new byte[BUFFER_SIZE];
                for (int i = 0; i < packetsCount; ++i) {
                    int count = inStream.read(data);
                    file.write(data, 0, count);
                }
                file.close();
            } else {
                System.out.println(status);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }
}
