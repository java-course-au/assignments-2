package ru.spbau.mit;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class LazyFactoryTest {
    private abstract static class CounterSupplier<T> implements Supplier<T> {
        protected T result;
        protected int counter = 0;

        public CounterSupplier(T result) {
            this.result = result;
        }

        public int getCounter() {
            return counter;
        }
    }

    private static class IntegerCounterSupplier extends CounterSupplier<Integer> {
        private static final int RESULT = 10;
        public IntegerCounterSupplier() {
            super(RESULT);
        }

        @Override
        public Integer get() {
            counter++;
            return result;
        }
    }

    private static class NullSupplier <T> extends CounterSupplier<T> {
        public NullSupplier() {
            super(null);
        }

        @Override
        public T get() {
            counter++;
            return null;
        }
    }

    public <T> void checkCounterAndSingleResult(Function<CounterSupplier<T>, Lazy<T>> getLazyFactory,
                                                CounterSupplier<T> supplier,
                                                boolean areMultipleCalculationsAllowed) {
        final int ITERATION_NUMBER = 100;

        Lazy<T> lazy = getLazyFactory.apply(supplier);
        assertTrue(supplier.getCounter() == 0);
        List<T> results = new ArrayList<>();
        for (int i = 0; i < ITERATION_NUMBER; i++) {
            results.add(lazy.get());
        }
        for (int i = 0; i < ITERATION_NUMBER; i++) {
            assertTrue(results.get(0) == results.get(i));
        }
        if (!areMultipleCalculationsAllowed) {
            assertEquals(supplier.getCounter(), 1);
        }
        assertEquals(results.get(0), supplier.get());
    }

    public <T> void checkMultiThreading(Function<CounterSupplier<T>, Lazy<T>> getLazyFactory,
                                        CounterSupplier<T> supplier, boolean areMultipleCalculationsAllowed) {
        final int THREAD_NUMBER = 100;

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
    }

    public <T> void checkNullSupplier(Function<CounterSupplier<T>, Lazy<T>> getLazyFactory,
                                      NullSupplier supplier, boolean areMultipleCalculationsAllowed) {
        checkCounterAndSingleResult(getLazyFactory, supplier, areMultipleCalculationsAllowed);
    }

    @Test
    public void testSingleThreadLazy() {
        checkCounterAndSingleResult(LazyFactory::createSingleThreadLazy, new IntegerCounterSupplier(), false);
        checkNullSupplier(LazyFactory::createSingleThreadLazy, new NullSupplier(), false);
    }

    @Test
    public void testMultipleThreadLazy() {
        checkCounterAndSingleResult(LazyFactory::createMultipleThreadLazy, new IntegerCounterSupplier(), false);
        checkMultiThreading(LazyFactory::createMultipleThreadLazy, new IntegerCounterSupplier(), false);
        checkNullSupplier(LazyFactory::createMultipleThreadLazy, new NullSupplier(), false);
        checkMultiThreading(LazyFactory::createMultipleThreadLazy, new NullSupplier(), false);
    }

    @Test
    public void testLockFreeMultipleThreadLazy() {
        checkCounterAndSingleResult(LazyFactory::createLockFreeMultipleThreadLazy,
                new IntegerCounterSupplier(), true);
        checkMultiThreading(LazyFactory::createLockFreeMultipleThreadLazy, new IntegerCounterSupplier(), true);
        checkNullSupplier(LazyFactory::createLockFreeMultipleThreadLazy, new NullSupplier(), true);
        checkMultiThreading(LazyFactory::createLockFreeMultipleThreadLazy, new NullSupplier(), true);
    }
}
