package ru.spbau.mit;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by ldvsoft on 08.03.16.
 */
public final class FTPFileEntry {
    private String fileName;
    private boolean isDirectory;

    public String getFileName() {
        return fileName;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public FTPFileEntry(String fileName, boolean isDirectory) {
        this.fileName = fileName;
        this.isDirectory = isDirectory;
    }

    @Override
    public int hashCode() {
        return fileName.hashCode() + Boolean.valueOf(isDirectory).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof FTPFileEntry)) {
            return false;
        }
        FTPFileEntry that = (FTPFileEntry) obj;
        return this.fileName.equals(that.fileName) && this.isDirectory == that.isDirectory;
    }

    @Override
    public String toString() {
        return String.format("[%s%s]", fileName, isDirectory ? " DIR" : "");
    }

    public void write(DataOutputStream dos) throws IOException {
        dos.writeUTF(fileName);
        dos.writeBoolean(isDirectory);
    }

    public static FTPFileEntry read(DataInputStream dis) throws IOException {
        return new FTPFileEntry(
                dis.readUTF(),
                dis.readBoolean()
        );
    }
}
