package ru.spbau.mit;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.List;

public final class ClientMain {
    private ClientMain() {
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            printUsageAndExit();
        }
        final String host = args[1];
        int port = 0;
        try {
            port = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            printUsageAndExit();
        }

        Client client = new Client(host, port);

        switch (args[0]) {
            case "list":
                List<FileInfo> files = client.sendListQuery(args[3]);
                for (FileInfo file : files) {
                    System.out.println("File " + file.getName() + ", isDir: " + file.getIsDir());
                }
                break;
            case "get":
                byte[] buffer = IOUtils.toByteArray(client.sendGetQuery(args[3]));
                for (byte aBuffer : buffer) {
                    System.out.print((char) aBuffer);
                }
                System.out.println();
                break;
            default:
                printUsageAndExit();
        }
    }

    private static void printUsageAndExit() {
        System.out.println("Usage:");
        System.out.println("list <host: String> <port: int> <path: String>");
        System.out.println("get <host: String> <port: int> <path: String>");
        System.exit(0);
    }
}
