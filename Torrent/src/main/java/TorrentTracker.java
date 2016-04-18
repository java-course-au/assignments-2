/**
 * Created by n_buga on 31.03.16.
 */

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TorrentTracker implements AutoCloseable {
    static public final int TIME_OUT_SCHEDULE =  20;

    private Set<TrackerFileInfo> availableFiles;
    private Map<Integer, TrackerFileInfo> idFileMap;
    private int maxID = 0;
    private boolean end = false;
    private Thread serverThread;
    private final int TORRENT_PORT = 8081;
    private final int TIME_OUT_ACCEPT = 500;

    public TorrentTracker() {
        availableFiles = new HashSet<>();
        idFileMap = new HashMap<>();
    }

    public void close() {
        end = true;
        try {
            serverThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        serverThread = new Thread(this::connectionHandler);
        serverThread.start();
    }

    public void getListOfAvailableFiles(Connection connection) {
        try {
            connection.sendListOfAvailableFiles(availableFiles.size(), availableFiles);
        } catch (IOException e) {
            e.printStackTrace();
            connection.close();
        }
    }

    public void upload(Connection connection) {
        if (connection.getClientInfo() == null) {
            try {
                connection.sendInt(-1);
            } catch (IOException e) {
                System.out.println("ClientInfo = null");
                e.printStackTrace();
                connection.close();
            }
        }
        String name = connection.readString();
        long size = connection.readLong();

        if (name == null || size == -1) {
            try {
                connection.sendInt(-1);
            } catch (IOException e) {
                e.printStackTrace();
                connection.close();
            }
            return;
        }
        idFileMap.put(maxID, new TrackerFileInfo(name, size, maxID));
        idFileMap.get(maxID).addClient(connection.getClientInfo());
        availableFiles.add(idFileMap.get(maxID));
        try {
            connection.sendInt(maxID++);
        } catch (IOException e) {
            e.printStackTrace();
            connection.close();
        }
    }

    public void getSources(Connection connection) {
        int fileID = connection.readInt();
        if (fileID == -1) {
            try {
                connection.sendSources(new HashSet<>());
            } catch (IOException e) {
                e.printStackTrace();
                connection.close();
            }
            return;
        }
        if (!idFileMap.containsKey(fileID)) {
            try {
                connection.sendSources(new HashSet<>());
            } catch (IOException e) {
                e.printStackTrace();
                connection.close();
            }
            return;
        }
        TrackerFileInfo trackerFileInfo = idFileMap.get(fileID);
        try {
            connection.sendSources(trackerFileInfo.getClientInfos());
        } catch (IOException e) {
            e.printStackTrace();
            connection.close();
        }
    }

    public void update(Connection connection) {
        connection.update();

        int port = connection.readInt();
        byte[] ip = getBytes(connection.getSocket().getRemoteSocketAddress().toString());

        if (connection.getClientInfo() == null) {
            if (port == -1) {
                System.out.println("Can't read port");
                try {
                    connection.sendBoolean(false);
                } catch (IOException e) {
                    e.printStackTrace();
                    connection.close();
                }
                return;
            }
            ClientInfo curClientInfo = new ClientInfo(ip, port);
            connection.setClientInfo(curClientInfo);
        }

        int countFiles = connection.readInt();
        if (countFiles == -1) {
            System.out.print("can't read count of files");
            try {
                connection.sendBoolean(false);
            } catch (IOException e) {
                e.printStackTrace();
                connection.close();
            }
            return;
        }

        Set<Integer> ids = connection.readSet(countFiles);
        for (int id: ids) {
            addClient(connection.getClientInfo(), id);
        }
        try {
            connection.sendBoolean(true);
        } catch (IOException e) {
            e.printStackTrace();
            connection.close();
        }
    }

    public byte[] getBytes(String ip) {
        byte[] result = new byte[Connection.COUNT_IP_PARTS];
        String ipPart = ip.split(":")[0];
        String ipWhole = ipPart.split("/")[1];
        String[] parts = ipWhole.split("\\.");

        for (int i = 0; i < Connection.COUNT_IP_PARTS; i++) {
            result[i] = (byte) ((int) parseIntOrNull(parts[i]));
        }
        return result;
    }

    private void addClient(ClientInfo curClientInfo, int idFile) {
        TrackerFileInfo currentTrackerFileInfo = idFileMap.get(idFile);
        for (ClientInfo clientInfo : currentTrackerFileInfo.getClientInfos()) {
            if (clientInfo.myEquals(curClientInfo)) {
                return;
            }
        }
        currentTrackerFileInfo.addClient(curClientInfo);
    }

    private Integer parseIntOrNull(String a) {
        try {
            return Integer.parseInt(a);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void connectionHandler() {
        try (ServerSocket serverSocket = new ServerSocket(TORRENT_PORT)) {
            serverSocket.setSoTimeout(TIME_OUT_ACCEPT);
            while (!end) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    (new Thread(() -> queryHandler(clientSocket))).start();
                } catch (SocketTimeoutException ignored) {
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ConnectionTimerTask implements Runnable {
        private Connection connection;

        ConnectionTimerTask(Connection curConnection) {
            connection = curConnection;
        }

        @Override
        public void run() {
            if (!connection.isUpdated()) {
                System.out.print("We close you");
                connection.close();
            }
            connection.resetIsUpdated();
        }
    }

    private void deleteClientFromFileInfo(Connection connection) {
        ClientInfo curClientInfo = connection.getClientInfo();
        Set<TrackerFileInfo> filesForRemove = new HashSet<>();
        for (TrackerFileInfo file: availableFiles) {
            file.getClientInfos().remove(curClientInfo);
            if (file.getClientInfos().size() == 0) {
                filesForRemove.add(file);
            }
        }
        for (TrackerFileInfo file: filesForRemove) {
            availableFiles.remove(file);
        }
    }

    private void queryHandler(Socket socket) {
        try (Connection curConnection = new Connection(socket)) {
            ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
            scheduledExecutorService.scheduleAtFixedRate(new ConnectionTimerTask(curConnection),
                    TIME_OUT_SCHEDULE, TIME_OUT_SCHEDULE, TimeUnit.SECONDS);
            int k = 0;
            while (!curConnection.isClosed()) {
                int type = curConnection.readQueryType();
                switch (type) {
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
                    case Connection.END_CONNECTION:
                        curConnection.close();
                        break;
                    case Connection.EOF:
                        continue;
                    default:
                        System.out.print("Undefined query ");
                        System.out.println(type);
                }
            }
            scheduledExecutorService.shutdown();
            deleteClientFromFileInfo(curConnection);
        }
    }
}
