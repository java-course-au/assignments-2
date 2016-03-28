package ru.spbau.mit;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SimpleFtpServer {
    private final int port;
    private final Path rootPath;
    private Thread listenThread;
    private ExecutorService clientThreads;
    private ServerSocket serverSocket;

    public SimpleFtpServer(int port) throws IOException {
        this(port, Paths.get(""));
    }

    public SimpleFtpServer(int port, Path rootPath) {
        this.port = port;
        this.rootPath = rootPath;
    }

    public void start() throws IOException {
        if (serverSocket != null) {
            throw new IllegalStateException("Server was already started");
        }
        clientThreads = Executors.newCachedThreadPool();
        serverSocket = new ServerSocket(port);
        listenThread = new Thread(new ListenRunnable());
        listenThread.start();
    }

    private class ListenRunnable implements Runnable {
        @Override
        public void run() {
            while (!Thread.interrupted()) {
                Socket client;
                try {
                    client = serverSocket.accept();
                } catch (IOException e) {
                    break;
                }
                clientThreads.submit(new SimpleFtpClientHandler(rootPath, client));
            }
            try {
                if (!serverSocket.isClosed()) {
                    serverSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Stops accepting new connections and closes socket,
     * unclosed connections are still processed
     */
    public void shutdown() throws InterruptedException, IOException {
        if (serverSocket == null) {
            throw new IllegalStateException("Server was not started");
        }
        if (!serverSocket.isClosed()) {
            serverSocket.close();
        }
        listenThread.interrupt();
        listenThread.join();
        clientThreads.shutdown();
    }
}
