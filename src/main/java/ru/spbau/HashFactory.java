package ru.spbau;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.*;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

/**
 * Created by rebryk on 06/03/16.
 */

public class HashFactory {
    private HashFactory() { }

    public static Hasher createSingleThreadHasher() {
        return new HashSingleThread();
    }

    public static Hasher createMultiThreadHasher() {
        return new HashMultiThread();
    }

    public static  Hasher createForkJoinHasher() {
        return new HashForkJoin();
    }

    private static String calculateMD5(File file) {
        try (FileInputStream stream = new FileInputStream(file)) {
            return DigestUtils.md5Hex(stream);
        } catch (Exception e) {
            System.out.println("No such file or directory!");
            return "";
        }
    }

    private static class HashSingleThread implements Hasher {
        @Override
        public String getHash(File file) {
            if (file.isDirectory()) {
                if (file.listFiles() != null) {
                    String data = Arrays
                            .asList(file.listFiles())
                            .stream()
                            .map(x -> new HashSingleThread().getHash(x))
                            .collect(Collectors.joining());
                    return DigestUtils.md5Hex(data);
                }
            } else {
                return calculateMD5(file);
            }
            return "";
        }
    }

    private static class HashMultiThread implements Hasher {
        private static class HashMultiThreadTask implements Callable<String> {
            private final File file;

            public HashMultiThreadTask(final File file) {
                this.file = file;
            }

            @Override
            public String call() throws Exception {
                if (file.isFile()) {
                    return calculateMD5(file);
                }

                if (file.listFiles() == null) {
                    return "";
                }

                ExecutorService exec = Executors.newFixedThreadPool(3);

                ArrayList<Future<String>> results = Arrays.asList(file.listFiles())
                        .stream()
                        .map(x -> exec.submit(new HashMultiThreadTask(x)))
                        .collect(Collectors.toCollection(ArrayList<Future<String>>::new));

                String result = results
                        .stream()
                        .map(x -> {
                            try {
                                return x.get();
                            } catch (InterruptedException | ExecutionException e) {
                                return "";
                            }})
                        .collect(Collectors.joining());

                exec.shutdown();

                return DigestUtils.md5Hex(result);
            }
        }

        @Override
        public String getHash(File file) throws Exception {
            return new HashMultiThreadTask(file).call();
        }
    }

    private static class HashForkJoin implements Hasher {
        private static class HashForkJoinTask extends RecursiveTask<String> {
            private final File file;

            public HashForkJoinTask(final File file) {
                this.file = file;
            }

            @Override
            protected String compute() {
                if (!file.isDirectory()) {
                    return calculateMD5(file);
                }

                if (file.listFiles() == null) {
                    return "";
                }

                ArrayList<HashForkJoinTask> tasks = Arrays.asList(file.listFiles())
                        .stream()
                        .map(HashForkJoinTask::new)
                        .peek(HashForkJoinTask::fork)
                        .collect(Collectors.toCollection(ArrayList<HashForkJoinTask>::new));

                String result = tasks
                        .stream()
                        .map(HashForkJoinTask::join)
                        .collect(Collectors.joining());

                return DigestUtils.md5Hex(result);
            }
        }

        @Override
        public String getHash(File file) throws Exception {
            return new HashForkJoinTask(file).compute();
        }
    }

}
