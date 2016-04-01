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
    public static final int FTP_REQUEST_LIST = 1;
    public static final int FTP_REQUEST_GET = 2;

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

    public int readRequest() throws IOException {
        return dis.readInt();
    }

    // LIST

    public void writeListRequest(String path) throws IOException {
        dos.writeInt(FTP_REQUEST_LIST);
        dos.writeUTF(path);
        dos.flush();
    }

    public String readListRequest() throws IOException {
        return dis.readUTF();
    }

    public void writeListResponse(List<FTPFileEntry> entries) throws IOException {
        dos.writeInt(entries.size());
        for (FTPFileEntry e : entries) {
            e.write(dos);
        }
        dos.flush();
    }

    public List<FTPFileEntry> readListResponse() throws IOException {
        List<FTPFileEntry> entries = new ArrayList<>();
        for (int size = dis.readInt(); size > 0; size--) {
            entries.add(FTPFileEntry.read(dis));
        }
        return entries;
    }

    // GET

    public void writeGetRequest(String path) throws IOException {
        dos.writeInt(FTP_REQUEST_GET);
        dos.writeUTF(path);
        dos.flush();
    }

    public String readGetRequest() throws IOException {
        return dis.readUTF();
    }

    public void writeGetResponse(long fileSize, InputStream fileStream) throws IOException {
        dos.writeLong(fileSize);
        if (fileSize > 0) {
            IOUtils.copyLarge(fileStream, dos);
        }
        dos.flush();
    }

    public InputStream readGetResponse() throws IOException {
        long fileSize = dis.readLong();
        return new LimitedInputStream(fileSize, dis);
    }
}

class LimitedInputStream extends InputStream {
    private long size;
    private InputStream is;

    LimitedInputStream(long size, InputStream is) {
        this.size = size;
        this.is = is;
    }

    @Override
    public int read() throws IOException {
        if (size == 0) {
            return -1;
        }
        int res = is.read();
        if (res == -1) {
            return res;
        }
        --size;
        return res;
    }
}
