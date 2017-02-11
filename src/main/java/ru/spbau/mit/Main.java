package ru.spbau.mit;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java Main <path-to-hash> <hasher-to-use>");
            System.err.println("  <hasher-to-use> - one of: single-thread, executor-service, fork-join");
            System.exit(1);
        }

        ForkJoinPool pool = new ForkJoinPool();
        ExecutorService executorService = Executors.newCachedThreadPool();
        try {
            PathHasher hasher;
            switch (args[1]) {
                case "single-thread":
                    hasher = new SingleThreadedPathHasher();
                    break;
                case "executor-service":
                    hasher = new ExecutorServicePathHasher(executorService);
                    break;
                case "fork-join":
                    hasher = new ForkJoinPathHasher(pool);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown hasher: " + args[1]);
            }

            Path path = Paths.get(args[0]).toAbsolutePath();
            long startTime = System.currentTimeMillis();
            System.out.printf("%s %s\n", hasher.calculate(path), path);
            System.err.printf("Calculated in %d msec\n", System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            executorService.shutdown();
            pool.shutdown();
        }
    }
}
