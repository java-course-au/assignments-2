package ru.spbau.mit;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public abstract class PathHasher {
    private static final MessageDigest DIGEST;

    static {
        try {
            DIGEST = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    protected static String calculateFileHash(Path path) {
        try (InputStream in = Files.newInputStream(path)) {
            return DigestUtils.md5Hex(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public abstract String calculate(Path path);
}
