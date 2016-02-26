package ru.spbau.mit;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Supplier;

public class LazyFactoryTest {
    private static final int CHECKS_COUNT = 100;
    private static final int THREADS_COUNT = 50;
    private static final int TIME_TO_SLEEP = 100;

    private static class SupplierCounter implements Supplier<String> {
        private String stringCounter = "";

        @Override
        public String get() {
            Thread.yield();
            stringCounter += "a";
            return stringCounter;
        }

        public String getCnt() {
            return stringCounter;
        }
    }

    private static class SupplierCounterReturnsNull implements Supplier<String> {
        private String stringCounter = "";

        @Override
        public String get() {
            Thread.yield();
            stringCounter += "a";
            return null;
        }

        public String getCnt() {
            return stringCounter;
        }
    }

    private void checkOneThread(LazyFactoryFromSupplier factory) {
        SupplierCounter supplier = new SupplierCounter();
        SupplierCounterReturnsNull supplierReturnsNull = new SupplierCounterReturnsNull();
        Lazy<String> lazy = factory.createLazy(supplier);
        Lazy<String> lazyForNull = factory.createLazy(supplierReturnsNull);
        assertEquals("", supplier.getCnt());
        assertEquals("", supplierReturnsNull.getCnt());
        String result = lazy.get();
        String nullResult = lazyForNull.get();
        assertEquals("a", supplier.getCnt());
        assertEquals("a", supplierReturnsNull.getCnt());
        assertEquals("a", result);
        assertEquals(null, nullResult);
        for (int i = 0; i < CHECKS_COUNT; i++) {
            assertTrue(result == lazy.get());
            assertTrue(null == lazyForNull.get());
            assertEquals("a", supplier.getCnt());
            assertEquals("a", supplierReturnsNull.getCnt());
        }
    }
    
    private void checkMultiThread(LazyFactoryFromSupplier factory, boolean callSupplierOnlyOnce) {
        SupplierCounter supplier = new SupplierCounter();
        Lazy<String> lazy = factory.createLazy(supplier);
        List<Thread> tasks = new ArrayList<>();
        List<String> results = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < THREADS_COUNT; i++) {
            Thread task = new Thread(() -> {
                try {
                    Thread.sleep(TIME_TO_SLEEP);
                } catch (InterruptedException ignored) {
                }
                results.add(lazy.get());
            });
            tasks.add(task);
            task.start();
        }
        for (Thread task : tasks) {
            try {
                task.join();
            } catch (InterruptedException ignored) {
            }
        }

        assertTrue(THREADS_COUNT == results.size());
        String firstResult = results.get(0);
        assertEquals(firstResult, "a");

        for (String result : results) {
            assertTrue(result == firstResult);
        }

        if (callSupplierOnlyOnce) {
            assertEquals("a", supplier.getCnt());
        }
    }

    @Test
    public void testCreateLazyOneThread_supplierCounter() {
        checkOneThread(LazyFactory::createLazyOneThread);
    }

    @Test
    public void testCreateLazyMultiThread_supplierCounter() {
        checkOneThread(LazyFactory::createLazyMultiThread);
    }

    @Test
    public void testCreateLazyLockFree_supplierCounter() {
        checkOneThread(LazyFactory::createLazyLockFree);
    }

    @Test
    public void testCreateLazyMultiThread_concurrency() {
        checkMultiThread(LazyFactory::createLazyMultiThread, true);
    }

    @Test
    public void testCreateLazyLockFree_concurrency() {
        checkMultiThread(LazyFactory::createLazyLockFree, false);
    }
}
