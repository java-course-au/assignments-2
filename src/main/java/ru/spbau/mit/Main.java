package ru.spbau.mit;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Supplier;

public abstract class Main {

    private static final int MILLISECONDS_IN_SECONDS = 1000;

    private static void run(Supplier<? extends FileHasher> fileHasherSupplier, Path path) {
        FileHasher fileHasher = fileHasherSupplier.get();
        System.out.println("-> FileHasher name: " + fileHasher.getClass().getName());
        long startTime = System.currentTimeMillis();
        System.out.println("Digest: " + fileHasher.getDigest(path));
        long endTime = System.currentTimeMillis();
        System.out.println("-> Time: " + (endTime - startTime) / MILLISECONDS_IN_SECONDS + "s");
        System.out.println();
    }

    public static void main(String[] args) {
        Path path = Paths.get(args[0]);

        run(FileHasherFactory::getSingleThreadFileHasher, path);
        run(FileHasherFactory::getThreadPoolFileHasher, path);
        run(FileHasherFactory::getForkJoinFileHasher, path);
    }
}
