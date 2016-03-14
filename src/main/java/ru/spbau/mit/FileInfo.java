package ru.spbau.mit;

public class FileInfo {
    private static final int P = (int) 1e9 + 7;

    private String name;
    private boolean isDirectory;

    public FileInfo(String name, boolean isDirectory) {
        this.name = name;
        this.isDirectory = isDirectory;
    }

    public String getName() {
        return name;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof FileInfo)) {
            return false;
        }
        FileInfo fileInfo = (FileInfo) obj;
        return name.equals(fileInfo.name) && isDirectory == fileInfo.isDirectory;
    }

    @Override
    public int hashCode() {
        return name.hashCode() * P + (isDirectory ? 1 : 0);
    }
}
