package ru.spbau.mit;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class LazyFactoryTest {
    private static final int ITERATION_NUMBER = 100;
    private static final int THREAD_NUMBER = 100;

    private static class CounterSupplier<T> implements Supplier<T> {
        private T result;
        private int counter = 0;

        CounterSupplier(T result) {
            this.result = result;
        }

        @Override
        public T get() {
            return result;
        }

        int getCounter() {
            return counter;
        }

        T getResult() {
            return result;
        }

        protected void incrementCounter() {
            counter++;
        }
    }

    private static class IntegerCounterSupplier extends CounterSupplier<Integer> {
        private static final int RESULT = 10;
        IntegerCounterSupplier() {
            super(RESULT);
        }

        @Override
        public Integer get() {
            incrementCounter();
            return getResult();
        }
    }

    private static class NullSupplier<T> extends CounterSupplier<T> {
        NullSupplier() {
            super(null);
        }

        @Override
        public T get() {
            incrementCounter();
            return null;
        }
    }

    public <T> void checkCounterAndSingleResult(Function<Supplier<T>, Lazy<T>> getLazyFactory,
                                                CounterSupplier<T> supplier) {
        Lazy<T> lazy = getLazyFactory.apply(supplier);
        assertTrue(supplier.getCounter() == 0);
        List<T> results = new ArrayList<>();
        for (int i = 0; i < ITERATION_NUMBER; i++) {
            results.add(lazy.get());
        }
        for (int i = 0; i < ITERATION_NUMBER; i++) {
            assertTrue(results.get(0) == results.get(i));
        }
        assertEquals(supplier.getCounter(), 1);
        assertEquals(results.get(0), supplier.get());
    }

    public <T> void checkMultiThreading(Function<Supplier<T>, Lazy<T>> getLazyFactory,
                                        CounterSupplier<T> supplier, boolean areMultipleCalculationsAllowed) {
        Lazy<T> lazy = getLazyFactory.apply(supplier);
        assertTrue(supplier.getCounter() == 0);
        List<Thread> threads = new ArrayList<>();
        List<T> results = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < THREAD_NUMBER; i++) {
            threads.add(new Thread(new Runnable() {
                @Override
                public void run() {
                    results.add(lazy.get());
                }
            }));
        }
        for (Thread thread : threads) {
            thread.start();
        }
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        for (int i = 0; i < THREAD_NUMBER; i++) {
            if (results.get(0) != results.get(i)) {
                System.out.println(results.get(0));
                System.out.println(results.get(i));
            }
            assertTrue(results.get(0) == results.get(i));
        }
        if (!areMultipleCalculationsAllowed) {
            assertEquals(supplier.getCounter(), 1);
        }
        assertTrue(supplier.getCounter() >= 1);
        assertEquals(results.get(0), supplier.get());
    }

    public <T> void checkNullSupplier(Function<Supplier<T>, Lazy<T>> getLazyFactory) {
        checkCounterAndSingleResult(getLazyFactory, new NullSupplier<>());
    }

    public void checkSingleThreadContracts(Function<Supplier<Integer>, Lazy<Integer>> getLazyFactory) {
        checkCounterAndSingleResult(getLazyFactory, new IntegerCounterSupplier());
        checkNullSupplier(getLazyFactory);
    }

    public void checkMultiThreadContracts(Function<Supplier<Integer>, Lazy<Integer>> getLazyFactory,
                                          boolean areMultipleCalculationsAllowed) {
        checkMultiThreading(getLazyFactory, new IntegerCounterSupplier(), areMultipleCalculationsAllowed);
        checkMultiThreading(getLazyFactory, new NullSupplier<>(), areMultipleCalculationsAllowed);
    }

    @Test
    public void testSingleThreadLazy() {
            checkSingleThreadContracts(LazyFactory::createSingleThreadLazy);
    }

    @Test
    public void testMultipleThreadLazy() {
        checkSingleThreadContracts(LazyFactory::createMultipleThreadLazy);
        checkMultiThreadContracts(LazyFactory::createMultipleThreadLazy, false);
    }

    @Test
    public void testLockFreeMultipleThreadLazy() {
        checkSingleThreadContracts(LazyFactory::createLockFreeMultipleThreadLazy);
        checkMultiThreadContracts(LazyFactory::createLockFreeMultipleThreadLazy, true);
    }
}
