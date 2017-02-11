package ru.spbau.mit;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class DirHasherExecutorService extends DirHasher {
    public static final int NTHREADS = 50;
    private ExecutorService executor = Executors.newFixedThreadPool(NTHREADS);


    private class Task implements Callable<String> {
        private final Path path;

        Task(Path path) {
            this.path = path;
        }

        @Override
        public String call() throws IOException {
            if (!Files.isReadable(path)) {
                throw new IOException("path ".join(path.toString(), " is not readable"));
            }

            if (Files.isRegularFile(path)) {
                return Hasher.md5Hash(path);
            }

            if (Files.isDirectory(path)) {
                List<Future<String>> futures = getDirList(path)
                        .map(x -> executor.submit(new Task(path))).collect(
                                Collectors
                                        .toList());
                return Hasher.md5Hash(path.toString() + futures.stream().map(x -> {
                    try {
                        return x.get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                    return "";
                }).collect(Collectors.joining()));
            }
            throw new IOException("skipped  ".join(path.toString()));
        }
    }

    @Override
    public String calculateMd5(Path path) throws IOException, InterruptedException, ExecutionException {
        return executor.submit(new Task(path)).get();
    }
}
