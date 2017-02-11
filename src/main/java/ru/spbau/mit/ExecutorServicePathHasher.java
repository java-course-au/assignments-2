package ru.spbau.mit;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class ExecutorServicePathHasher extends PathHasher {
    private final ExecutorService executorService;

    public ExecutorServicePathHasher(ExecutorService executorService) {
        this.executorService = executorService;
    }

    private class Task implements Callable<String> {
        private final Path path;

        Task(Path path) {
            this.path = path;
        }

        @Override
        public String call() {
            if (Files.isRegularFile(path)) {
                return calculateFileHash(path);
            } else if (Files.isDirectory(path)) {
                try {
                    List<Future<String>> subs =
                            Files.list(path)
                                    .sorted()
                                    // Do not use Task::new because of
                                    // https://bugs.openjdk.java.net/browse/JDK-8044748
                                    .map(p -> new Task(p))
                                    .map(executorService::submit)
                                    .collect(Collectors.toList());
                    String dirDescription = path.getFileName().toString()
                            +
                            subs.stream()
                                    .map(f -> {
                                        try {
                                            return f.get();
                                        } catch (InterruptedException | ExecutionException e) {
                                            throw new RuntimeException(e);
                                        }
                                    })
                                    .collect(Collectors.joining());
                    return DigestUtils.md5Hex(dirDescription);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                throw new RuntimeException("Found something: neither file nor directory");
            }
        }
    }

    @Override
    public String calculate(Path path) {
        Task task = new Task(path);
        try {
            return executorService.submit(task).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
