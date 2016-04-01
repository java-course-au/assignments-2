import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by n_buga on 13.03.16.
 */
public class Client implements AutoCloseable {
    private Connection curConnection;

    public void close() {
        curConnection.close();
    }

    public Client(String hostName, int portNumber) throws ConnectException{
        for (int i = 0; i < 5; i++) {
            try {
                Socket clientSocket = new Socket(hostName, portNumber);
                curConnection = new Connection(clientSocket);
                break;
            } catch (ConnectException e) {
                throw e;
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void get(String path, OutputStream os) throws IOException {
        curConnection.ask(1, path);
        curConnection.readGet(os);
    }

    public ArrayList<FTPfile> list(String path) throws IOException {
        curConnection.ask(2, path);
        return curConnection.readList();
    }
}
