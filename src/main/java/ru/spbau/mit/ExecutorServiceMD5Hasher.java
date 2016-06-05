package ru.spbau.mit;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ExecutorServiceMD5Hasher implements MD5Hasher {
    private final ExecutorService taskExecutor = Executors.newCachedThreadPool();

    @Override
    public String calculate(Path path) {
        String result = null;
        try {
            result = taskExecutor.submit(() -> calculateRecursive(path)).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        taskExecutor.shutdown();
        return result;
    }

    private String calculateRecursive(Path path) throws IOException, ExecutionException, InterruptedException {
        if (Files.isDirectory(path)) {
            ArrayList<Future<String>> results = new ArrayList<>();
            Files.list(path).forEach((p) -> results.add(taskExecutor.submit(() -> calculateRecursive(p))));
            StringBuilder stringBuilder = new StringBuilder(DigestUtils.md5Hex(path.getFileName().toString()));
            for (Future<String> result : results) {
                stringBuilder.append(result.get());
            }
            return DigestUtils.md5Hex(stringBuilder.toString());
        } else if (Files.isRegularFile(path)) {
            return DigestUtils.md5Hex(Files.readAllBytes(path));
        }
        return "";
    }
}
