package ru.spbau;

import org.apache.commons.codec.digest.DigestUtils;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Created by rebryk on 01/03/16.
 */

public class Main {
    private static final String path = "/Users/rebryk/test";

    public static void runHasher(final Hasher hasher) {
        long start = System.currentTimeMillis();
        try {
            System.out.println(hasher.getHash(new File(path)));
        } catch (Exception e) {
            System.out.println("FAIL!");
        }
        long end = System.currentTimeMillis();
        System.out.println("Worked " + (end - start) / 1000.0);
    }

    public static void main(String[] args) {
        System.out.println("SingleThread");
        runHasher(HashFactory.createSingleThreadHasher());
        System.out.println("");

        System.out.println("MultiThread");
        runHasher(HashFactory.createMultiThreadHasher());
        System.out.println("");

        System.out.println("ForkJoin");
        runHasher(HashFactory.createForkJoinHasher());
        System.out.println("");
    }
}
