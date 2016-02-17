package ru.spbau.mit;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Array;
import java.nio.file.Paths;
import java.util.*;


import static org.junit.Assert.fail;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static junitx.framework.Assert.assertEquals;

import static ru.spbau.mit.SecondPartTasks.*;

public class SecondPartTasksTest {

    @Test
    public void testFindQuotes() {
        assertEquals(
                Arrays.asList("input01.txt", "input03.txt"),
                findQuotes(Arrays.asList("input01.txt", "input02.txt", "input03.txt"), "test"));
    }

    @Test
    public void testPiDividedBy4() {
        assertEquals(true, Math.abs(Math.PI / 4.0 - piDividedBy4()) < 1e-2);
    }

    @Test
    public void testFindPrinter() {
        Map<String, List<String>> data = new HashMap<>();
        data.put("Pyshkin", Arrays.asList("A", "B", "C", "D"));
        data.put("Gogol", Arrays.asList("AB", "C", "DE"));
        data.put("Shevchenko", Arrays.asList("ABC", "DE", "F"));
        assertEquals("Shevchenko", findPrinter(data));
    }

    @Test
    public void testCalculateGlobalOrder() {
        List<Map<String, Integer>> data = new ArrayList<Map<String, Integer>>();

        data.add(new HashMap<String, Integer>(){{
            put("Apples", 1);
            put("Oranges", 10);
            put("Pineapples", 7);
        }});

        data.add(new HashMap<String, Integer>(){{
            put("Apples", 3);
            put("Plums", 6);
            put("Pineapples", 3);
        }});

        data.add(new HashMap<String, Integer>(){{
            put("Melons", 13);
            put("Plums", 2);
            put("Pineapples", 10);
        }});

        Map<String, Integer> result = calculateGlobalOrder(data);
        assertEquals(4, (int)result.get("Apples"));
        assertEquals(10, (int)result.get("Oranges"));
        assertEquals(20, (int)result.get("Pineapples"));
        assertEquals(8, (int)result.get("Plums"));
        assertEquals(13, (int)result.get("Melons"));
        assertEquals(5, result.entrySet().size());
    }
}