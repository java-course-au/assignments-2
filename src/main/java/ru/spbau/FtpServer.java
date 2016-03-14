package ru.spbau;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by rebryk on 10/03/16.
 */

public class FtpServer {
    private final int port;
    private ServerSocket serverSocket;

    private Thread listeningThread;
    private boolean isRunning;

    private List<Thread> threadList;

    public FtpServer(int port) {
        this.port = port;
        threadList = new ArrayList<>();
        listeningThread = new Thread(new ListenHandler(this));
    }

    public void start() {
        isRunning = true;
        listeningThread.start();
    }

    public void stop() {
        try {
            isRunning = false;
            listeningThread.join();
            threadList.forEach(Thread::interrupt);
            for (Thread thread: threadList) {
                thread.join();
            }
            System.out.println("FtpServer: stopped.");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private class ListenHandler implements Runnable {
        private final FtpServer server;

        public ListenHandler(FtpServer server) {
            this.server = server;
        }

        @Override
        public void run() {
            try {
                server.serverSocket = new ServerSocket(server.port);
                server.serverSocket.setSoTimeout(1000);
                System.out.println("ListenHandler: serverSocket was opened.");

                while (server.isRunning) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        System.out.println("ListenHandler: socket was accepted.");
                        Thread thread = new Thread(new ClientHandler(server, clientSocket));
                        server.threadList.add(thread);
                        thread.start();
                    } catch (SocketTimeoutException e) {
                        // try again...
                    }
                }

                server.serverSocket.close();
                System.out.println("ListenHandler: serverSocket was closed.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class ClientHandler implements Runnable {
        private static final int BUFFER_SIZE = 4096;

        private final FtpServer server;
        private final Socket socket;

        public ClientHandler(FtpServer server, Socket socket) {
            this.server = server;
            this.socket = socket;
        }

        private void getList(final String path, final DataOutputStream outStream) {
            try {
                File dir = new File(path);
                if (dir.isFile()) {
                    outStream.writeUTF("Error: path isn't directory!");
                } else {
                    outStream.writeUTF("Ok");
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
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void getFile(String path, DataOutputStream outStream) {
            try {
                File file = new File(path);
                if (file.isDirectory()) {
                    outStream.writeUTF("Error: file is directory!");
                } else {
                    outStream.writeUTF("Ok");

                    long packetsCount = file.length() / BUFFER_SIZE;
                    if (packetsCount * BUFFER_SIZE < file.length()) {
                        ++packetsCount;
                    }

                    outStream.writeLong(packetsCount);

                    InputStream in = new FileInputStream(file);
                    byte[] buffer = new byte[BUFFER_SIZE];
                    int read;
                    while ((read = in.read(buffer)) != -1) {
                        outStream.write(buffer, 0, read);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            DataInputStream inStream;
            DataOutputStream outStream;

            try {
                inStream = new DataInputStream(socket.getInputStream());
                outStream = new DataOutputStream(socket.getOutputStream());

                while (server.isRunning) {
                    try {
                        while (server.isRunning && inStream.available() == 0) {
                            Thread.sleep(100);
                        }

                        if (server.isRunning) {
                            int type = inStream.readInt();
                            String path = inStream.readUTF();

                            if (type == 1) {
                                getList(path, outStream);
                            } else if (type == 2) {
                                getFile(path, outStream);
                            }
                        }
                    } catch (InterruptedException e) {
                        System.out.println("ClientHandler: interrupted!");
                    }
                }

                inStream.close();
                outStream.close();
                socket.close();

                System.out.println("ClientHandler: socket was closed.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
