package ru.spbau.mit.torrent.client;

import ru.spbau.mit.torrent.client.request.GetRequest;
import ru.spbau.mit.torrent.client.response.StatResponse;
import ru.spbau.mit.torrent.tracker.request.ListRequest;
import ru.spbau.mit.torrent.tracker.request.UpdateRequest;
import ru.spbau.mit.torrent.tracker.request.UploadRequest;
import ru.spbau.mit.torrent.tracker.response.ListResponse;
import ru.spbau.mit.torrent.tracker.response.SourcesResponse;
import ru.spbau.mit.torrent.tracker.response.UploadResponse;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

public class Client implements Runnable {
    private static final Long DEFAULT_PART_SIZE = 8192L;
    private static final int DEFAULT_PORT = 6666;
    private static final int DEFAULT_THREADS_NUM = 30;
    private static final InetSocketAddress DEFAULT_TRACKER_ADRESS = new InetSocketAddress("127.0.0.1", 8081);
    private static final Path DEFAULT_FILE_MANAGER_PROPS_PATH = Paths.get("src/test/resources/file_manager"
            + ".properties");
    private static final ExecutorService DEFAULT_CONNECTION_EXECUTOR = Executors
            .newFixedThreadPool(DEFAULT_THREADS_NUM);


    private SharedComponents sharedComponents = new SharedComponents();
    private int port;
    private InetSocketAddress trackerAdress = null;
    private ExecutorService connectionExecutor;

    private ServerSocket serverSocket;
    private BufferedReader controlInput;

    private Thread seedControlThread;
    private Thread updateControlThread;
    private Thread downloadControlThread;

    private boolean shuttingDown = false;
    private AtomicBoolean haveDownloadedAllFiles = new AtomicBoolean(false);

    public Client(PrintStream log, InputStream controlInput, Properties props) {
        sharedComponents.setLog(log);
        this.controlInput = new BufferedReader(new InputStreamReader(controlInput));
        setupDefaults();
        loadProps(props);
    }

    private void loadProps(Properties properties) {
        if (properties.containsKey("tracker_port")) {
            trackerAdress = new InetSocketAddress(trackerAdress.getAddress(), Integer.parseInt(properties
                    .getProperty("tracker_port")));
        }
        if (properties.containsKey("tracker_host")) {
            try {
                trackerAdress = new InetSocketAddress(InetAddress.getByName(properties.getProperty(
                        "tracker_host")),
                        trackerAdress.getPort());
            } catch (UnknownHostException e) {
                e.printStackTrace(sharedComponents.getLog());
            }
        }
        if (properties.containsKey("port")) {
            port = Integer.parseInt(properties.getProperty("port"));
        }
        if (properties.containsKey("file_manager_path")) {
            try {
                sharedComponents.setFilesManager(new FilesManager(Paths.get(properties.getProperty(
                        "file_manager_path")), DEFAULT_PART_SIZE));
            } catch (IOException e) {
                e.printStackTrace(sharedComponents.getLog());
            }
        }
        if (properties.containsKey("threads")) {
            connectionExecutor = Executors.newFixedThreadPool(Integer.parseInt(properties.getProperty("threads")));
        }
    }

    private void setupDefaults() {
        port = DEFAULT_PORT;
        try {
            sharedComponents.setFilesManager(new FilesManager(DEFAULT_FILE_MANAGER_PROPS_PATH, DEFAULT_PART_SIZE));
        } catch (IOException e) {
            e.printStackTrace(sharedComponents.log);
        }
        connectionExecutor = DEFAULT_CONNECTION_EXECUTOR;
        trackerAdress = DEFAULT_TRACKER_ADRESS;
    }

