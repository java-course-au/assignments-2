package ru.spbau.mit;

import java.io.OutputStream;
import java.util.List;

public interface Client {
    void connect(String path, int port);
    void disconnect();
    List<FileInfo> executeList(String path);
    void executeGet(String path, OutputStream outputStream);
}
