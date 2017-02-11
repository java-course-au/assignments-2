package ru.spbau.mit;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class PathHasherTest {
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();
    private final PathHasher hasher;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {new SingleThreadedPathHasher()},
                {new ExecutorServicePathHasher(EXECUTOR_SERVICE)},
                {new ForkJoinPathHasher(ForkJoinPool.commonPool())}
        });
    }

    public PathHasherTest(PathHasher hasher) {
        this.hasher = hasher;
    }

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    private static final byte[] FILE1_CONTENTS = "This is\nfile1.txt".getBytes();
    private static final String FILE1_MD5 = DigestUtils.md5Hex(FILE1_CONTENTS);

    private static final byte[] FILE2_CONTENTS = "This is\nfile2.txt".getBytes();
    private static final String FILE2_MD5 = DigestUtils.md5Hex(FILE2_CONTENTS);

    private static final byte[] AFILE_CONTENTS = "This is\nafile.txt".getBytes();
    private static final String AFILE_MD5 = DigestUtils.md5Hex(AFILE_CONTENTS);

    private static final byte[] ZFILE_CONTENTS = "This is\nzfile.txt".getBytes();
    private static final String ZFILE_MD5 = DigestUtils.md5Hex(ZFILE_CONTENTS);

    private static final String DIR1_NAME = "some-dir";
    private static final String DIR1_MD5 = DigestUtils.md5Hex(DIR1_NAME + FILE1_MD5 + FILE2_MD5);

    private static final String DIR2_NAME = "some-super-dir";
    private static final String DIR2_MD5 = DigestUtils.md5Hex(DIR2_NAME + AFILE_MD5 + DIR1_MD5 + ZFILE_MD5);

    @Test
    public void testFile() throws IOException {
        Path file1 = folder.newFile("file1.txt").toPath();
        Files.write(file1, FILE1_CONTENTS);
        assertEquals(FILE1_MD5, hasher.calculate(file1));
    }

    @Test
    public void testDirectory() throws IOException {
        Path dir1 = folder.newFolder(DIR1_NAME).toPath();
        Files.write(dir1.resolve("file1.txt"), FILE1_CONTENTS);
        Files.write(dir1.resolve("file2.txt"), FILE2_CONTENTS);
        assertEquals(DIR1_MD5, hasher.calculate(dir1));
    }

    @Test
    public void testSubdirectory() throws IOException {
        Path dir2 = folder.newFolder(DIR2_NAME).toPath();

        Path dir1 = dir2.resolve(DIR1_NAME);
        Files.createDirectory(dir1);
        Files.write(dir1.resolve("file1.txt"), FILE1_CONTENTS);
        Files.write(dir1.resolve("file2.txt"), FILE2_CONTENTS);

        Files.write(dir2.resolve("afile.txt"), AFILE_CONTENTS);
        Files.write(dir2.resolve("zfile.txt"), ZFILE_CONTENTS);
        assertEquals(DIR2_MD5, hasher.calculate(dir2));
    }
}
