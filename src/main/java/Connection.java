import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by n_buga on 13.03.16.
 */
public class Connection implements AutoCloseable {
    private Socket socket;
    private Boolean isClosed;
    private DataOutputStream out;
    private DataInputStream in;

    public boolean isClosed() {
        return isClosed;
    }

    public Connection(Socket socket) throws IOException {
        this.socket = socket;
        out = new DataOutputStream(socket.getOutputStream());
        in = new DataInputStream(socket.getInputStream());
        isClosed = false;
    }

    public synchronized void close() {
        if (isClosed) {
            return;
        }
        try {
            socket.close();
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
        } catch (IOException ignored) { }
        isClosed = true;
    }

    public void waitUntilClosed() {
        final int delay = 100;
        try {
            while (in != null && in.available() > 0) {
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException err) {
                    System.out.println("Unexpected interrupt while waiting closing connection");
                    break;
                }
            }
        } catch (IOException ignored) { }
        close();
    }

    public void ask(int id, String path) throws IOException {
        out.writeInt(id);
        out.flush();
        out.writeUTF(path);
        out.flush();
    }

    public int readAskType() {
        try {
            return in.readInt();
        } catch (IOException e) {
            isClosed = true;
            return 0;
        }
    }

    public String readPath() throws IOException {
        return in.readUTF();
    }

    public void writeGet(int size, InputStream file) throws IOException {
        final int packetSize = 1024;
        out.writeInt(size);
        out.flush();
        byte[] buf = new byte[packetSize];
        int len;
        while ((len = file.read(buf)) > 0) {
            out.write(buf, 0, len);
            out.flush();
        }
    }

    public Client.GetResponseContent readGet() throws IOException {
        int size = in.readInt();
        Client.GetResponseContent result = new Client.GetResponseContent(in, size);
        (new Thread(this::waitUntilClosed)).start();
        return result;
    }

    public void writeList(int size, ArrayList<FTPfile> files) throws IOException {
        out.writeInt(size);
        out.flush();
        for (FTPfile ftpfile: files) {
            out.writeUTF(ftpfile.getName());
            out.writeBoolean(ftpfile.isDirectory());
            out.flush();
        }
    }

    public ArrayList<FTPfile> readList() throws IOException {
        int size = in.readInt();
        ArrayList<FTPfile> files = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            String name = in.readUTF();
            Boolean isDirectory = in.readBoolean();
            files.add(new FTPfile(name, isDirectory));
        }
        close();
        return files;
    }
}