    private void closeAll() {
        shuttingDown = true;
        try {
            sharedComponents.getFilesManager().saveToDrive();
        } catch (IOException e) {
            e.printStackTrace(sharedComponents.getLog());
        }

        try {
            serverSocket.close();
            sharedComponents.log.println("success closed seed socket");
            seedControlThread.join();
        } catch (IOException e) {
            sharedComponents.log.println("failed close socket, now kill child using stop");
            seedControlThread.interrupt();
        } catch (InterruptedException e) {
            sharedComponents.log.println("failed join child, now kill child using stop");
            seedControlThread.interrupt();
            connectionExecutor.shutdownNow();
        }

        updateControlThread.interrupt();
        sharedComponents.log.println("OK");
    }

    public ListResponse listRequestToTracker() throws IOException {
        TrackerConnection connection = new TrackerConnection(trackerAdress);
        connection.getRequestWriter().writeRequest(new ListRequest());

        ListResponse response = connection.getResponseReader().nextListResponse();

        try {
            connection.closeAll();
        } catch (IOException e) {
            e.printStackTrace(sharedComponents.getLog());
        }

        return response;
    }

    public boolean haveAllFilesDownloaded() {
        return haveDownloadedAllFiles.get();
    }

    public UploadResponse uploadNewFileToTracker(Path path) throws IOException {
        TrackerConnection connection = new TrackerConnection(trackerAdress);

        UploadRequest request = new UploadRequest();
        request.setName(path.getFileName().toString());
        request.setSize(Files.size(path));

        connection.getRequestWriter().writeRequest(request);

        UploadResponse response = connection.getResponseReader().nextUploadResponse();

        try {
            connection.closeAll();
        } catch (IOException e) {
            e.printStackTrace(sharedComponents.getLog());
        }

        return response;
    }

    public SourcesResponse getFileSeeds(Integer id) {
        return new FileInfoSeedCollector(sharedComponents, trackerAdress, id).call();
    }

    public void addNewDistributionFile(Integer fileId, Path path) throws IOException {
        sharedComponents.getFilesManager().insertNewDistributedFile(fileId, path);
        sharedComponents.getFilesManager().saveToDrive();
    }

    public void removeDistribution(Integer fileId) throws IOException {
        sharedComponents.getFilesManager().removeDistributedFile(fileId);
        sharedComponents.getFilesManager().saveToDrive();
    }

    public void removeDistributionPart(Integer fileId, Integer partId) throws IOException {
        sharedComponents.getFilesManager().removeDistributedFileEntry(fileId, partId);
        sharedComponents.getFilesManager().saveToDrive();
    }

    public void addNewReadyToDownloadFile(Integer fileId, Long size, Path downloadPath) throws IOException {
        sharedComponents.getFilesManager().insertNewReadyToDownloadFile(fileId, new FilesManager
                .ReadyToDownloadFilesEntry(downloadPath, size));
        sharedComponents.getFilesManager().saveToDrive();
    }

    public StatResponse getFilePartsInfo(Integer fileId, InetSocketAddress seed) {
        return new FileInfoPartsCollector(seed, sharedComponents, fileId).call();
    }

    public void sendSeedInfoToTracker() {
        try {
            sharedComponents.log.println("updating info to tracker");
            TrackerConnection connection = new TrackerConnection(trackerAdress);
            UpdateRequest request = new UpdateRequest();
            request.setIds(sharedComponents.getFilesManager()
                    .getAvailableFileIds());
            request.setSeedPort((short) port);
            connection.getRequestWriter().writeRequest(request);
            connection.closeAll();
        } catch (IOException e) {
            e.printStackTrace(sharedComponents.log);
        }
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace(sharedComponents.log);
            return;
        }
        sharedComponents.log.println("success open tracker socket");

        seedControlThread = new Thread(new SeedControlRunnable());
        updateControlThread = new Thread(new TrackerUpdaterRunnable());
        downloadControlThread = new Thread(new DownloadManagerRunnable());

        seedControlThread.start();
        updateControlThread.start();
        downloadControlThread.start();

        while (true) {
            String command;
            try {
                command = controlInput.readLine();
                if (command == null) {
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace(sharedComponents.log);
                continue;
            }

            if (command.equals("quit")) {
                break;
            }
        }

        closeAll();
    }

    public class SharedComponents {

