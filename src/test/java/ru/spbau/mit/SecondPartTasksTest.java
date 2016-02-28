package ru.spbau.mit;

import org.junit.Test;

import java.lang.reflect.Array;
import java.util.*;

import static org.junit.Assert.assertEquals;

import static ru.spbau.mit.SecondPartTasks.*;

public class SecondPartTasksTest {
    @Test
    public void testFindQuotes() {
        List<String> correctAnswer = Arrays.asList("Some very very very find me very very very boring text...",
                "find me find me find me",
                "fifind meme",
                "Ok, here you are: find me.");
        List<String> answer = findQuotes(Arrays.asList("src/test/resources/example1.txt",
                        "src/test/resources/example0.txt"),
                "find me");
        Collections.sort(correctAnswer);
        Collections.sort(answer);
        assertEquals(correctAnswer, answer);
    }

    @Test
    public void testPiDividedBy4() {
        assertEquals(Math.PI / 4, piDividedBy4(), 0.001);
    }

    @Test
    public void testFindPrinter() {
        Map<String, List<String>> compositions = new HashMap<>();
        String pushkinWinterEvening =
                "The storm covers skies in darkness,\n" +
                "Spinning snowy whirlwinds tight,\n" +
                "Now it wails like a beast wildest,\n" +
                "Now it cries like a week child.";
        String pushkinWinterMorning =
                "Cold frost and sunshine: day of wonder!\n" +
                "But you, my friend, are still in slumber -\n" +
                "Wake up, my beauty, time belies.";
        String tolstoyWarAndPeace = "Anna Pavlovna's drawing room was gradually filling. " +
                "The highest Petersburg society was assembled there: " +
                "people differing widely in age and character but alike in the social circle to which they belonged. " +
                "Prince Vasili's daughter, the beautiful Helene, " +
                "came to take her father to the ambassador's entertainment; " +
                "she wore a ball dress and her badge as maid of honor.";
        String rowlingHarryPotter0 = "Mr. and Mrs. Dursley, ";
        String rowlingHarryPotter1 = "of number four, Privet Drive, ";
        String rowlingHarryPotter2 = "were proud to say that ";
        String rowlingHarryPotter3 = "they were perfectly normal, ";
        String rowlingHarryPotter4 = "thank you very much.";

        compositions.put("Pushkin", Arrays.asList(pushkinWinterEvening, pushkinWinterMorning));
        compositions.put("Tolstoy", Arrays.asList(tolstoyWarAndPeace));
        compositions.put("Rowling", Arrays.asList(rowlingHarryPotter0,
                rowlingHarryPotter1,
                rowlingHarryPotter2,
                rowlingHarryPotter3,
                rowlingHarryPotter4));
        compositions.put("Me", new ArrayList<>());
        assertEquals("Tolstoy", findPrinter(compositions));

        assertEquals(null, findPrinter(new HashMap<>()));
    }

    @Test
    public void testCalculateGlobalOrder() {
        Map<String, Integer> order0 = new HashMap<>();
        order0.put("apples", 50);
        order0.put("bananas", 10);

        Map<String, Integer> order1 = new HashMap<>();
        order1.put("apples", 150);
        order1.put("oranges", 100);
        order1.put("pears", 40);

        Map<String, Integer> order2 = new HashMap<>();
        order2.put("pears", 40);
        order2.put("bananas", 20);

        Map<String, Integer> globalOrder = new HashMap<>();
        globalOrder.put("apples", 50 + 150);
        globalOrder.put("bananas", 10 + 20);
        globalOrder.put("oranges", 100);
        globalOrder.put("pears", 40 + 40);

        Map<String, Integer> globalOrder12 = new HashMap<>();
        globalOrder12.put("apples", 150);
        globalOrder12.put("bananas", 20);
        globalOrder12.put("oranges", 100);
        globalOrder12.put("pears", 40 + 40);

        assertEquals(new HashMap<String, Integer>(), calculateGlobalOrder(new ArrayList<>()));
        assertEquals(order1, calculateGlobalOrder(Arrays.asList(order1)));
        assertEquals(globalOrder12, calculateGlobalOrder(Arrays.asList(order1, order2)));
        assertEquals(globalOrder, calculateGlobalOrder(Arrays.asList(order0, order1, order2)));
    }
}
