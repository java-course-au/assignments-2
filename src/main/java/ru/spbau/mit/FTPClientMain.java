package ru.spbau.mit;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;

/**
 * Created by ldvsoft on 08.03.16.
 */
public abstract class FTPClientMain {
    private static final Map<String, Integer> ACTIONS = new Hashtable<>();
    private static final int ARG_HOST = 0;
    private static final int ARG_PORT = 1;
    private static final int ARG_ACTION = 2;
    private static final int ARG_PATH = 3;
    private static final int ARG_OUTPUT = 4;

    static {
        ACTIONS.put("list", FTPConnection.FTP_ACTION_LIST);
        ACTIONS.put("get", FTPConnection.FTP_ACTION_GET);
    }

    public static void main(String[] args) {
        if (args.length < ARG_ACTION + 1) {
            System.err.printf("Not enough arguments to connect to server.\n");
            helpAndHalt();
        }

        String host = args[ARG_HOST];
        int port;
        String portString = args[ARG_PORT];
        String actionString = args[ARG_ACTION];
        try {
            port = Integer.parseInt(portString);
        } catch (NumberFormatException ignored) {
            System.err.printf("Wrong port: \"%s\".\n", portString);
            helpAndHalt();
            return;
        }
        if (!ACTIONS.containsKey(actionString)) {
            System.err.printf("Wrong action: '%s'.\n", actionString);
            helpAndHalt();
            return;
        }
        int action = ACTIONS.get(actionString);

        try (FTPClient client = new FTPClient(host, port)) {
            String path, outputFile;
            switch (action) {
                case FTPConnection.FTP_ACTION_LIST:
                    if (args.length < ARG_PATH + 1) {
                        System.err.printf("No path.\n");
                        helpAndHalt();
                    }
                    path = args[ARG_PATH];
                    client.list(path).stream().forEach(
                            e -> System.out.printf("%s %s\n", e.getFileName(), e.isDirectory() ? "DIR" : "")
                    );
                    break;
                case FTPConnection.FTP_ACTION_GET:
                    if (args.length < ARG_OUTPUT + 1) {
                        System.err.printf("No path or output file.\n");
                        helpAndHalt();
                    }
                    path = args[ARG_PATH];
                    outputFile = args[ARG_OUTPUT];
                    client.get(path, new FileOutputStream(outputFile));
                    break;
                default:
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void helpAndHalt() {
        System.err.printf(""
                        + "Usage:\n"
                        + "\tjava FTPClientMain.class <host> <port> <action> [params...]\n"
                        + "Available ACTIONS:\n"
        );
        ACTIONS.keySet()
                .forEach(action -> System.err.printf("\t%s\n", action));
        System.exit(1);
    }
}
