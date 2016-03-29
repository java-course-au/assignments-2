package ru.spbau.mit.simpleftp.common;

import java.nio.file.Path;

public class MyFtpGetResponse implements MyFtpResponse {
    private Long size;
    private Path path;

    public Long getSize() {
        return size;
    }

    public Path getPath() {
        return path;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public boolean isFailed() {
        return size == 0;
    }

    public void setFailed() {
        size = 0L;
    }
}
