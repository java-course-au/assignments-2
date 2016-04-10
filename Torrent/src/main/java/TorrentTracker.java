/**
 * Created by n_buga on 31.03.16.
 */

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class TorrentTracker implements AutoCloseable {
    static private final int TIME_OUT =  10000;

    private ArrayList<FileInfo> availableFiles;
    private Map<Integer, FileInfo> idFileMap;
    private int maxID = 0;
    private boolean end = false;

    public TorrentTracker() {
        availableFiles = new ArrayList<FileInfo>();
        idFileMap = new HashMap<Integer, FileInfo>();
    }

    public void close() {}

    public void start() {
        (new Thread(this::connectionHandler)).start();
    }

    public void getListOfAvailableFiles(Connection connection) {
        connection.sendListOfAvailableFiles(availableFiles.size(), availableFiles);
    }

    public void upload(Connection connection) {
        if (connection.getClient() == null) {
            connection.sendInt(-1);
        }
        String name = connection.readString();
        int size = connection.readInt();

        if (name == null || size == -1) {
            connection.sendInt(-1);
            return;
        }
        idFileMap.put(maxID, new FileInfo(name, size, maxID));
        idFileMap.get(maxID).addClient(connection.getClient());
        availableFiles.add(idFileMap.get(maxID));
        connection.sendInt(maxID++);
    }

    public void getSources(Connection connection) {
        int fileID = connection.readInt();
        if (fileID == -1) {
            connection.sendSources(new HashSet<>());
            return;
        }
        if (!idFileMap.containsKey(fileID)) {
            connection.sendSources(new HashSet<>());
            return;
        }
        FileInfo fileInfo = idFileMap.get(fileID);
        connection.sendSources(fileInfo.getClients());
    }

    public void update(Connection connection) {
        connection.update();
        if (connection.getClient() == null) {
            short port = connection.readShort();
            if (port == -1) {
                connection.sendBoolean(false);
                return;
            }
            byte[] ip = getBytes(connection.getSocket().getRemoteSocketAddress().toString());
            Client curClient = new Client(ip, port);
            connection.setClient(curClient);
        }

        int countFiles = connection.readInt();
        if (countFiles == -1) {
            connection.sendBoolean(false);
        }

        ArrayList<Integer> ids = connection.readIDs(countFiles);
        for (int id: ids) {
            addClient(connection.getClient(), id);
        }
    }

    private void addClient(Client curClient, int idFile) {
        FileInfo currentFileInfo = idFileMap.get(idFile);
        for (Client client: currentFileInfo.getClients()) {
            if (client.equals(curClient)) {
                return;
            }
        }
        currentFileInfo.addClient(curClient);
    }

    private byte[] getBytes(String ip) {
        byte[] result = new byte[4];
        String[] parts = ip.split(".");
        for (int i = 0; i < 4; i++) {
            result[i] = (byte) ((int)parseIntOrNull(parts[i]));
        }
        return result;
    }

    private Integer parseIntOrNull(String a) {
        try {
            return Integer.parseInt(a);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void connectionHandler() {
        try (ServerSocket serverSocket = new ServerSocket(8081)) {
            while (!end) {
                Socket clientSocket = serverSocket.accept();
                (new Thread(() -> queryHandler(clientSocket))).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ConnectionTimerTask extends TimerTask {
        private Connection connection;

        ConnectionTimerTask(Connection curConnection) {
            connection = curConnection;
        }

        @Override
        public void run() {
            if (!connection.isUpdated()) {
                connection.close();
            }
            deleteClientFromFileInfo(connection);
            connection.resetIsUpdated();
        }
    }

    private void deleteClientFromFileInfo(Connection connection) {
        Client curClient = connection.getClient();
        for (FileInfo file: availableFiles) {
            file.getClients().remove(curClient);
        }
    }

    private void queryHandler(Socket socket) {
        Connection curConnection = new Connection(socket);
        Timer timer = new Timer();
        TimerTask checkConnection = new ConnectionTimerTask(curConnection);
        timer.schedule(checkConnection, TIME_OUT, TIME_OUT);
        while (!curConnection.isClosed()) {
            switch (curConnection.readQueryType()) {
                case Connection.LIST_QUERY:
                    getListOfAvailableFiles(curConnection);
                    break;
                case Connection.SOURCES_QUERY:
                    getSources(curConnection);
                    break;
                case Connection.UPDATE_QUERY:
                    update(curConnection);
                    break;
                case Connection.UPLOAD_QUERY:
                    upload(curConnection);
                    break;
            }
        }
        checkConnection.cancel();
        timer.cancel();
    }
}
