package ru.spbau.mit;

import com.google.common.hash.Hashing;
import com.google.common.io.Files;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;

public final class Hasher {
    private Hasher() {}
    public static String md5Hash(Path file) throws IOException {
        return Files.hash(file.toFile(), Hashing.md5()).toString();
    }

    public static String md5Hash(String requestString) {
        return Hashing.md5().hashString(requestString, Charset.defaultCharset()).toString();
    }
}
