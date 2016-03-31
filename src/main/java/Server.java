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
    private final int CLOSE_QUERY = 0;
    private final int GET_QUERY = 1;
    private final int LIST_QUERY = 2;
    private ServerSocket serverSocket;

    public void close() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException ignored) { }
    }

    public Server(int portNumber) {
        start(portNumber);
    }

    public void connectionCatcher() {
        try {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                (new Thread(() -> connectionHandler(clientSocket))).start();
            }
        } catch (IOException e) {
            close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void connectionHandler(Socket clientSocket) {
        try (Connection curConnection = new Connection(clientSocket)) {
            while (!curConnection.isClosed()) {
                int requestType = curConnection.readAskType();
                if (requestType == Client.CLOSE_QUERY) {
                    curConnection.close();
                    break;
                }
                if (requestType == Client.GET_QUERY) {
                    getFileContent(curConnection);
                } else if (requestType == Client.LIST_QUERY) {
                    getListOfFiles(curConnection);
                } else {
                    throw new Exception("Wrong format");
                }
            }
        } catch (IOException e) {
            close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void start(int portNumber) {
        try {
            serverSocket = new ServerSocket(portNumber);
        } catch (IOException e) {
            return;
        }
        (new Thread(() -> {
            try {
                connectionCatcher();
            } catch (Exception e) {
                e.printStackTrace();
            }
        })).start();
    }

    private void getFileContent(Connection curConnection) throws IOException {
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

    private void getListOfFiles(Connection curConnection) throws IOException {
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
