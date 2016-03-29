package ru.spbau.mit.simpleftp.common;

import java.io.InputStream;

public class MyFtpGetInputStreamResponse {
    private Long size;
    private InputStream inputStream;

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public boolean isFailed() {
        return size == 0;
    }

    public void setFailed() {
        size = 0L;
    }
}
