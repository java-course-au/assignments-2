package ru.spbau.mit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

public class DirHasherOneThread extends DirHasher {
    public String calculateMd5(Path path) throws IOException {
        if (!Files.isReadable(path)) {
            throw new IOException("path ".join(path.toString(), " is not readable"));
        }

        if (Files.isRegularFile(path)) {
            return Hasher.md5Hash(path);
        }

        if (Files.isDirectory(path)) {
            return Hasher.md5Hash(path.toString()
                    + getDirList(path).map(x -> {
                        try {
                            return calculateMd5(x);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return "";
                    }).collect(Collectors.joining()));
        }
        throw new IOException("skipped  ".join(path.toString()));
    }
}
