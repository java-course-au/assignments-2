package ru.spbau.mit;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class SecondPartTasksTest {

    @Test
    public void testFindQuotes() {
        List<String> expected = Arrays.asList("abacaba", "abacabadabacaba aaaaaaa", "aabababacaba",
                "aabacaba", "abacabaabacabaabacaba abacabaabacabaabacaba", "zzzabacabazzza");
        Collections.sort(expected);
        List<String> actual = SecondPartTasks.findQuotes(Arrays.asList("src/test/resources/testFindQuotes00.txt", "src/test/resources/testFindQuotes01.txt"), "abacaba");
        Collections.sort(actual);
        assertEquals(expected, actual);
    }

    @Test
    public void testPiDividedBy4() {
        assertTrue(Math.abs(Math.PI / 4 - SecondPartTasks.piDividedBy4()) < 0.01);
    }

    @Test
    public void testFindPrinter() {
        Map<String, List<String>> compositions = new HashMap<>();
        assertEquals(null, SecondPartTasks.findPrinter(compositions));
        compositions.put("name1", Arrays.asList("aaaaa", "bbbbb", "ccccc"));
        assertEquals("name1", SecondPartTasks.findPrinter(compositions));
        compositions.put("name2", Arrays.asList("aaaaaaaaaaa", "ccccc"));
        assertEquals("name2", SecondPartTasks.findPrinter(compositions));
        compositions.put("name3", Arrays.asList("a", "b", ""));
        assertEquals("name2", SecondPartTasks.findPrinter(compositions));
        compositions.put("name3", Arrays.asList("aa", "aa", "aa", "aa", "aa", "aa", "aa", "aa", "aa", "aa", "aa"));
        assertEquals("name3", SecondPartTasks.findPrinter(compositions));
    }

    @Test
    public void testCalculateGlobalOrder() {
        List<Map<String, Integer>> orders = new ArrayList<>();
        assertEquals(new HashMap<String, Integer>(), SecondPartTasks.calculateGlobalOrder(orders));
        Map<String, Integer> order1 = new HashMap<>();
        order1.put("a", 10);
        orders.add(order1);
        assertEquals(order1, SecondPartTasks.calculateGlobalOrder(orders));
        Map<String, Integer> order2 = new HashMap<>();
        order2.put("a", 1);
        order2.put("b", 2);
        orders.add(order2);
        Map<String, Integer> orderSum1 = new HashMap<>();
        orderSum1.put("a", 11);
        orderSum1.put("b", 2);
        assertEquals(orderSum1, SecondPartTasks.calculateGlobalOrder(orders));
        Map<String, Integer> order3 = new HashMap<>();
        order3.put("a", 5);
        order3.put("c", 5);
        orders.add(order3);
        Map<String, Integer> orderSum2 = new HashMap<>();
        orderSum2.put("a", 16);
        orderSum2.put("b", 2);
        orderSum2.put("c", 5);
        assertEquals(orderSum2, SecondPartTasks.calculateGlobalOrder(orders));
    }
}