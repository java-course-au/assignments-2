package ru.spbau.mit.simpleftp.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ru.spbau.mit.simpleftp.etc.ConfigParser;

public class MyFtpServer implements Runnable {
    public class MyFtpSharedComponents {
        public PrintStream log;
        public Path rootDir;
    }

    private static final int DEFAULT_PORT = 6666;
    private static final int THREADS_NUM_DEFAULT = 30;

    private static final ExecutorService DEFAULT_CONNECTION_EXECUTOR = Executors
            .newFixedThreadPool(THREADS_NUM_DEFAULT);

    private static final Path DEFAULT_ROOT_DIR = Paths.get("").toAbsolutePath();

    private int port;
    private MyFtpSharedComponents sharedComponents = new MyFtpSharedComponents();
    private ExecutorService connectionExecutor;

    private ServerSocket serverSocket;
    private BufferedReader controlInput;

    public MyFtpServer(Path confPath, PrintStream log, InputStream controlInput) {
        sharedComponents.log = log;
        this.controlInput = new BufferedReader(new InputStreamReader(controlInput));
        setupDefaults();
        if (confPath != null) {
            try {
                parseConfig(confPath);
            } catch (IOException e) {
                e.printStackTrace(sharedComponents.log);
                setupDefaults();
            }
        }
    }

    private void parseConfig(Path path) throws IOException {
        Properties properties = ConfigParser.parseConfig(path);
        if (properties.containsKey("port")) {
            port = Integer.parseInt(properties.getProperty("port"));
        }
        if (properties.containsKey("root_dir")) {
            sharedComponents.rootDir = Paths.get(properties.getProperty("root_dir"));
        }
        if (properties.containsKey("threads")) {
            connectionExecutor = Executors.newFixedThreadPool(Integer.parseInt(properties.getProperty("threads")));
        }
    }

    private void setupDefaults() {
        port = DEFAULT_PORT;
        connectionExecutor = DEFAULT_CONNECTION_EXECUTOR;
        sharedComponents.rootDir = DEFAULT_ROOT_DIR;
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace(sharedComponents.log);
            return;
        }
        sharedComponents.log.println("success open server socket");

        Thread childControlThread = new Thread(this);
        childControlThread.start();

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

        try {
            serverSocket.close();
            sharedComponents.log.println("success closed server socket");
            childControlThread.join();
        } catch (IOException e) {
            sharedComponents.log.println("failed close socket, now kill child using stop");
            childControlThread.interrupt();
        } catch (InterruptedException e) {
            sharedComponents.log.println("failed join child, now kill child using stop");
            childControlThread.interrupt();
            connectionExecutor.shutdownNow();
        }

        sharedComponents.log.println("OK");
    }

    @Override
    public void run() {
        sharedComponents.log.println("Child control thread is running now");
        while (!serverSocket.isClosed()) {
            Socket s;
            try {
                s = serverSocket.accept();
                connectionExecutor.execute(new MyFtpHander(s, sharedComponents));
            } catch (IOException e) {
                e.printStackTrace(sharedComponents.log);
                break;
            }
        }
        connectionExecutor.shutdownNow();
        sharedComponents.log.println("Child control thread have quited");
    }
}
