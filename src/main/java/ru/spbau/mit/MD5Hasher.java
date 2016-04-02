package ru.spbau.mit;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Function;

public final class MD5Hasher {
    private MD5Hasher() {}

    private static String getMD5Hash(String path, Function<Path, String> directoryHasher) {
        Path p = Paths.get(path);
        if (!Files.exists(p)) {
            throw new InvalidPathException(path, "File doesn't exist");
        }
        if (Files.isRegularFile(p)) {
            try {
                return DigestUtils.md5Hex(new FileInputStream(p.toString()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (Files.isDirectory(p)) {
                return directoryHasher.apply(p);
        }
        return "";
    }

    public static String getSingleThreadMD5Hash(String path) {
        return getMD5Hash(path, (Path directoryPath) -> {
            StringBuilder builder = new StringBuilder(directoryPath.toString());
            try {
                Files.list(directoryPath).forEach(p -> {
                    builder.append(getSingleThreadMD5Hash(p.toString()));
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
            return DigestUtils.md5Hex(builder.toString());
        });
    }

    public static String getMultiThreadMD5Hash(String path) {
        return getMD5Hash(path, (Path directoryPath) -> {
            ExecutorService taskExecutor = Executors.newCachedThreadPool();
            List<Future> hashesList = new ArrayList<>();

            try {
                Files.list(directoryPath).forEach(p -> {
                    hashesList.add(taskExecutor.submit(() -> getMultiThreadMD5Hash(p.toString())));
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
            taskExecutor.shutdown();

            StringBuilder builder = new StringBuilder(directoryPath.toString());
            for (Future hash : hashesList) {
                try {
                    builder.append(hash.get());
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }

            return DigestUtils.md5Hex(builder.toString());
        });
    }

    public static String getForkJoinMD5Hash(String path) {
        return new ForkJoinPool().invoke(new ForkJoinHasher(path));
    }

    private static final class ForkJoinHasher extends RecursiveTask<String> {
        private String path;

        private ForkJoinHasher(String path) {
            this.path = path;
        }

        @Override
        public String compute() {
            return getMD5Hash(path, (Path directoryPath) -> {
                List<RecursiveTask> tasksList = new ArrayList<>();

                try {
                    Files.list(directoryPath).forEach(p -> {
                        RecursiveTask task = new ForkJoinHasher(p.toString());
                        task.fork();
                        tasksList.add(task);
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }

                StringBuilder builder = new StringBuilder(directoryPath.toString());
                for (RecursiveTask task : tasksList) {
                        builder.append(task.join());
                }
                return DigestUtils.md5Hex(builder.toString());
            });
        }
    }
}
