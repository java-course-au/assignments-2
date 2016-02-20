package ru.spbau.mit;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class SecondPartTasksTest {

    @Test
    public void testFindQuotes() {
        List<String> answer1 = Arrays.asList(
                "test123",
                "test12",
                "test test test",
                "123 alalalala test",
                "         1 2 3 4 5 5 test"
        );

        List<String> answer2 = Arrays.asList(
                "test123",
                "123 alalalala test",
                "123 123 321"
        );

        List<String> paths = Arrays.asList(
                "src/test/resources/testFindQuotes-1.txt",
                "src/test/resources/testFindQuotes-2.txt"
        );

        assertEquals(answer1, SecondPartTasks.findQuotes(paths, "test"));
        assertEquals(answer2, SecondPartTasks.findQuotes(paths, "123"));
        assertEquals(Collections.emptyList(), SecondPartTasks.findQuotes(paths, "======"));
    }

    @Test
    public void testPiDividedBy4() {
        assertEquals(Math.PI / 4, SecondPartTasks.piDividedBy4(), 1e-4);
    }

    @Test
    public void testFindPrinter() {
        Map<String, List<String>> compositions = new HashMap<>();
        assertEquals(null, SecondPartTasks.findPrinter(compositions));

        compositions.put("0", Arrays.asList("", "", "", ""));
        assertEquals("0", SecondPartTasks.findPrinter(compositions));

        compositions.put("1", Arrays.asList("abc", "de", "fgh", "i"));
        compositions.put("2", Arrays.asList("abc", "def", "gh"));
        compositions.put("3", Arrays.asList("ab", "cd", "ef", "", "", "g", "hi", "klm"));
        assertEquals("3", SecondPartTasks.findPrinter(compositions));

        compositions.remove("3");
        assertEquals("1", SecondPartTasks.findPrinter(compositions));

        compositions.put("4", Arrays.asList("", "", "", "", "", "", "The red fox jumps over the lazy dog."));
        assertEquals("4", SecondPartTasks.findPrinter(compositions));

    }

    @Test
    public void testCalculateGlobalOrder() {
        String good1 = "banana";
        String good2 = "apple";
        String good3 = "gold";

        HashMap<String, Integer> order1 = new HashMap<>();
        order1.put(good1, 12);
        order1.put(good2, 42);
        order1.put(good3, 156);

        HashMap<String, Integer> order2 = new HashMap<>();
        order2.put(good3, 1);

        HashMap<String, Integer> order3 = new HashMap<>();
        order3.put(good1, 124);
        order3.put(good3, 99);

        HashMap<String, Integer> total = new HashMap<>();
        total.put(good1, 12 + 124);
        total.put(good2, 42);
        total.put(good3, 156 + 1 + 99);

        assertEquals(order1,
                SecondPartTasks.calculateGlobalOrder(Collections.singletonList(order1))
        );
        assertEquals(new HashMap<String, Integer>(),
                SecondPartTasks.calculateGlobalOrder(Collections.emptyList())
        );
        assertEquals(total,
                SecondPartTasks.calculateGlobalOrder(Arrays.asList(order1, order2, order3))
        );
    }
}