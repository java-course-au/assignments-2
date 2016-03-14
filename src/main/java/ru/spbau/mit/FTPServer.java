package ru.spbau.mit;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.logging.Logger;

public class FTPServer {

//    private static final int NOT_EXISTS = 0;
//    private Integer serverPort = 8080;
//    private ServerSocket serverSocket = null;
//    private boolean isStopped = false;
//    private Thread workThread = new Thread(() -> {
//        while (!isStopped()) {
//            Socket clientSocket = null;
//            try {
//                clientSocket = serverSocket.accept();
//            } catch (IOException e) {
//                if (isStopped()) {
//                    System.out.println("Server Stopped.");
//                    return;
//                }
//                throw new RuntimeException(
//                        "Error accepting client connection", e);
//            }
//            new Thread(
//                    new WorkerRunnable(
//                            clientSocket, "Multithreaded Server")
//            ).start();
//        }
//        System.out.println("Server Stopped.");
//    });
//
//    public static Integer SERVER_DEFAULT_PORT = 8080;
//    public FTPServer(int port) {
//        this.serverPort = port;
//    }
//
//
//    public void setUp() {
////            synchronized(this){
////                this.runningThread = Thread.currentThread();
////            }
//        openServerSocket();
//        workThread.start();
//    }
//
//    private synchronized boolean isStopped() {
//        return this.isStopped;
//    }
//
//    public synchronized void tearDown() {
//        this.isStopped = true;
//        try {
//            this.serverSocket.close();
//            workThread.join();
//        } catch (IOException e) {
//            throw new RuntimeException("Error closing server", e);
//        } catch (InterruptedException e) {
//            throw new RuntimeException("Error stopping work thread", e);
//        }
//    }
//
//    private void openServerSocket() {
//        try {
//            this.serverSocket = new ServerSocket(this.serverPort);
//        } catch (IOException e) {
//            throw new RuntimeException("Cannot open port " + serverPort.toString(), e);
//        }
//    }
//
//    private static class WorkerRunnable implements Runnable {
//
//        private static final int LIST = 1;
//        private static final int GET = 2;
//
//        private Socket clientSocket = null;
//        private String serverText = null;
//
//        public WorkerRunnable(Socket clientSocket, String serverText) {
//            this.clientSocket = clientSocket;
//            this.serverText = serverText;
//        }
//
//        public void run() {
//            try {
//                DataInputStream input = new DataInputStream(clientSocket.getInputStream());
//                DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());
//
//                doJob(input, output);
//
//                output.close();
//                input.close();
//            } catch (IOException e) {
//                //report exception somewhere.
//                e.printStackTrace();
//            }
//        }
//
//        private void doJob(DataInputStream input, DataOutputStream output) throws IOException {
//            int requestType = input.readInt();
//            String path = new BufferedReader(new InputStreamReader(input)).readLine();
//
//            if (requestType == LIST) {
//                list(output, path);
//            } else if (requestType == GET) {
//                get(output, path);
//            }
//        }
//    }

    public static final int SERVER_DEFAULT_PORT = 8080;
    public static final int LIST = 1;
    public static final int GET = 2;
    private static final int NOT_EXISTS = 0;
    private final ServerSocket serverSocket;
    private int port;
    private Thread workThread;

    public FTPServer (int portNumber) throws IOException {
        port = portNumber;
        serverSocket = new ServerSocket(port);
        workThread = new Thread(() -> {
            while(!Thread.interrupted()) {
                try {
                    processClient();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.err.println("One more happy client is successfully processed!");
                Logger.getAnonymousLogger().info("meow");
            }
            System.err.println("Tschuess!");
        });
        workThread.start();
    }

    private void processClient() throws IOException {
        Socket clientSocket = serverSocket.accept();

        DataInputStream input = new DataInputStream(clientSocket.getInputStream());
        DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());

        try {
            while (true) {
                int requestType = input.readInt();
                String path = input.readUTF();

                if (requestType == LIST) {
                    list(output, path);
                } else if (requestType == GET) {
                    get(output, path);
                }
            }
        } catch (IOException e) {
            clientSocket.shutdownInput();
            clientSocket.shutdownOutput();
            output.close();
            input.close();
            clientSocket.close();
        }
    }

    private static void list(DataOutputStream output, String path) throws IOException {
        Path filePath = Paths.get(path);
        if (Files.notExists(filePath)) {
            output.writeInt(NOT_EXISTS);
            output.flush();
            return;
        }

        File dir = new File(path);
        File[] files = dir.listFiles();

        output.writeInt(files.length);
        for (int i = 0; i < files.length; i++) {
            output.writeUTF(files[i].getPath());
            output.writeBoolean(files[i].isDirectory());
        }
        output.flush();
    }

    private static void get(DataOutputStream output, String path) throws IOException {
        Path filePath = Paths.get(path);
        if (Files.notExists(filePath)) {
            output.writeLong((long) NOT_EXISTS);
            output.flush();
            return;
        }

        output.writeLong(Files.size(filePath));
        output.write(Files.readAllBytes(filePath));
        output.flush();
    }

    public synchronized void tearDown () throws InterruptedException, IOException {
        workThread.interrupt();
        serverSocket.close();
        workThread.join();
    }
}

