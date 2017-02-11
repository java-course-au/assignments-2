package ru.spbau.mit;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;

/**
 * Created by Seva on 09.02.2016.
 */
public class TestLazy {

    private static final int ITER_COUNT = 1000;
    private static final int THREAD_COUNT = 1000;
    private static final int ONE_SECOND = 1000;

    private static class RandomNumberSupplier implements Supplier<Integer> {
        private final Random random = new Random(123);

        @Override
        public Integer get() {
            try {
                Thread.sleep(random.nextInt(ONE_SECOND));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return random.nextInt();
        }
    }

    private static class SideEffectSupplier implements Supplier<Integer> {
        private List<Integer> evaluatedNumbers = Collections.synchronizedList(new ArrayList<>());

        @Override
        public Integer get() {
            int result = new RandomNumberSupplier().get();
            evaluatedNumbers.add(result);
            return result;
        }
    }

    private static class NullSupplier implements Supplier<Object> {
        private Object value = null;

        @Override
        public synchronized Object get() {
            if (value == null) {
                value = new Object();
                return null;
            }
            return value;
        }
    }

    @Rule
    public ExpectedException lazyNullSupplierException = ExpectedException.none();

    private List runLazyManyThreads(Lazy lazy) {
        List<Thread> threads = new ArrayList<>();
        final List results = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < THREAD_COUNT; i++) {
            threads.add(new Thread(() -> results.add(lazy.get())));
        }

        threads.forEach(Thread::start);
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return results;
    }

    public void checkLazinessContract(Function<Supplier, Lazy> lazyFactory) {
        SideEffectSupplier supplier = new SideEffectSupplier();
        lazyFactory.apply(supplier);
        try {
            Thread.sleep(ONE_SECOND);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertEquals(0, supplier.evaluatedNumbers.size()); //no evaluations were made even in a separate thread
    }

    @Test
    public void checkLazyNullSupplier() {
        lazyNullSupplierException.expect(IllegalArgumentException.class);
        LazyFactory.createLazy(null);
    }

    @Test
    public void checkMultiThreadLazyNullSupplier() {
        lazyNullSupplierException.expect(IllegalArgumentException.class);
        LazyFactory.createMultiThreadLazy(null);
    }

    @Test
    public void checkNonBlockingMultiThreadLazyNullSupplier() {
        lazyNullSupplierException.expect(IllegalArgumentException.class);
        LazyFactory.createNonBlockingMultiThreadLazy(null);
    }

    public void checkLazyOneThread(Function<Supplier, Lazy> lazyFactory) {
        SideEffectSupplier supplier = new SideEffectSupplier();
        Lazy<Integer> lazy = lazyFactory.apply(supplier);
        Integer expectedNumber = lazy.get();
        for (int i = 0; i < ITER_COUNT; i++) {
            assertEquals(expectedNumber, lazy.get());
        }
        assertEquals(1, supplier.evaluatedNumbers.size());

        NullSupplier nullSupplier = new NullSupplier();
        lazy = lazyFactory.apply(nullSupplier);
        for (int i = 0; i < ITER_COUNT; i++) {
            assertEquals(null, lazy.get());
        }
    }

    public void checkLazyMultiThread(Function<Supplier, Lazy> lazyFactory, boolean checkForOnlyOneCalculation) {
        SideEffectSupplier supplier = new SideEffectSupplier();
        Lazy<Integer> lazy = lazyFactory.apply(supplier);
        List result = runLazyManyThreads(lazy);
        assertEquals(result.stream().distinct().count(), 1);
        if (checkForOnlyOneCalculation) {
            assertEquals(supplier.evaluatedNumbers.size(), 1);
        }

        NullSupplier nullSupplier = new NullSupplier();
        lazy = lazyFactory.apply(nullSupplier);
        result = runLazyManyThreads(lazy);
        assertEquals(result.stream().distinct().count(), 1);
        if (checkForOnlyOneCalculation) {
            assertEquals(supplier.evaluatedNumbers.size(), 1);
        }
    }

    @Test
    public void testAll() {
        List<Function<Supplier, Lazy>> factories = Arrays.asList(
                LazyFactory::createLazy,
                LazyFactory::createMultiThreadLazy,
                LazyFactory::createNonBlockingMultiThreadLazy
        );

        for (Function<Supplier, Lazy> factory : factories) {
            checkLazyOneThread(factory);
            checkLazinessContract(factory);
        }

        checkLazyMultiThread(LazyFactory::createMultiThreadLazy, true);
        checkLazyMultiThread(LazyFactory::createNonBlockingMultiThreadLazy, false);
    }
}
