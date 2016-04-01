package ru.spbau.mit;

import java.io.IOException;

public abstract class FTPServerMain {
    private static final int ARG_PORT = 0;
    private static FTPServer ftpServer;

    public static void main(String[] args) {
        if (args.length < ARG_PORT + 1) {
            System.err.printf("Missing port.\n");
            helpAndHalt();
        }

        int port;
        String portString = args[ARG_PORT];
        try {
            port = Integer.parseInt(portString);
        } catch (NumberFormatException ignored) {
            System.err.printf("Wrong port: \"%s\".\n", portString);
            helpAndHalt();
            return;
        }

        try {
            ftpServer = new FTPServer(port);
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    ftpServer.close();
                }
            }));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void helpAndHalt() {
        System.err.printf(""
                        + "Usage:\n"
                        + "\tjava FTPServerMain.class <port>\n"
        );
        System.exit(1);
    }
}
