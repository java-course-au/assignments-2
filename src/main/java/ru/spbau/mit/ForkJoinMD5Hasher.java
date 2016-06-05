package ru.spbau.mit;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class ForkJoinMD5Hasher implements MD5Hasher {
    private final ExecutorService taskExecutor = Executors.newCachedThreadPool();

    @Override
    public String calculate(Path path) {
        return new ForkJoinPool().invoke(new MD5HasherTask(path));
    }

    private static class MD5HasherTask extends RecursiveTask<String> {
        private Path path;

        MD5HasherTask(Path path) {
            this.path = path;
        }

        @Override
        protected String compute() {
            try {
                if (Files.isRegularFile(path)) {
                    return DigestUtils.md5Hex(Files.readAllBytes(path));
                } else {
                    ArrayList<MD5HasherTask> subTasks = new ArrayList<>();
                    Files.list(path).forEach((p) -> {
                        MD5HasherTask task = new MD5HasherTask(p);
                        subTasks.add(task);
                        task.fork();
                    });
                    StringBuilder stringBuilder = new StringBuilder(
                            DigestUtils.md5Hex(path.getFileName().toString())
                    );
                    for (MD5HasherTask task : subTasks) {
                        stringBuilder.append(task.join());
                    }
                    return DigestUtils.md5Hex(stringBuilder.toString());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "";
        }
    }
}
