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

The dwarves of yore made mighty spells,
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

    private static final String[][] texts = {
            {"Far over the misty mountains cold",
                    "To dungeons deep and caverns old",
                    "We must away ere break of day",
                    "To seek the pale enchanted gold."},

            {"The dwarves of yore made mighty spells,",
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
    private static final ArrayList<String> paths = new ArrayList<>();
    private static final HashMap<String, List<String>> library = new HashMap<>();
    private static final HashMap<String, Integer> elves = new HashMap<>();
    private static final HashMap<String, Integer> people = new HashMap<>();
    private static final HashMap<String, Integer> dwarves = new HashMap<>();
    private static final HashMap<String, Integer> hobbits = new HashMap<>();
    private static final ArrayList<Map<String, Integer>> orders = new ArrayList<>();
    private static final HashMap<String, Integer> result = new HashMap<>();

    static {
        // creating files for testing the first task
        for (int i = 0; i < NUM_TEXTS; i++) {
            String fileName = TITLE_PREF + Integer.toString(i);
            try {
                Files.write(Paths.get(fileName),
                        Arrays.asList(texts[i]));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            paths.add(fileName);
        }

        // creating a map for testing the third task
        for (int i = 0; i < NUM_TEXTS; i++) {
            String authorName = TITLE_PREF + Integer.toString(i);
            library.put(authorName, Arrays.asList(texts[i]));
        }

        // creating maps for the forth test
        elves.put("palantir", 3);
        elves.put("bow", 10);
        orders.add(elves);

        people.put("palantir", 4);
        people.put("bow", 10);
        people.put("axe", 5);
        orders.add(people);

        dwarves.put("axe", 5);
        orders.add(dwarves);

        hobbits.put("blade", 1);
        hobbits.put("bow", 10);
        orders.add(hobbits);

        result.put("palantir", 7);
        result.put("blade", 1);
        result.put("bow", 30);
        result.put("axe", 10);
    }

    @Test
    public void testFindQuotes() {
        assertEquals(
                Arrays.asList("To seek the pale enchanted gold.", "In hollow halls beneath the fells."),
                SecondPartTasks.findQuotes(paths, "al")
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
        assertEquals(TITLE_PREF + "4", SecondPartTasks.findPrinter(library));
    }

    @Test
    public void testCalculateGlobalOrder() {
        assertEquals(result, SecondPartTasks.calculateGlobalOrder(orders));
    }
}