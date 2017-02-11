package ru.spbau.mit;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.Assert.*;

public class LazyFactoryTest {

    private static final Integer VALUE = 5;
    private static final Integer TIME_TO_SLEEP = 1000;
    private static final int THREAD_COUNT = 100;

    private interface CountingSupplier<T> extends Supplier<T> {
        int getCount();
    }

    /*
     * Test set one: just check double getting.
     */
    private void doTestBasic(Function<Supplier<Integer>, Lazy<Integer>> provider, boolean checkSingleGet) {
        doTestBasic(provider, VALUE, checkSingleGet);
        doTestBasic(provider, null, checkSingleGet);
    }

    private void doTestBasic(
            Function<Supplier<Integer>, Lazy<Integer>> provider,
            final Integer value,
            boolean checkSingleGet) {
        CountingSupplier<Integer> supplier = new CountingSupplier<Integer>() {
            private int count = 0;

            @Override
            public Integer get() {
                count++;
                return value;
            }

            @Override
            public int getCount() {
                return count;
            }
        };

        Lazy<Integer> lazy = provider.apply(supplier);
        assertEquals(0, supplier.getCount());
        Object result = lazy.get();
        assertTrue(value == result);
        assertEquals(1, supplier.getCount());
        result = lazy.get();
        assertTrue(value == result);

        if (checkSingleGet) {
            assertEquals(1, supplier.getCount());
        }
    }

    /*
     * With normal values
     */
    @Test
    public void testGetSingleThreadLazyBasic() throws Exception {
        doTestBasic(LazyFactory::getSingleThreadLazy, true);
    }

    @Test
    public void testGetConcurrentLazyBasic() {
        doTestBasic(LazyFactory::getConcurrentLazy, true);
    }

    @Test
    public void testGetLockFreeLazyBasic() {
        // LockFree is allowed (and actually can) get value more than one time
        doTestBasic(LazyFactory::getLockFreeLazy, VALUE, false);
    }

    /*
     * Test set two: create a bunch of threads, and look them getting the same value
     */
    private void doTestConcurrency(Function<Supplier<Integer>, Lazy<Integer>> provider) {
        doTestConcurrency(provider, true);
    }

    private void doTestConcurrency(Function<Supplier<Integer>, Lazy<Integer>> provider, boolean checkSingleGet) {
        CountingSupplier<Integer> supplier = new CountingSupplier<Integer>() {
            private int count = 0;

            @Override
            public Integer get() {
                int toReturn;
                synchronized (this) {
                    toReturn = count++;
                }
                try {
                    Thread.sleep(TIME_TO_SLEEP);
                } catch (InterruptedException ignored) {
                }
                return toReturn;
            }

            @Override
            public synchronized int getCount() {
                return count;
            }
        };

        Lazy<Integer> lazy = provider.apply(supplier);

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        List<Future<Integer>> futures = new ArrayList<>();
        for (int i = 0; i != THREAD_COUNT; ++i) {
            futures.add(executor.submit(() -> {
                try {
                    Thread.sleep(TIME_TO_SLEEP);
                } catch (InterruptedException ignored) {
                }

                return lazy.get();
            }));
        }

        Object lastValue = null;
        boolean wasValue = false;
        for (Future<Integer> future : futures) {
            try {
                Object newValue = future.get();
                if (wasValue) {
                    assertTrue(lastValue == newValue);
                }
                wasValue = true;
                lastValue = newValue;
            } catch (InterruptedException | ExecutionException ignored) {
            }
        }

        if (checkSingleGet) {
            assertEquals(1, supplier.getCount());
        }
    }

    @Test
    public void testGetConcurrencyLazyConcurrent() {
        doTestConcurrency(LazyFactory::getConcurrentLazy);
    }

    @Test
    public void testGetLockFreeLazyConcurrent() {
        // LockFree is allowed (and actually can) get value more than one time
        doTestConcurrency(LazyFactory::getLockFreeLazy, false);
    }
}
