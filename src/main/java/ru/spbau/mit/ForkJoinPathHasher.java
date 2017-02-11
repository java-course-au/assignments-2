package ru.spbau.mit;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Collectors;

public class ForkJoinPathHasher extends PathHasher {
    private final ForkJoinPool pool;

    public ForkJoinPathHasher(ForkJoinPool pool) {
        this.pool = pool;
    }

    private static class Task extends RecursiveTask<String> {
        private final Path path;

        Task(Path path) {
            this.path = path;
        }

        @Override
        protected String compute() {
            if (Files.isRegularFile(path)) {
                return calculateFileHash(path);
            } else if (Files.isDirectory(path)) {
                try {
                    List<RecursiveTask<String>> subs =
                            Files.list(path)
                                    .sorted()
                                    .map(Task::new)
                                    .peek(Task::fork)
                                    .collect(Collectors.toList());
                    String dirDescription = path.getFileName().toString()
                            +
                            subs.stream()
                                    .map(RecursiveTask::join)
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
        pool.submit(task);
        try {
            return task.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
