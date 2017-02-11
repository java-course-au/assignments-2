package ru.spbau.mit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Collectors;

public class DirHasherForkJoin extends DirHasher {
    private ForkJoinPool pool = new ForkJoinPool();

    @Override
    public String calculateMd5(Path path) throws Exception {
        return pool.invoke(new Task(path));
    }

    private class Task extends RecursiveTask<String> {
        private Path path;

        Task(Path path) {
            this.path = path;
        }

        @Override
        protected String compute() {
            if (!Files.isReadable(path)) {
                throw new RuntimeException("path ".join(path.toString(), " is not readable"));
            }

            try {
                if (Files.isRegularFile(path)) {
                    return Hasher.md5Hash(path);
                }

                if (Files.isDirectory(path)) {
                    List<ForkJoinTask<String>> futures = getDirList(path).
                            map(Task::new)
                            .peek(Task::fork).collect(Collectors.toList());

                    return Hasher.md5Hash(path.toString()
                            + futures.stream().map(x -> x.join())
                                    .collect(Collectors.joining()));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            throw new RuntimeException("skipped  ".join(path.toString()));
        }

    }

}
