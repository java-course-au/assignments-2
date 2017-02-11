package ru.spbau.mit;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public abstract class DirHasher {
    abstract String calculateMd5(Path path) throws Exception;

    public Stream<Path> getDirList(Path path) throws IOException {
        return Files.
                list(path).
                sorted();
    }
}
