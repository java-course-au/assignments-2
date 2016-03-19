import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by n_buga on 13.03.16.
 */
public class Client  {
    private String hostName;
    private int portNumber;

    public static class GetAnswerType {
        private InputStream inputStream;
        private final int STORAGE_SIZE = 1024;
        private byte[] storage = new byte[STORAGE_SIZE];
        private int size;
        private int curPosition = 0;
        private int positionInStorage = STORAGE_SIZE;

        public GetAnswerType(InputStream inputStream, int size) {
            this.inputStream = inputStream;
            this.size = size;
        }
        public InputStream getInputStream() {
            return inputStream;
        }
        public int getSize() {
            return size;
        }
        public boolean hasNextByte() {
            return curPosition < size;
        }
        public char readNextByte() throws Exception {
            if (curPosition >= size) {
                throw new Exception();
            }
            if (positionInStorage == STORAGE_SIZE) {
                int len = inputStream.read(storage, 0, STORAGE_SIZE);
                if (len == 0) {
                    throw new Exception("can't read anything");
                }
                positionInStorage = 0;
            }
            curPosition++;
            return (char) storage[positionInStorage++];
        }
    }

    public Client(String hostName, int portNumber) {
        this.hostName = hostName;
        this.portNumber = portNumber;
    }

    public GetAnswerType get(String path) throws IOException {
        Socket clientSocket = new Socket(hostName, portNumber);
        Connection curConnection = new Connection(clientSocket);
        curConnection.ask(1, path);
        return curConnection.readGet();
    }

    public ArrayList<FTPfile> list(String path) throws IOException {
        ArrayList<FTPfile> result;
        try (
                Socket clientSocket = new Socket(hostName, portNumber);
                Connection curConnection = new Connection(clientSocket)) {
            curConnection.ask(2, path);
            result = curConnection.readList();
        }
        return result;
    }
}
