package ru.spbau.mit;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ldvsoft on 08.03.16.
 */
public class FTPConnection implements AutoCloseable {
    public static final int FTP_ACTION_LIST = 1;
    public static final int FTP_ACTION_GET = 2;

    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;

    public FTPConnection(Socket socket) throws IOException {
        this.socket = socket;
        dis = new DataInputStream(socket.getInputStream());
        dos = new DataOutputStream(socket.getOutputStream());
    }

    public void close() {
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }

    public int readAction() throws IOException {
        return dis.readInt();
    }

    // LIST

    public void writeActionList(String path) throws IOException {
        dos.writeInt(FTP_ACTION_LIST);
        dos.writeUTF(path);
        dos.flush();
    }

    public String readActionList() throws IOException {
        return dis.readUTF();
    }

    public void writeList(List<FTPFileEntry> entries) throws IOException {
        dos.writeInt(entries.size());
        for (FTPFileEntry e : entries) {
            e.write(dos);
        }
        dos.flush();
    }

    public List<FTPFileEntry> readList() throws IOException {
        List<FTPFileEntry> entries = new ArrayList<>();
        for (int size = dis.readInt(); size > 0; size--) {
            entries.add(FTPFileEntry.read(dis));
        }
        return entries;
    }

    // GET

    public void writeActionGet(String path) throws IOException {
        dos.writeInt(FTP_ACTION_GET);
        dos.writeUTF(path);
        dos.flush();
    }

    public String readActionGet() throws IOException {
        return dis.readUTF();
    }

    public void writeGet(long fileSize, InputStream fileStream) throws IOException {
        dos.writeLong(fileSize);
        if (fileSize > 0) {
            IOUtils.copyLarge(fileStream, dos);
        }
        dos.flush();
    }

    public void readGet(OutputStream fileStream) throws IOException {
        long fileSize = dis.readLong();
        if (fileSize > 0) {
            IOUtils.copyLarge(dis, fileStream, 0, fileSize);
        }
    }
}
