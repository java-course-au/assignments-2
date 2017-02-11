package ru.spbau.mit;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

public class SingleThreadedPathHasher extends PathHasher {
    @Override
    public String calculate(Path path) {
        if (Files.isRegularFile(path)) {
            return calculateFileHash(path);
        } else if (Files.isDirectory(path)) {
            try {
                String dirDescription = path.getFileName().toString()
                        +
                        Files.list(path)
                                .sorted()
                                .map(this::calculate)
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
