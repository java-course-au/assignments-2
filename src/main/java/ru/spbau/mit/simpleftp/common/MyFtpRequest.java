package ru.spbau.mit.simpleftp.common;

import java.nio.file.Path;

public class MyFtpRequest {
    public MyFtpRequest() {}
    public enum Type {
        GET, LIST;
    }

    private Type type;
    private Path path;

    public void setType(Type type) {
        this.type = type;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public Type getType() {
        return type;
    }

    public Path getPath() {
        return path;
    }

    public MyFtpRequest(Type type, Path path) {
        this.type = type;
        this.path = path;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        switch (type) {
            case GET:
                stringBuilder.append("GET ");
                break;
            default:
                stringBuilder.append("LIST ");
                break;
        }
        stringBuilder.append(path.toString());
        return stringBuilder.toString();
    }

    public MyFtpRequest resolvePathWithRootDir(Path rootDir) {
        path = rootDir.resolve(path);
        return this;
    }
}
