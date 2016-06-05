package ru.spbau.mit;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

public class OneThreadMD5Hasher implements MD5Hasher {
    @Override
    public String calculate(Path path) {
        try {
            if (Files.isDirectory(path)) {
                return DigestUtils.md5Hex(
                        Files.list(path).map(this::calculate).collect(
                                Collectors.joining("", DigestUtils.md5Hex(path.getFileName().toString()), "")
                        ));
            } else if (Files.isRegularFile(path)) {
                return DigestUtils.md5Hex(Files.readAllBytes(path));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}
