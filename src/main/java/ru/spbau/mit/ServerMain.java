package ru.spbau.mit;

import java.io.IOException;

public final class ServerMain {
    private ServerMain() {
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            printUsageAndExit();
        }
        try {
            new Server(Integer.parseInt(args[0])).start();
        } catch (NumberFormatException e) {
            System.err.println("Wrong port format");
            printUsageAndExit();
        }
    }

    private static void printUsageAndExit() {
        System.out.println("Usage:");
        System.out.println("<port: int>");
        System.exit(0);
    }
}
