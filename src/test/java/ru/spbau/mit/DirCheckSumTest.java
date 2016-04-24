package ru.spbau.mit;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.Assert.assertArrayEquals;

public class DirCheckSumTest {
    private static final int TEST_FILES_NUM = 4;
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private DirCheckSum checker;

    @Before
    public void fillTempDir() throws IOException {
        for (byte i = 0; i < TEST_FILES_NUM; i++) {
            File file;
            try {
                file = folder.newFile();
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
            byte[] a = {i};
            Files.write(file.toPath(), a);
        }
        checker = new DirCheckSum();
    }

    @Test
    public void testCheckSumOneThread() throws
            Exception {
        byte[] oneThreadRes = checker.checkSumOneThread(folder.getRoot().toPath());
        byte[] threadPoolRes = checker.checkSumThreadPool(folder.getRoot().toPath());
        byte[] forkJoinRes = checker.checkSumForkJoin(folder.getRoot().toPath());
        assertArrayEquals(oneThreadRes, threadPoolRes);
        assertArrayEquals(threadPoolRes, forkJoinRes);
        assertArrayEquals(forkJoinRes, oneThreadRes);
    }

    @Test
    public void testTimeCompare() throws Exception {
        checker.compareTime(folder.getRoot().toPath());

        // for
        //     checker.compareTime(Paths.get("/home/liza/term4/java/"));
        // the output was
        //     INFO: One thread time: 13298 Thread Pool time: 550 Fork-Join time: 992
    }
}
