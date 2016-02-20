package ru.spbau.mit;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Supplier;

public class LazyFactoryTest {
    private static class SupplierCounter implements Supplier<Integer> {
        private Integer cnt = 0;

        @Override
        public Integer get() {
            Thread.yield();
            cnt++;
            return cnt;
        }

        public Integer getCnt() {
            return cnt;
        }
    }

    private static class SupplierCounterReturnsNull implements Supplier<Integer> {
        private Integer cnt = 0;

        @Override
        public Integer get() {
            Thread.yield();
            cnt++;
            return null;
        }

        public Integer getCnt() {
            return cnt;
        }
    }

    private void checkOneThread(LazyFactoryFromSupplier factory) {
        SupplierCounter supplier = new SupplierCounter();
        Lazy<Integer> lazy = factory.createLazy(supplier);
        assertEquals(Integer.valueOf(0), supplier.getCnt());
        Integer result = lazy.get();
        assertEquals(Integer.valueOf(1), supplier.getCnt());
        assertEquals(Integer.valueOf(1), result);
        for (int i = 0; i < 100; i++) {
            assertTrue(result == lazy.get());
            assertEquals(Integer.valueOf(1), supplier.getCnt());
        }
    }

    private void checkOneThread_returnsNull(LazyFactoryFromSupplier factory) {
        SupplierCounterReturnsNull supplierReturnsNull = new SupplierCounterReturnsNull();
        Lazy<Integer> lazy = factory.createLazy(supplierReturnsNull);
        assertEquals(Integer.valueOf(0), supplierReturnsNull.getCnt());
        Integer result = lazy.get();
        assertEquals(Integer.valueOf(1), supplierReturnsNull.getCnt());
        assertEquals(null, result);
        for (int i = 0; i < 100; i++) {
            assertTrue(null == lazy.get());
            assertEquals(Integer.valueOf(1), supplierReturnsNull.getCnt());
        }
    }

    private void checkMultiThread(LazyFactoryFromSupplier factory, boolean callSupplierOnlyOnce) {
        SupplierCounter supplier = new SupplierCounter();
        Lazy<Integer> lazy = factory.createLazy(supplier);
        List<Future<Integer>> list = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(50);
        for (int i = 0; i < 50; i++) {
            list.add(executor.submit(() -> {
                Thread.yield();
                return lazy.get();
            }));
        }
        try {
            Integer result = list.get(0).get();
            assertEquals(Integer.valueOf(1), result);
            for (int i = 1; i < 50; i++) {
                assertTrue(result == list.get(i).get());
            }
        } catch (Exception ignored) {
        }
        if (callSupplierOnlyOnce) {
            assertEquals(Integer.valueOf(1), supplier.getCnt());
        }
    }

    private void checkMultiThread_returnsNull(LazyFactoryFromSupplier factory, boolean callSupplierOnlyOnce) {
        SupplierCounterReturnsNull supplier = new SupplierCounterReturnsNull();
        Lazy<Integer> lazy = factory.createLazy(supplier);
        List<Future<Integer>> list = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(50);
        for (int i = 0; i < 50; i++) {
            list.add(executor.submit(() -> {
                Thread.yield();
                return lazy.get();
            }));
        }
        try {
            for (Future<Integer> future : list) {
                assertTrue(null == future.get());
            }
        } catch (Exception ignored) {
        }
        if (callSupplierOnlyOnce) {
            assertEquals(Integer.valueOf(1), supplier.getCnt());
        }
    }

    @Test
    public void testCreateLazyOneThread_supplierCounter() {
        checkOneThread(LazyFactory::createLazyOneThread);
    }

    @Test
    public void testCreateLazyOneThread_supplierReturnsNull() {
        checkOneThread_returnsNull(LazyFactory::createLazyOneThread);
    }

    @Test
    public void testCreateLazyMultiThread_supplierCounter() {
        checkOneThread(LazyFactory::createLazyMultiThread);
    }

    @Test
    public void testCreateLazyMultiThread_supplierReturnsNull() {
        checkOneThread_returnsNull(LazyFactory::createLazyMultiThread);
    }

    @Test
    public void testCreateLazyLockFree_supplierCounter() {
        checkOneThread(LazyFactory::createLazyLockFree);
    }

    @Test
    public void testCreateLazyLockFree_supplierReturnsNull() {
        checkOneThread_returnsNull(LazyFactory::createLazyLockFree);
    }

    @Test
    public void testCreateLazyMultiThread_concurrency() {
        checkMultiThread(LazyFactory::createLazyMultiThread, true);
    }

    @Test
    public void testCreateLazyLockFree_concurrency() {
        checkMultiThread(LazyFactory::createLazyLockFree, false);
    }

    @Test
    public void testCreateLazyMultiThread_concurrencyReturnsNull() {
        checkMultiThread_returnsNull(LazyFactory::createLazyMultiThread, true);
    }

    @Test
    public void testCreateLazyLockFree_concurrencyReturnsNull() {
        checkMultiThread_returnsNull(LazyFactory::createLazyLockFree, false);
    }
}
