package ru.spbau.mit;

import org.junit.Test;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.function.Function;
import java.util.function.Supplier;
import static org.junit.Assert.*;

/**
 * Created by rozplokhas on 15.02.16.
 */
public class LazyFactoryTest {
    private class Tester implements Supplier<Object> {
        private int callsCounter = 0;

        public int getCallsCounter() {
            return callsCounter;
        }

        public Object get() {
            callsCounter++;
            return null;
        }
    }

    private static final int NUMBER_OF_CALLS = 5;
    private void checkOneThread(Function<Supplier<Object>, Lazy<Object>> lazyCreator) {
        Tester tester = new Tester();
        Lazy<Object> lazy = lazyCreator.apply(tester);

        assertEquals(tester.getCallsCounter(), 0);

        for (int i = 0; i < NUMBER_OF_CALLS; i++) {
            assertTrue(lazy.get() == null);
        }

        assertEquals(tester.getCallsCounter(), 1);

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


    private static final int SLEEP_TIME = 100;
    private class SleepyTester implements Supplier<Integer> {
        private int callsCounter = 0;

        public int getCallsCounter() {
            return callsCounter;
        }

        public Integer get() {
            try {
                Thread.sleep(SLEEP_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return callsCounter++;
        }
    }

    private static final int NUMBER_OF_THREADS = 10;
    private void checkRaceCondition(Function<Supplier<Integer>, Lazy<Integer>> lazyCreator,
                                    boolean checkOnlyOnceFlag) {
        Integer[] values = new Integer[NUMBER_OF_THREADS];
        Thread[] threads = new Thread[NUMBER_OF_THREADS];
        SleepyTester tester = new SleepyTester();

        CyclicBarrier barrier = new CyclicBarrier(NUMBER_OF_THREADS);

        Lazy<Integer> lazyInteger = lazyCreator.apply(tester);

        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            final int ind = i;
            threads[ind] = new Thread(() -> {
                try {
                    barrier.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (BrokenBarrierException e) {
                    throw new RuntimeException(e);
                }
                values[ind] = lazyInteger.get();
            });
        }

        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            threads[i].start();
        }

        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        for (int i = 1; i < NUMBER_OF_THREADS; i++) {
            assertTrue(values[i - 1] == values[i]);
        }

        if (checkOnlyOnceFlag) {
            assertEquals(tester.getCallsCounter(), 1);
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
