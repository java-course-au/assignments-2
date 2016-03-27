package ru.spbau.mit;

import org.junit.Test;

public class TestMD5Hasher {
    private static final double MILLISECONDS_TO_SECONDS = 0.001;
    private static final String PATH = "C:\\Users\\Никита\\Documents\\Programming\\";

    @Test
    public void testGetSingleThreadMD5Hash() {
        System.err.println("Singlethread:");
        long startTime = System.currentTimeMillis();
        System.err.println(MD5Hasher.getSingleThreadMD5Hash(PATH));
        long finishTime = System.currentTimeMillis();
        System.err.println((finishTime - startTime) * MILLISECONDS_TO_SECONDS);
    }

    @Test
    public void testGetMultiThreadMD5Hash() {
        System.err.println("Multithread:");
        long startTime = System.currentTimeMillis();
        System.err.println(MD5Hasher.getMultiThreadMD5Hash(PATH));
        long finishTime = System.currentTimeMillis();
        System.err.println((finishTime - startTime) * MILLISECONDS_TO_SECONDS);
    }

    @Test
    public void testForkJoinMD5Hash() {
        System.err.println("Fork-Join:");
        long startTime = System.currentTimeMillis();
        System.err.println(MD5Hasher.getForkJoinMD5Hash(PATH));
        long finishTime = System.currentTimeMillis();
        System.err.println((finishTime - startTime) * MILLISECONDS_TO_SECONDS);
    }
}
