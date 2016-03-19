import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.File;
import java.util.ArrayList;
/**
 * Created by n_buga on 13.03.16.
 */
public class Server implements AutoCloseable {
    private ServerSocket serverSocket;

    public void close() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException ignored) { }
    }

    public Server(int portNumber) {
        try {
            serverSocket = new ServerSocket(portNumber);
        } catch (IOException e) {
            return;
        }
        (new Thread(() -> {
            try {
                connectionHandler();
            } catch (Exception e) {
                e.printStackTrace();
            }
        })).start();
    }

    public void connectionHandler() {
        try {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                Connection curConnection = new Connection(clientSocket);
                while (!curConnection.isClosed()) {
                    int askType = curConnection.readAskType();
                    if (askType == 0) {
                        curConnection.close();
                        break;
                    }
                    if (askType == 1) {
                        getHandler(curConnection);
                    } else if (askType == 2) {
                        listHandler(curConnection);
                    } else {
                        throw new Exception("Wrong format");
                    }
                }
                curConnection.close();
            }
        } catch (IOException e) {
            close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getHandler(Connection curConnection) throws IOException {
        String filePath = curConnection.readPath();
        File file = new File(filePath);
        int size;
        if (file.exists() && file.isFile()) {
            size = (int) file.length();
        } else {
            System.out.println("File doesn't exist");
            return;
        }
        InputStream is = new FileInputStream(file);
        curConnection.writeGet(size, is);
    }

    private void listHandler(Connection curConnection) throws IOException {
        String path = curConnection.readPath();
        File filePath = new File(path);
        File[] files = filePath.listFiles();
        ArrayList<FTPfile> ftpfiles = new ArrayList<>();
        if (files == null) {
            curConnection.writeList(0, ftpfiles);
            return;
        }
        for (File file: files) {
            ftpfiles.add(new FTPfile(file.getName(), file.isDirectory()));
        }
        curConnection.writeList(files.length, ftpfiles);
    }
}
