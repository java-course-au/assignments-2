package ru.spbau.mit;

import org.apache.commons.io.IOUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;

/**
 * Created by ldvsoft on 08.03.16.
 */
public abstract class FTPClientMain {
    private static final Map<String, Integer> REQUESTS = new Hashtable<>();
    private static final int ARG_HOST = 0;
    private static final int ARG_PORT = 1;
    private static final int ARG_ACTION = 2;
    private static final int ARG_PATH = 3;
    private static final int ARG_OUTPUT = 4;

    static {
        REQUESTS.put("list", FTPConnection.FTP_REQUEST_LIST);
        REQUESTS.put("get", FTPConnection.FTP_REQUEST_GET);
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
        if (!REQUESTS.containsKey(actionString)) {
            System.err.printf("Wrong request: '%s'.\n", actionString);
            helpAndHalt();
            return;
        }
        int request = REQUESTS.get(actionString);

        try (FTPClient client = new FTPClient(host, port)) {
            String path;
            String outputFile;
            switch (request) {
                case FTPConnection.FTP_REQUEST_LIST:
                    if (args.length < ARG_PATH + 1) {
                        System.err.printf("No path.\n");
                        helpAndHalt();
                    }
                    path = args[ARG_PATH];
                    client.list(path).stream().forEach(
                            e -> System.out.printf("%s %s\n", e.getFileName(), e.isDirectory() ? "DIR" : "")
                    );
                    break;
                case FTPConnection.FTP_REQUEST_GET:
                    if (args.length < ARG_OUTPUT + 1) {
                        System.err.printf("No path or output file.\n");
                        helpAndHalt();
                    }
                    path = args[ARG_PATH];
                    outputFile = args[ARG_OUTPUT];
                    IOUtils.copy(client.get(path), new FileOutputStream(outputFile));
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
                        + "Available requests:\n"
        );
        REQUESTS.keySet()
                .forEach(action -> System.err.printf("\t%s\n", action));
        System.exit(1);
    }
}
