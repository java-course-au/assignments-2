package ru.spbau.mit;

import java.io.IOException;

public interface NonBlockingBase {
    int onAcceptable();

    int onReadable();

    int onWritable() throws IOException;
}
