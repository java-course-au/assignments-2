package ru.spbau.mit;

import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static ru.spbau.mit.SecondPartTasks.*;

public class SecondPartTasksTest {

    @Test
    public void testFindQuotes() throws FileNotFoundException {
        List<String> fileName = new ArrayList<>(10);
        List<String> res = new ArrayList<>();
        String name = "file";
        for (int i = 0; i < 9; i++) {
            name += "a";
            PrintWriter pw = new PrintWriter(new File(name));
            pw.print("abacabadaba" + name + "\n");
            pw.print("abbb" + name + "\n");
            fileName.add(name);
            if (i > 0) {
                res.add("abbb" + name);
            }
            pw.close();
        }

        List<String> out = findQuotes(fileName, "bfileaa");

        assertEquals(res, out);
    }

    @Test
    public void testPiDividedBy4() {
        double pi4 = piDividedBy4();
        assertTrue(pi4 < 3.15 / 4);
        assertTrue(pi4 > 3.13 / 4);
    }

    @Test
    public void testFindPrinter() {

        List<String> songs1 = new ArrayList<String>();
        songs1.add("ab1");
        songs1.add("ab2");
        songs1.add("");
        songs1.add("ab3");

        List<String> songs2 = new ArrayList<String>();
        songs2.add("ab1");
        songs2.add("ab2");
        songs2.add("");
        songs2.add("abfjhdsf3");

        List<String> songs3 = new ArrayList<String>();
        songs3.add("ab1");
        songs3.add("ab2");
        songs3.add("fff");
        songs3.add("ab3");

        HashMap<String, List<String>> hm = new HashMap<>();
        hm.put("Alb1", songs1);
        hm.put("Alb2", songs2);
        hm.put("Alb3", songs3);

        assertEquals(findPrinter(hm), "Alb2");
    }

    @Test
    public void testCalculateGlobalOrder() {
        List<Map<String, Integer>> orders = new ArrayList<>(10);
        HashMap<String, Integer> res = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            orders.add(new HashMap<String, Integer>());
        }
        for (int i = 0; i < 10; i++) {
            putOrder(orders, res, i, "a", 10);
        }
        for (int i = 0; i < 5; i++) {
            putOrder(orders, res, i, "u", 11);
        }
        for (int i = 4; i < 10; i++) {
            putOrder(orders, res, i, "b", 1);
        }
        for (int i = 3; i < 7; i++) {
            putOrder(orders, res, i, "school", 179);
        }
        assertEquals(res, calculateGlobalOrder(orders));
    }

    private void putOrder(List<Map<String, Integer>> orders, HashMap<String, Integer> res,
                          int i, String key, int value) {
        orders.get(i).put(key, value);
        if (res.containsKey(key)) {
            res.put(key, res.get(key) + value);
        } else {
            res.put(key, value);
        }
    }
}
