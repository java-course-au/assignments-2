package ru.spbau.mit;

import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static junitx.framework.Assert.assertEquals;

public class SecondPartTasksTest {
    private static final String RES_DIR = "src/test/resources";

    private static List<String> getFiles() {
        try {
            return Files
                    .walk(Paths.get(RES_DIR))
                    .map(x -> x.toString())
                    .filter(x -> !x.equals(RES_DIR))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    private static List<String> cutAllFilesSorted(List<String> files) {
        return files.stream()
                .map(x -> Paths.get(x).getFileName().toString())
                .sorted()
                .collect(Collectors.toList());
    }

    @Test
    public void testFindQuotes() {
        final List<String> files = getFiles();
        assertEquals(Arrays.asList("Hello_world"),
                cutAllFilesSorted(SecondPartTasks.findQuotes(files, "random")));
        assertEquals(Arrays.asList("Day that never comes"),
                cutAllFilesSorted(SecondPartTasks.findQuotes(files, "yeaahh")));
        assertEquals(Arrays.asList("Day that never comes",
                "Hello_world"),
                cutAllFilesSorted(SecondPartTasks.findQuotes(files, "word")));
    }

    private static final double EPS = 0.01;

    @Test
    public void testPiDividedBy4() {
        final double res1 = Math.PI / 4;
        assertEquals(res1, SecondPartTasks.piDividedBy4(), EPS);
    }

    private static final List<String> PUSHKIN = Arrays.asList("aaaaaaaaaaaaaaaaaa", "bbbbbbbbbbbbbbbb", "cccccc");
    private static final List<String> LERMONTOV = Arrays.asList("a", "b", "c");
    private static final List<String> TWO_PAC = Arrays.asList("f*ck", "yeah",
            "biiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii***");

    private static final Map<String, List<String>> MAP_AUTHOR_RUSSIAN = new HashMap<String, List<String>>() {
        {
            put("PUSHKIN", PUSHKIN);
            put("LERMONTOV", LERMONTOV);
        }
    };

    private static final Map<String, List<String>> MAP_AUTHOR_ALL = new HashMap<String, List<String>>() {
        {
            put("PUSHKIN", PUSHKIN);
            put("LERMONTOV", LERMONTOV);
            put("2Pac", TWO_PAC);
        }
    };

    @Test
    public void testFindPrinter() {
        assertEquals("PUSHKIN", SecondPartTasks.findPrinter(MAP_AUTHOR_RUSSIAN));
        assertEquals("2Pac", SecondPartTasks.findPrinter(MAP_AUTHOR_ALL));
    }

    private static final Map<String, Integer> PYTEROCHKA = ImmutableMap.of(
            "SYR", 100,
            "MYASO", 100500,
            "VODKO", ((int) 3L)
    );

    private static final Map<String, Integer> MAGNIT = ImmutableMap.of(
            "SYR", 5,
            "MYASO", 100500,
            "VODKO", ((int) 10L),
            "VINE PREMIUM", ((int) 1L)
    );

    private static final Map<String, Integer> PRODUCTY_PLUS_MINUS = ImmutableMap.of(
            "OTSTOY", 100500
    );

    private static final List<Map<String, Integer>> PREMIUM_LIST = Arrays.asList(PYTEROCHKA, MAGNIT);
    private static final List<Map<String, Integer>> CHEAP_LIST =
            Arrays.asList(PYTEROCHKA, MAGNIT, PRODUCTY_PLUS_MINUS);


    @Test
    public void testCalculateGlobalOrder() {
        final Map<String, Integer> result1 = ImmutableMap.of(
            "OTSTOY", 100500,
            "VINE PREMIUM", ((int) 1L),
            "VODKO", ((int) 13L),
            "MYASO", 201000,
            "SYR", 105
        );
        Assert.assertEquals(result1, SecondPartTasks.calculateGlobalOrder(CHEAP_LIST));

        final Map<String, Integer> result2 = ImmutableMap.of(
                "VINE PREMIUM", ((int) 1L),
                "VODKO", ((int) 13L),
                "MYASO", 201000,
                "SYR", 105
        );
        Assert.assertEquals(result2, SecondPartTasks.calculateGlobalOrder(PREMIUM_LIST));
    }

}
