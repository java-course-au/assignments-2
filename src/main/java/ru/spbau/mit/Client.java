package ru.spbau.mit;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface Client {
    void connect(String path, int port) throws IOException;
    void disconnect() throws IOException;
    List<FileInfo> executeList(String path) throws IOException;
    InputStream executeGet(String path) throws IOException;
}
