import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by n_buga on 13.03.16.
 */
public class Connection {
    Socket socket;
    Boolean isClosed;
    DataOutputStream out;
    DataInputStream in;
    public Connection (Socket socket) throws IOException {
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
            if (out != null) out.close();
            if (in != null) in.close();
        } catch (IOException ignored) {}
        isClosed = true;
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
        out.writeInt(size);
        out.flush();
        byte[] buf = new byte[1024];
        int len;
        while ((len = file.read(buf)) > 0) {
            out.write(buf, 0, len);
            out.flush();
        }
    }

    public void readGet(OutputStream os) throws IOException {
        int size = in.readInt();
        byte[] buf = new byte[1024];
        while (size > 0) {
            int len = in.read(buf, 0, Math.min(1024, size));
            if (len == 0) continue;
            os.write(buf, 0, len);
            os.flush();
            size -= len;
        }
    }

    public void writeList(int size, ArrayList<FTPfile> files) throws IOException {
        out.writeInt(size);
        out.flush();
        for (FTPfile ftpfile: files) {
            out.writeUTF(ftpfile.name);
            out.writeBoolean(ftpfile.isDirectory);
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
        return files;
    }
}
