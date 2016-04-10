/**
 * Created by n_buga on 31.03.16.
 */

import java.io.*;
import java.lang.reflect.Array;
import java.net.Socket;
import java.util.*;

public class Connection {
    static public final int LIST_QUERY = 1;
    static public final int UPLOAD_QUERY = 2;
    static public final int SOURCES_QUERY = 3;
    static public final int UPDATE_QUERY = 4;

    private boolean isClosed = false;
    private boolean isUpdated = false;
    private Client client = null;

    private DataInputStream in;
    private DataOutputStream out;
    private Socket socket;

    public Connection(Socket socket) {
        try {
            this.socket = socket;
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Client getClient() {
        return client;
    }

    public void update() {
        isUpdated = true;
    }

    public void resetIsUpdated() {
        isUpdated = false;
    }

    public boolean isUpdated() {
        return isUpdated;
    }

    public boolean isClosed() {
        return isClosed;
    }

    public void close() {
        isClosed = true;
        try {
            in.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Socket getSocket() {
        return socket;
    }

    public int readQueryType() {
        try {
            return in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int readInt() {
        try {
            return in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public String readString() {
        try {
            return in.readUTF();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public short readShort() {
        try {
            return in.readShort();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  -1;
    }

    public ArrayList<Integer> readIDs(int countFiles) {
        ArrayList<Integer> result = new ArrayList<>();
        for (int i = 0; i < countFiles; i++) {
            result.add(readInt());
        }
        return result;
    }

    public void sendListOfAvailableFiles(int size, ArrayList<FileInfo> files) {
        try {
            out.write(size);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (FileInfo file: files) {
            try {
                out.write(file.getID());
                out.write(file.getName().getBytes());
                out.write(file.getSize());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendInt(int id) {
        try {
            out.write(id);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendBoolean(boolean b) {
        try {
            out.writeBoolean(b);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendSources(Set<Client> clients) {
        try {
            out.write(clients.size());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (Client client: clients) {
            try {
                out.write(client.getServerIP());
                out.write(client.getServerPort());
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
