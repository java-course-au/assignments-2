package ru.spbau.mit;

import org.junit.Test;

import java.util.function.Function;
import java.util.function.Supplier;
import static org.junit.Assert.*;

/**
 * Created by rozplokhas on 15.02.16.
 */
public class LazyFactoryTest {
    private class Tester implements Supplier<Object> {
        public int callsCounter = 0;

        public Object get() {
            callsCounter++;
            return null;
        }
    }

    private void checkOneThread(Function<Supplier<Object>, Lazy<Object>> lazyCreator) {
        Tester tester = new Tester();
        Lazy<Object> lazy = lazyCreator.apply(tester);

        assertEquals(tester.callsCounter, 0);

        for (int i = 0; i < 5; i++) {
            assertTrue(lazy.get() == null);
        }

        assertEquals(tester.callsCounter, 1);

        Lazy<Object> lazyObj = lazyCreator.apply(() -> new Object());

        Object first = lazyObj.get();
        Object second = lazyObj.get();

        assertTrue(first == second);
    }

    @Test
    public void testOneTreadSimple() {
        checkOneThread(LazyFactory::createSimpleLazy);
    }

    @Test
    public void testOneTreadMultithread() {
        checkOneThread(LazyFactory::createMultithreadLazy);
    }

    @Test
    public void testOneTreadLockFree() {
        checkOneThread(LazyFactory::createLockFreeLazy);
    }


    private class SleepyTester implements Supplier<Integer> {
        int callsCounter = 0;
        public Integer get() {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return callsCounter++;
        }
    }

    private void checkRaceCondition(Function<Supplier<Integer>, Lazy<Integer>> lazyCreator,
                                    boolean checkOnlyOnceFlag) {
        Integer[] values = new Integer[10];
        Thread[] threads = new Thread[10];
        SleepyTester tester = new SleepyTester();

        Lazy<Integer> lazyInteger = lazyCreator.apply(tester);

        for (int i = 0; i < 10; i++) {
            final int ind = i;
            threads[ind] = new Thread(() -> {values[ind] = lazyInteger.get();});
            threads[ind].start();
        }

        for (int i = 0; i < 10; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        for (int i = 1; i < 10; i++) {
            assertTrue(values[i - 1] == values[i]);
        }

        if (checkOnlyOnceFlag) {
            assertEquals(tester.callsCounter, 1);
        }
    }

    @Test
    public void testRaceMultithread() {
        checkRaceCondition(LazyFactory::createMultithreadLazy, true);
    }

    @Test
    public void testRaceLockFree() {
        checkRaceCondition(LazyFactory::createLockFreeLazy, false);
    }
}
