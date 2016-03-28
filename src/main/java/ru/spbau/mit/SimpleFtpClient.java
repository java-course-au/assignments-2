package ru.spbau.mit;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import static ru.spbau.mit.SimpleFtpProtocol.COMMAND_GET;
import static ru.spbau.mit.SimpleFtpProtocol.COMMAND_LIST;

public class SimpleFtpClient implements AutoCloseable {
    private final Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private boolean commandWasHandled = false;

    public SimpleFtpClient(String host, int port) throws IOException {
        this(new Socket(host, port));
    }

    public SimpleFtpClient(Socket socket) throws IOException {
        this.socket = socket;
        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
        } catch (Exception e) {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
            throw e;
        }
    }

    public DirectoryItem[] list(String path) throws IOException {
        if (commandWasHandled) {
            throw new IllegalStateException("Cannot handle more than one command per connection");
        }
        commandWasHandled = true;
        out.writeInt(COMMAND_LIST);
        out.writeUTF(path);
        int count = in.readInt();
        DirectoryItem[] result = new DirectoryItem[count];
        for (int i = 0; i < count; i++) {
            String name = in.readUTF();
            boolean isDirectory = in.readBoolean();
            result[i] = new DirectoryItem(name, isDirectory);
        }
        return result;
    }

    public InputStream get(String path) throws IOException {
        if (commandWasHandled) {
            throw new IllegalStateException("Cannot handle more than one command per connection");
        }
        commandWasHandled = true;
        out.writeInt(COMMAND_GET);
        out.writeUTF(path);
        int length = in.readInt();
        return in;
    }

    @Override
    public void close() throws IOException {
        in.close();
        out.close();
        socket.shutdownInput();
        socket.shutdownOutput();
        socket.close();
    }
}