        private PrintStream log;
        private FilesManager filesManager;

        public FilesManager getFilesManager() {
            return filesManager;
        }

        public void setFilesManager(FilesManager filesManager) {
            this.filesManager = filesManager;
        }

        public PrintStream getLog() {
            return log;
        }

        public void setLog(PrintStream log) {
            this.log = log;
        }
    }


    private class DownloadManagerRunnable implements Runnable {
        private Map<Integer, FilesManager.ReadyToDownloadFilesEntry> readyToDownloadFiles;
        private Map<Integer, Future<?>> pendingSeedsInfoTasks = new HashMap<>();
        private Map<Integer, Map<InetSocketAddress, Future<StatResponse>>> pendingPartsInfoTasks = new HashMap<>();
        private Map<Integer, Map<Integer, Future<?>>> pendingDownloadTasks = new HashMap<>();
        private Map<Integer, Future<?>> pendingMergeTasks = new HashMap<>();

        private Path getPartPath(Path downloadPath, Integer partId) {
            return Paths.get(downloadPath.toString() + "_" + String.valueOf(partId) + ".part");
        }

        private void preparingAndSubmitSeedsInfoTasks() {
            readyToDownloadFiles = sharedComponents.getFilesManager()
                    .getReadyToDownloadFiles();
            sharedComponents.getFilesManager().cleanReadyToDownloadFiles();

            readyToDownloadFiles.entrySet().forEach(entry -> {
                int fileId = entry.getKey();
                pendingSeedsInfoTasks.put(fileId, connectionExecutor.submit(new FileInfoSeedCollector(
                        sharedComponents, trackerAdress, fileId)));
            });
        }

        private void submitDownloadTasks(int fileId, Map<Integer, InetSocketAddress> partsAdresses) {
            pendingDownloadTasks.put(fileId, new TreeMap<>());
            partsAdresses.entrySet().forEach(entry -> {
                int partId = entry.getKey();
                GetRequest getRequest = new GetRequest();
                getRequest.setId(fileId);
                getRequest.setPart(entry.getKey());
                pendingDownloadTasks.get(fileId).put(partId, connectionExecutor.submit(new PartDownloader(
                        getPartPath(readyToDownloadFiles.get(fileId).getDownloadPath(), partId),
                        sharedComponents.getFilesManager().getPartSize(readyToDownloadFiles.get(fileId), partId),
                        getRequest,
                        entry.getValue(),
                        sharedComponents)));
            });
        }

        private void submitPartsInfoTasks(int fileId, SourcesResponse sourcesResponse) {
            if (sourcesResponse != null) {
                pendingPartsInfoTasks.put(fileId, new HashMap<>());
                for (InetSocketAddress socketAddress : sourcesResponse.getSocketAddresses()) {
                    pendingPartsInfoTasks.get(fileId).put(socketAddress, connectionExecutor.submit(new
                            FileInfoPartsCollector(socketAddress, sharedComponents, fileId)));
                }
            }
        }

        private void getPartsInfoTasksAndSubmitDownloadTasks() {
            pendingPartsInfoTasks.entrySet().forEach(entry -> {
                int fileId = entry.getKey();
                Map<Integer, InetSocketAddress> partsAdresses = new HashMap<>();
                int needPartsSize = sharedComponents.getFilesManager().getFilePartsNum(readyToDownloadFiles.get(
                        fileId));
                for (Map.Entry<InetSocketAddress, Future<StatResponse>> addressesEntry
                        : entry.getValue().entrySet()) {
                    if (partsAdresses.size() == needPartsSize) {
                        break;
                    }
                    StatResponse response = null;
                    InetSocketAddress socketAddress = addressesEntry.getKey();
                    try {
                        response = addressesEntry.getValue().get();
                    } catch (Exception e) {
                        e.printStackTrace(sharedComponents.getLog());
                        continue;
                    }
                    for (Integer newPartId : response.getParts()) {
                        partsAdresses.put(newPartId, socketAddress);
                    }
                }

                if (partsAdresses.size() == needPartsSize) {
                    submitDownloadTasks(fileId, partsAdresses);
                }
            });
        }

