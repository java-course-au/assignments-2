package ru.spbau.mit;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static ru.spbau.mit.SecondPartTasks.*;

public class SecondPartTasksTest {

    @Test
    public void testFindQuotes() {
        List<String> answersFirst = Arrays.asList(
                "abacaba",
                "abacabadabacaba",
                "acacaba //there is a small typo in 'abacaba' word",
                " abacaba ");

        List<String> answersSecond = Arrays.asList(
                "Not a string from ABBA's 'money' song",
                "Money, money, money",
                "Money, money, money",
                "If I had a little money",
                "Money, money, money",
                "Money, money, money"
        );

        List<String> paths = Arrays.asList("src/test/resources/abacabas.txt", "src/test/resources/abba.txt");
        assertEquals(answersFirst, findQuotes(paths, "abacaba"));
        assertEquals(answersSecond, findQuotes(paths, "money"));
        assertEquals(new ArrayList<String>(), findQuotes(new ArrayList<>(), ""));
    }

    @Test
    public void testPiDividedBy4() {
        assertEquals(Math.PI / 4, piDividedBy4(), 1e-4);
    }

    @Test
    public void testFindPrinter() {
        Map<String, List<String>> compositions = new HashMap<>();
        compositions.put("abc", Arrays.asList("abc", "de", "fgh"));
        compositions.put("xyz", Arrays.asList("abc", "defgh"));
        compositions.put("answer", Arrays.asList("abcdefghi"));
        assertEquals("answer", findPrinter(compositions));
        assertEquals(null, findPrinter(new HashMap<>()));
        compositions.put("newAnswer", Arrays.asList("", "", "", "", "a", "veryLongString"));
        assertEquals("newAnswer", findPrinter(compositions));
    }

    @Test
    public void testCalculateGlobalOrder() {
        Map<String, Integer> firstMap = new HashMap<>(), secondMap = new HashMap<>(), thirdMap = new HashMap<>();
        firstMap.put("banana", 3);
        firstMap.put("apple", 5);
        firstMap.put("mushrooms", 239);

        secondMap.put("banana", 123);
        secondMap.put("apple", 1);

        thirdMap.put("mushrooms", 1);
        thirdMap.put("test", 10);

        Map<String, Integer> answer1 = new HashMap<>();
        answer1.put("banana", 123 + 3);
        answer1.put("apple", 5 + 1);
        answer1.put("mushrooms", 1 + 239);
        answer1.put("test", 10);

        assertEquals(answer1, calculateGlobalOrder(Arrays.asList(firstMap, secondMap, thirdMap)));
        assertEquals(firstMap, calculateGlobalOrder(Arrays.asList(firstMap)));
        assertEquals(new HashMap<>(), calculateGlobalOrder(Arrays.asList(new HashMap<String, Integer>())));
    }
}