package ru.spbau.mit;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;

/**
 * Created by ldvsoft on 08.03.16.
 */
public class FTPClient {
    private String host;
    private int port;

    public FTPClient(String host, int port) throws IOException {
        this.host = host;
        this.port = port;
    }

    public List<FTPFileEntry> list(String path) throws IOException {
        try (FTPConnection connection = new FTPConnection(new Socket(host, port))) {
            connection.writeActionList(path);
            return connection.readList();
        }
    }

    public void get(String path, OutputStream outputStream) throws IOException {
        try (FTPConnection connection = new FTPConnection(new Socket(host, port))) {
            connection.writeActionGet(path);
            connection.readGet(outputStream);
        }
    }
}
