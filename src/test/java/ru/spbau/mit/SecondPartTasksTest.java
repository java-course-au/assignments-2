package ru.spbau.mit;

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

    @Test
    public void testPiDividedBy4() {
        final double eps = 0.005;
        assertTrue(Math.abs(piDividedBy4() * 4 - Math.PI) < eps);
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
        Map<String, Integer> order0 = new HashMap<>();
        order0.put("Cabernet", 35);
        order0.put("Jamon", 10);
        order0.put("Ham", 100);

        Map<String, Integer> order1 = new HashMap<>();
        order1.put("Cabernet", 115);
        order1.put("Foie Gras", 24);
        order1.put("Caviar", 58);

        Map<String, Integer> order2 = new HashMap<>();
        order2.put("Cabernet", 200);
        order2.put("Jamon", 15);
        order2.put("Olives", 400);
        order2.put("Caviar", 66);

        Map<String, Integer> order3 = new HashMap<>();
        order3.put("Buckwheat", 1_000_000);
        order3.put("Sausages", 10_000);

        Map<String, Integer> globalOrder = new HashMap<>();
        globalOrder.put("Cabernet", 350);
        globalOrder.put("Foie Gras", 24);
        globalOrder.put("Jamon", 25);
        globalOrder.put("Olives", 400);
        globalOrder.put("Ham", 100);
        globalOrder.put("Caviar", 124);
        globalOrder.put("Buckwheat", 1_000_000);
        globalOrder.put("Sausages", 10_000);

        assertEquals(calculateGlobalOrder(Arrays.asList(order0, order1, order2, order3)), globalOrder);
    }
}