package ru.spbau.mit;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;

public class DirCheckSumTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private static int TEST_FILES_NUM = 4;

    @Before
    public void fillTempDir() throws IOException {
        for (int i = 0; i < TEST_FILES_NUM; i++) {
            File file;
            try {
                file = folder.newFile();
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
            byte[] a = {0};
            Files.write(file.toPath(), a);
        }
    }

    @Test
    public void testCheckSumOneThread() throws
            Exception {
        //fillTempDir();
        DirCheckSum checker = new DirCheckSum();
        byte[] oneThreadRes = checker.checkSumOneThread(folder.getRoot().toPath());
        byte[] threadPoolRes = checker.checkSumThreadPool(folder.getRoot().toPath());
        byte[] forkJoinRes = checker.checkSumForkJoin(folder.getRoot().toPath());
//        assertEquals(oneThreadRes, threadPoolRes);
        assertEquals(threadPoolRes, forkJoinRes);
    }
}