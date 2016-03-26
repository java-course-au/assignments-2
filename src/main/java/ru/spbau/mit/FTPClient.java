package ru.spbau.mit;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.List;

/**
 * Created by ldvsoft on 08.03.16.
 */
public class FTPClient implements AutoCloseable {
    private FTPConnection connection;

    public FTPClient(String host, int port) throws IOException {
        connection = new FTPConnection(new Socket(host, port));
    }

    @Override
    public void close() {
        connection.close();
    }

    public List<FTPFileEntry> list(String path) throws IOException {
        connection.writeListRequest(path);
        return connection.readListResponse();
    }

    public InputStream get(String path) throws IOException {
        connection.writeGetRequest(path);
        return connection.readGetResponse();
    }
}
