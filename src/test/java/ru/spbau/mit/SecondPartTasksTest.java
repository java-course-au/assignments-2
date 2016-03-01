package ru.spbau.mit;

import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static junitx.framework.Assert.assertEquals;

import static ru.spbau.mit.SecondPartTasks.*;

public class SecondPartTasksTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private File createFileWithContents(String content) throws IOException {
        File file = folder.newFile();
        PrintWriter printWriter = new PrintWriter(new FileWriter(file));
        printWriter.print(content);
        printWriter.close();
        return file;
    }

    @Test
    public void testFindQuotes() throws IOException {
        File file1 = createFileWithContents("abacaba\ndabacaba\nabac\n"),
                file2 = createFileWithContents("123123\nabac\ncabac\nabacabadabacaba\n");
        Assert.assertEquals(
                Arrays.asList("abacaba", "abacabadabacaba", "dabacaba")
                        .stream().sorted().collect(Collectors.toList()),
                findQuotes(Arrays.asList(file1.getAbsolutePath(), file2.getAbsolutePath()), "abacaba")
                        .stream().sorted().collect(Collectors.toList())
        );
        Assert.assertEquals(
                Arrays.asList("abacabadabacaba")
                        .stream().sorted().collect(Collectors.toList()),
                findQuotes(Arrays.asList(file1.getAbsolutePath(), file2.getAbsolutePath()), "bacabadabacaba")
                        .stream().sorted().collect(Collectors.toList())
        );
        Assert.assertEquals(
                Arrays.asList()
                        .stream().sorted().collect(Collectors.toList()),
                findQuotes(Arrays.asList(file1.getAbsolutePath(), file2.getAbsolutePath()), "baa")
                        .stream().sorted().collect(Collectors.toList())
        );
    }

    @Test
    public void testPiDividedBy4() {
        final double EPSILON = 1e-2;
        assertEquals(Math.PI / 4, piDividedBy4(), EPSILON);
    }

    @Test
    public void testFindPrinter() {
        Assert.assertEquals("Nikolay", findPrinter(ImmutableMap.of(
                "Tolstoy", Arrays.asList("123", "456"),
                "Lev", Arrays.asList("123", "456", "789"),
                "Nikolay", Arrays.asList("1234567890"))));
        Assert.assertEquals("Lev", findPrinter(ImmutableMap.of(
                "Tolstoy", Arrays.asList("123", "456"),
                "Lev", Arrays.asList("0123", "2456", "789"),
                "Nikolay", Arrays.asList("1234567890"))));
        Assert.assertEquals("Tolstoy", findPrinter(ImmutableMap.of(
                "Tolstoy", Arrays.asList("123", "456"))));
    }

    @Test
    public void testCalculateGlobalOrder() {
        Assert.assertEquals(ImmutableMap.of(
                "lamp", 49,
                "pizza", 3 + 1,
                "sock", 5 + 10,
                "pen", 12
                ),
                calculateGlobalOrder(Arrays.asList(
                        ImmutableMap.of(
                                "pizza", 3,
                                "sock", 5,
                                "pen", 12),
                        ImmutableMap.of(
                                "sock", 10,
                                "pizza", 1,
                                "lamp", 49)
                ))
        );
    }
}
