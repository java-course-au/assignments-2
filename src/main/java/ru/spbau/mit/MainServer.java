package ru.spbau.mit;

import java.io.IOException;

/**
 * Created by olga on 13.03.16.
 */
public final class MainServer {
    private MainServer() {}
    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            help();
        }
        int port = -1;
        try {
            port = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            help();
        }

        Server server = new Server(port);
        server.start();
    }

    private static void help() {
        System.err.print("args: <port:int>\n");
        System.exit(1);
    }
}

