package ru.spbau.mit;

import org.junit.Before;
import org.junit.Test;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static ru.spbau.mit.SecondPartTasks.*;

public class SecondPartTasksTest {
    private static final String FILE_NAME_1 = "1";
    private static final String FILE_NAME_2 = "2";
    private static final Path PATH_1 = Paths.get(".", FILE_NAME_1);
    private static final Path PATH_2 = Paths.get(".", FILE_NAME_2);
    private static final String SUB_STRING = "cat!";

    @Before
    public void deleteIfExists() throws IOException {
        Files.deleteIfExists(PATH_1);
        Files.deleteIfExists(PATH_2);
    }

    public String constructAns(String prefix, String suffix, List<String> answer) {
        String curLine = prefix + SUB_STRING + suffix;
        answer.add(curLine);
        return curLine;
    }

    public void writeToFile(DataOutputStream out, List<String> lines) throws IOException {
        for (String line: lines) {
            out.write(line.getBytes());
            out.write("\n".getBytes());
        }
    }

    @Test
    public void testFindQuotes() {
        List<String> rightAnswer = new ArrayList<>();
        List<String> answer;
        try (DataOutputStream dou1 = new DataOutputStream(new FileOutputStream(PATH_1.toFile()));
             DataOutputStream dou2 = new DataOutputStream(new FileOutputStream(PATH_2.toFile()))) {

            List<String> lines1 = Arrays.asList("abacaba", constructAns("aba", "caba", rightAnswer),
                    constructAns("", "", rightAnswer), "lala");
            writeToFile(dou1, lines1);
            List<String> lines2 = Arrays.asList("", "ba", "caca!", constructAns("", "", rightAnswer));
            writeToFile(dou2, lines2);
            answer = findQuotes(Arrays.asList(PATH_1.toString(), PATH_2.toString()), SUB_STRING);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        assertEquals(rightAnswer, answer);
    }

    @Test
    public void testPiDividedBy4() {
        final double eps = 1e-5;
        assertTrue((Math.PI / 4 - piDividedBy4()) < eps);
    }

    @Test
    public void testFindPrinter() {
        Map<String, List<String>> query1 = new HashMap<String, List<String>>() { {
            put("autor", Arrays.asList("a1", "a2"));
        } };
        assertEquals("autor", findPrinter(query1));
        Map<String, List<String>> query2 = new HashMap<String, List<String>>() { {
            put("autor", Arrays.asList("a1", "a2"));
            put("miss", Arrays.asList("simon", "black", "white"));
            put("alerto", Arrays.asList("myBeautifulWeather", "HowIWantTomorrow", "xyz"));
            put("mihaelo", Arrays.asList("Hello!"));
            put("lira", Arrays.asList("Cat and world, part2"));
        } };
        assertEquals("alerto", findPrinter(query2));
    }

    @Test
    public void testCalculateGlobalOrder() {
        Map<String, Integer> answer1 = new HashMap<String, Integer>() { {
            put("potato", 100);
        } };
        List<Map<String, Integer>> query1 = Arrays.asList(answer1);
        assertEquals(answer1, calculateGlobalOrder(query1));
        List<Map<String, Integer>> query2 = Arrays.asList(
                new HashMap<String, Integer>() { {
                    put("potato", 100);
                    put("cucumber", 1);
                    put("milk", 200);
                } },
                new HashMap<String, Integer>() { {
                    put("milk", 200);
                    put("cheese", 10);
                    put("potato", 1);
                } },
                new HashMap<String, Integer>() { {
                    put("milk", 1);
                    put("chocolate", 2);
                } },
                new HashMap<String, Integer>() { {
                    put("chocolate", 1);
                } }
        );
        Map<String, Integer> answer2 = new HashMap<String, Integer>() { {
            put("potato", 101);
            put("cucumber", 1);
            put("milk", 401);
            put("cheese", 10);
            put("chocolate", 3);
        } };
        assertEquals(calculateGlobalOrder(query2), answer2);
    }
}
