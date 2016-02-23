package ru.spbau.mit;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import static ru.spbau.mit.SecondPartTasks.*;

public class SecondPartTasksTest {

    @Test
    public void testFindQuotes() {
        try {
            Set<String> expected = new TreeSet<>(Files.lines(Paths.get("src/test/resources/testresfile.txt"))
                                                      .collect(Collectors.toList()));
            Set<String> result = new TreeSet<>(findQuotes(Arrays.asList("src/test/resources/testfile1.txt",
                                                                        "src/test/resources/testfile2.txt"),
                                                          "they"));

            assertEquals(expected, result);

            List<String> empty = findQuotes(Arrays.asList("src/test/resources/testfile1.txt",
                                                          "src/test/resources/testfile2.txt"),
                                            "happy");

            assertTrue(empty.isEmpty());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static final double EPS = 0.005;
    private static final double MULTIPLIER = 4.0;
    @Test
    public void testPiDividedBy4() {
        assertTrue(Math.abs(piDividedBy4() * MULTIPLIER - Math.PI) < EPS);
    }

    @Test
    public void testFindPrinter() {
        Map<String, List<String>> compositions = new HashMap<>();
        compositions.put("author0", Arrays.asList());
        assertEquals(findPrinter(compositions), "author0");
        compositions.put("author1", Arrays.asList("aaaaaaaaaaaaaaa"));
        assertEquals(findPrinter(compositions), "author1");
        compositions.put("autor2", Arrays.asList("aaaa", "bbb", "ccc"));
        assertEquals(findPrinter(compositions), "author1");
        compositions.put("author3", Arrays.asList("aaaaaa", "bbbb", "ccccc", "ddddd", "ee"));
        assertEquals(findPrinter(compositions), "author3");
        compositions.put("author4", Arrays.asList("aaaaaaaaaaaaaaaaaaaaaaaa"));
        assertEquals(findPrinter(compositions), "author4");
    }

    @Test
    public void testCalculateGlobalOrder() {
        assertEquals(calculateGlobalOrder(Arrays.asList(ORDER_0, ORDER_1, ORDER_2, ORDER_3)), GLOBAL_ORDER);
    }

    private static final Map<String, Integer> ORDER_0 = ImmutableMap.of(
            "Cabernet", 35,
            "Jamon", 10,
            "Ham", 100
    );

    private static final Map<String, Integer> ORDER_1 = ImmutableMap.of(
            "Cabernet", 115,
            "Foie Gras", 24,
            "Caviar", 58
    );

    private static final Map<String, Integer> ORDER_2 = ImmutableMap.of(
            "Cabernet", 200,
            "Jamon", 15,
            "Olives", 400,
            "Caviar", 66
    );

    private static final Map<String, Integer> ORDER_3 = ImmutableMap.of(
            "Buckwheat", 1_000_000,
            "Sausages", 10_000
    );

    private static final int TOTAL_CABERNET = 350;
    private static final int TOTAL_FOIE_GRAS = 24;
    private static final int TOTAL_JAMON = 25;
    private static final int TOTAL_OLIVES = 400;
    private static final int TOTAL_HAM = 100;
    private static final int TOTAL_CAVIAR = 124;
    private static final int TOTAL_BUCKWHEAT = 1_000_000;
    private static final int TOTAL_SAUSAGES = 10_000;
    private static final Map<String, Integer> GLOBAL_ORDER =
            new ImmutableMap.Builder<String, Integer>()
                    .put("Cabernet", TOTAL_CABERNET)
                    .put("Foie Gras", TOTAL_FOIE_GRAS)
                    .put("Jamon", TOTAL_JAMON)
                    .put("Olives", TOTAL_OLIVES)
                    .put("Ham", TOTAL_HAM)
                    .put("Caviar", TOTAL_CAVIAR)
                    .put("Buckwheat", TOTAL_BUCKWHEAT)
                    .put("Sausages", TOTAL_SAUSAGES)
                    .build();
}
