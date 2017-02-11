package ru.spbau.mit;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

public class MyClassTest {

    @Rule
    public final ThreadExpectedException exp = new ThreadExpectedException();

    @Test
    public void testGood() throws InterruptedException {
        Thread a = new Thread(() -> {
            throw new RuntimeException();
        });
        Thread b = new Thread(() -> {
            throw new RuntimeException();
        });
        exp.expect(RuntimeException.class);
        exp.registerThread(a);
        exp.registerThread(b);
        a.start();
        b.start();
        a.join();
        b.join();
    }

    @Test
    public void testBad() throws InterruptedException {
        Thread a = new Thread(() -> {
            throw new RuntimeException();
        });
        Thread b = new Thread(() -> {
            throw new RuntimeException();
        });
        exp.registerThread(a);
        exp.registerThread(b);
        a.start();
        b.start();
        a.join();
        b.join();
    }

    @Test
    public void testAnotherBad() throws InterruptedException {
        Thread a = new Thread(() -> {
            while (true) {
                continue;
            }
        });
        Thread b = new Thread(() -> {
            while (true) {
                continue;
            }
        });
        exp.expect(RuntimeException.class);
        exp.registerThread(a);
        exp.registerThread(b);
        a.start();
        b.start();
    }

}