        private void getSeedsInfoTasksAndSubmitPartsInfoTasks() {
            pendingSeedsInfoTasks.entrySet().forEach(entry -> {
                SourcesResponse sourcesResponse = null;
                int fileId = entry.getKey();
                try {
                    sourcesResponse = (SourcesResponse) entry.getValue().get();
                    submitPartsInfoTasks(fileId, sourcesResponse);
                } catch (Exception e) {
                    e.printStackTrace(sharedComponents.getLog());
                }
            });
        }

        private void downloadAllAndSubmitMergePartsTasks() {
            pendingDownloadTasks.entrySet().forEach(entry -> {
                int fileId = entry.getKey();
                List<Path> partsPaths = new ArrayList<>();
                entry.getValue().entrySet().forEach(partsEntry -> {
                    try {
                        partsEntry.getValue().get();
                        int partId = partsEntry.getKey();
                        partsPaths.add(getPartPath(
                                readyToDownloadFiles.get(fileId).getDownloadPath(), partId));
                        sharedComponents.getFilesManager().insertNewDistributedPart(fileId, partId,
                                getPartPath(
                                readyToDownloadFiles.get(fileId).getDownloadPath(), partId));
                    } catch (Exception e) {
                        e.printStackTrace(sharedComponents.getLog());
                    }
                });
                pendingMergeTasks.put(entry.getKey(), connectionExecutor.submit(new PartsMerger(
                        sharedComponents,
                        readyToDownloadFiles.get(fileId).getDownloadPath(),
                        partsPaths)));
            });
        }

        private void mergeAllAndSendComplitedToFilesManager() {
            pendingMergeTasks.entrySet().forEach(entry -> {
                try {
                    int fileId = entry.getKey();
                    entry.getValue().get();
                    for (int partId = 0; partId < sharedComponents.getFilesManager().getFilePartsNum(
                            readyToDownloadFiles.get(entry.getKey())); partId++) {
                        Path partPath = getPartPath(readyToDownloadFiles.get(fileId).getDownloadPath(),
                                partId);
                        sharedComponents.getFilesManager().removeDistributedFileEntry(fileId, partId);
                        Files.delete(partPath);
                    }
                    sharedComponents.getFilesManager().insertNewDistributedFile(fileId,
                            readyToDownloadFiles.get(fileId).getDownloadPath());
                } catch (Exception e) {
                    e.printStackTrace(sharedComponents.getLog());
                }
            });
        }

        @Override
        public void run() {
            preparingAndSubmitSeedsInfoTasks();
            getSeedsInfoTasksAndSubmitPartsInfoTasks();
            getPartsInfoTasksAndSubmitDownloadTasks();
            downloadAllAndSubmitMergePartsTasks();
            mergeAllAndSendComplitedToFilesManager();
            sharedComponents.log.println("All Files have downloaded");
            haveDownloadedAllFiles.set(true);
        }
    }

    private class TrackerUpdaterRunnable implements Runnable {
        @Override
        public void run() {
            final int sleepingTimeout = 30 * 1000;
            while (!shuttingDown) {
                sendSeedInfoToTracker();
                try {
                    Thread.sleep(sleepingTimeout);
                } catch (InterruptedException e) {
                    e.printStackTrace(sharedComponents.getLog());
                }
            }
        }
    }

    private class SeedControlRunnable implements Runnable {
        @Override
        public void run() {
            sharedComponents.log.println("Child control thread is running now");
            while (!serverSocket.isClosed() && !shuttingDown) {
                Socket s;
                try {
                    s = serverSocket.accept();
                    connectionExecutor.execute(new SeedHandler(s, sharedComponents));
                } catch (IOException e) {
                    e.printStackTrace(sharedComponents.log);
                    break;
                }
            }
            connectionExecutor.shutdownNow();
            sharedComponents.log.println("Child control thread have quited");
        }
    }
}
