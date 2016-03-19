package ru.spbau.mit;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Created by olga on 13.03.16.
 */
public final class MainClient {
    private static final int HOST_ARG = 0;
    private static final int PORT_ARG = 1;
    private static final int QUERY_ARG = 2;
    private static final int PATH_ARG = 3;
    private static final int CNT_ARGS = 4;


    private MainClient() {
    }

    public static void main(String[] args) throws IOException {
        if (args.length < CNT_ARGS) {
            help();
        }
        String host = args[HOST_ARG];
        int port = -1;
        try {
            port = Integer.parseInt(args[PORT_ARG]);
        } catch (NumberFormatException e) {
            help();
        }

        String query = args[QUERY_ARG].toUpperCase();
        String path = args[PATH_ARG];

        Client client = new Client(host, port);

        if (Objects.equals(query, "GET")) {
            InputStream is = client.get(path);
            int val = is.read();
            while (val != -1) {
                System.out.print(((char) val));
                val = is.read();
            }
        } else if (Objects.equals(query, "LIST")) {
            ArrayList<Client.FileEntry> listOfFile = client.list(path);
            for (Client.FileEntry file : listOfFile) {
                System.out.print(file.getName() + " " + file.getDir().toString() + "\n");
            }
        } else {
            help();
        }
    }

    private static void help() {
        System.err.print("args: <host:String>, <port:int>, <GET/LIST:String>, <path:String>\n");
        System.exit(1);
    }
}
