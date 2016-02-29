package ru.spbau.mit;

import org.junit.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/*
Far over the misty mountains cold
To dungeons deep and caverns old
We must away ere break of day
To seek the pale enchanted gold.

The DWARVES of yore made mighty spells,
While hammers fell like ringing bells
In places deep, where dark things sleep,
In hollow halls beneath the fells.

For ancient king and elvish lord
There many a gleaming golden hoard
They shaped and wrought, and light they caught
To hide in gems on hilt of sword.

On silver necklaces they strung
The flowering stars, on crowns they hung
The dragon-fire, in twisted wire
They meshed the light of moon and sun.
 */

public class SecondPartTasksTest {

    private static final String[][] TEXTS = {
            {"Far over the misty mountains cold",
                    "To dungeons deep and caverns old",
                    "We must away ere break of day",
                    "To seek the pale enchanted gold."},

            {"The DWARVES of yore made mighty spells,",
                    "While hammers fell like ringing bells",
                    "In places deep, where dark things sleep,",
                    "In hollow halls beneath the fells."},

            {"For ancient king and elvish lord",
                    "There many a gleaming golden hoard",
                    "They shaped and wrought, and light they caught",
                    "To hide in gems on hilt of sword."},

            {"On silver necklaces they strung",
                    "The flowering stars, on crowns they hung",
                    "The dragon-fire, in twisted wire",
                    "They meshed the light of moon and sun."},

            {"The wind was on the withered heath,",
                    "but in the forest stirred no leaf:",
                    "there shadows lay be night or day,",
                    "and dark things silent crept beneath.",
                    "The wind came down from mountains cold,",
                    "and like a tide it roared and rolled;",
                    "the branches groaned, the forest moaned,",
                    "and leaves were laid upon the mould."}
    };
    private static final String TITLE_PREF = "secondPartTaskTestFile";
    private static final int NUM_TEXTS = 5;
    private static final ArrayList<String> PATHS = new ArrayList<>();
    private static final HashMap<String, List<String>> LIBRARY = new HashMap<>();
    private static final HashMap<String, Integer> ELVES = new HashMap<>();
    private static final HashMap<String, Integer> PEOPLE = new HashMap<>();
    private static final HashMap<String, Integer> DWARVES = new HashMap<>();
    private static final HashMap<String, Integer> HOBBITS = new HashMap<>();
    private static final ArrayList<Map<String, Integer>> ORDERS = new ArrayList<>();
    private static final HashMap<String, Integer> RESULT = new HashMap<>();

    static {
        // creating files for testing the first task
        for (int i = 0; i < NUM_TEXTS; i++) {
            String fileName = TITLE_PREF + Integer.toString(i);
            try {
                Files.write(Paths.get(fileName),
                        Arrays.asList(TEXTS[i]));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            PATHS.add(fileName);
        }

        // creating a map for testing the third task
        for (int i = 0; i < NUM_TEXTS; i++) {
            String authorName = TITLE_PREF + Integer.toString(i);
            LIBRARY.put(authorName, Arrays.asList(TEXTS[i]));
        }

        // creating maps for the forth test
        ELVES.put("palantir", 3);
        ELVES.put("bow", 10);
        ORDERS.add(ELVES);

        PEOPLE.put("palantir", 4);
        PEOPLE.put("bow", 10);
        PEOPLE.put("axe", 5);
        ORDERS.add(PEOPLE);

        DWARVES.put("axe", 5);
        ORDERS.add(DWARVES);

        HOBBITS.put("blade", 1);
        HOBBITS.put("bow", 10);
        ORDERS.add(HOBBITS);

        RESULT.put("palantir", 7);
        RESULT.put("blade", 1);
        RESULT.put("bow", 30);
        RESULT.put("axe", 10);
    }

    @Test
    public void testFindQuotes() {
        assertEquals(
                Arrays.asList("To seek the pale enchanted gold.", "In hollow halls beneath the fells."),
                SecondPartTasks.findQuotes(PATHS, "al")
        );
    }

    @Test
    public void testPiDividedBy4() {
        double actualResult = Math.PI / 4;
        double eps = 0.01;
        double probability = SecondPartTasks.piDividedBy4();
        assertTrue((probability < actualResult + eps) && (probability > actualResult - eps));
    }

    @Test
    public void testFindPrinter() {
        assertEquals(TITLE_PREF + "4", SecondPartTasks.findPrinter(LIBRARY));
    }

    @Test
    public void testCalculateGlobalOrder() {
        assertEquals(RESULT, SecondPartTasks.calculateGlobalOrder(ORDERS));
    }
}
