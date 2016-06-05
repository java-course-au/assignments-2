package ru.spbau.mit;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class Main {
    private static final int MILLIS = 1000;

    private Main() {
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Format: [path] [type = 0, 1 or 2]");
            return;
        }
        Path path = Paths.get(args[0]);
        int type = Integer.parseInt(args[1]);
        long startTime = System.currentTimeMillis();
        MD5Hasher hasher;
        switch (type) {
            case 0:
                hasher = new OneThreadMD5Hasher();
                break;
            case 1:
                hasher = new ExecutorServiceMD5Hasher();
                break;
            case 2:
                hasher = new ForkJoinMD5Hasher();
                break;
            default:
                throw new UnsupportedOperationException();
        }
        System.out.println(hasher.calculate(path));
        System.out.println((double) (System.currentTimeMillis() - startTime) / MILLIS);
    }
}
