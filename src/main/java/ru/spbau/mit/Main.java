package ru.spbau.mit;

import java.io.IOException;
import java.nio.file.Paths;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Expected arguments: <port> <file-to-serve>");
            System.exit(1);
        }
        try {
            new FileServingServer(Integer.parseInt(args[0]), Paths.get(args[1])).run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
