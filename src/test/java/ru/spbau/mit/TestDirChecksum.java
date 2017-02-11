package ru.spbau.mit;

import org.junit.Test;

import static ru.spbau.mit.DirChecksum.*;

/**
 * Created by Сева on 01.03.2016.
 */
public class TestDirChecksum {
    private static final String PATH = "C:\\Users\\Seva\\Dropbox\\Programming\\Codeforces";
    @Test
    public void testOneThreadChecksum() {
        long startTime = System.currentTimeMillis();
        System.err.println("testOneThreadChecksum: checksum=" + getOneThreadChecksum(PATH));
        long usedTime = System.currentTimeMillis() - startTime;
        System.err.println("testOneThreadChecksum: spent " + usedTime);
    }

    @Test
    public void testMultiThreadChecksum() {
        long startTime = System.currentTimeMillis();
        System.err.println("testMultiThreadChecksum: checksum=" + getMultiThreadChecksum(PATH));
        long usedTime = System.currentTimeMillis() - startTime;
        System.err.println("testMultiThreadChecksum: spent " + usedTime);
    }

    @Test
    public void testForkJoinChecksum() {
        long startTime = System.currentTimeMillis();
        System.err.println("testForkJoinChecksum: checksum=" + getForkJoinChecksum(PATH));
        long usedTime = System.currentTimeMillis() - startTime;
        System.err.println("testForkJoinChecksum: spent " + usedTime);
    }

}
