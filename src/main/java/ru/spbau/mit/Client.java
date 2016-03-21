package ru.spbau.mit;

import java.io.InputStream;
import java.util.List;

public interface Client {
    void connect(String path, int port);
    void disconnect();
    List<FileInfo> executeList(String path);
    InputStream executeGet(String path);
}
