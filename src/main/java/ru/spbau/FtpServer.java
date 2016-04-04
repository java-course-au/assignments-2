package ru.spbau;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by rebryk on 10/03/16.
 */

public class FtpServer {
    private static final int QUERY_GET_LIST = 1;
    private static final int QUERY_GET_FILE = 2;

    private final int port;
    private ServerSocket serverSocket;
    private final ExecutorService threadPool = Executors.newCachedThreadPool();

    private Thread listeningThread;
    private boolean isRunning;

    public FtpServer(int port) {
        this.port = port;
        listeningThread = new Thread(new ListenHandler(this));
    }

    public void start() {
        isRunning = true;
        listeningThread.start();
    }

    public void stop() throws InterruptedException {
        isRunning = false;
        listeningThread.join();
        threadPool.shutdown();
    }

    private final class ListenHandler implements Runnable {
        private final FtpServer server;

        private ListenHandler(FtpServer server) {
            this.server = server;
        }

        @Override
        public void run() {
            try {
                server.serverSocket = new ServerSocket(server.port);
                server.serverSocket.setSoTimeout(1000);

                while (server.isRunning) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        threadPool.submit(new ClientHandler(clientSocket));
                    } catch (SocketTimeoutException e) {
                        // try again...
                    }
                }

                server.serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private final class ClientHandler implements Runnable {
        private static final int BUFFER_SIZE = 4096;

        private final Socket socket;

        private ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            DataInputStream inStream;
            DataOutputStream outStream;

            try {
                inStream = new DataInputStream(socket.getInputStream());
                outStream = new DataOutputStream(socket.getOutputStream());

                while (isRunning) {
                    try {
                        int type = inStream.readInt();
                        String path = inStream.readUTF();

                        if (type == QUERY_GET_LIST) {
                            getList(path, outStream);
                        } else if (type == QUERY_GET_FILE) {
                            getFile(path, outStream);
                        } else {
                            System.out.print("Unknown request!!!");
                        }
                    } catch (IOException e) {
                        // server closed
                    }
                }

                inStream.close();
                outStream.close();
                socket.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void getList(final String path, final DataOutputStream outStream) throws IOException {
            File dir = new File(path);
            if (dir.isFile()) {
                outStream.writeInt(0);
            } else {
                File[] files = dir.listFiles();
                if (files != null) {
                    outStream.writeInt(files.length);
                    Arrays.asList(files).stream().forEach(file -> {
                        try {
                            outStream.writeUTF(file.getName());
                            outStream.writeUTF(Boolean.toString(file.isDirectory()));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                }
            }
        }

        private void getFile(String path, DataOutputStream outStream) throws IOException {
            File file = new File(path);
            if (file.isDirectory()) {
                outStream.writeLong(0);
            } else {
                outStream.writeLong(file.length());
                BufferedInputStream fileContent = new BufferedInputStream(Files.newInputStream(file.toPath()));
                byte[] buffer = new byte[BUFFER_SIZE];
                while (fileContent.available() > 0) {
                    int len = fileContent.read(buffer, 0, BUFFER_SIZE);
                    outStream.write(buffer, 0, len);
                }
                outStream.flush();
            }
        }
    }
}
