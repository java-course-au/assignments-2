package ru.spbau.mit;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class LazyFactoryTest {
    private static final int CHECKS_COUNT = 100;
    private static final int THREADS_COUNT = 50;

    private static class SupplierCounter implements Supplier<Object> {
        private AtomicInteger cnt = new AtomicInteger(0);
        private Object result;

        public SupplierCounter(Object result) {
            this.result = result;
        }

        @Override
        public Object get() {
            Thread.yield();
            cnt.incrementAndGet();
            return result;
        }

        public Integer getCnt() {
            return cnt.get();
        }
    }

    private void checkOneThread(LazyFactoryFromSupplier factory, Object returnValue) {
        SupplierCounter supplier = new SupplierCounter(returnValue);
        Lazy<Object> lazy = factory.createLazy(supplier);

        assertTrue(0 == supplier.getCnt());

        for (int i = 0; i < CHECKS_COUNT; i++) {
            assertTrue(returnValue == lazy.get());
            assertTrue(1 == supplier.getCnt());
        }
    }

    private void checkMultiThread(LazyFactoryFromSupplier factory, boolean callSupplierOnlyOnce, Object returnValue) {
        SupplierCounter supplier = new SupplierCounter(returnValue);
        Lazy<Object> lazy = factory.createLazy(supplier);
        List<Thread> tasks = new ArrayList<>();
        List<Object> results = Collections.synchronizedList(new ArrayList<>());
        CyclicBarrier barrier = new CyclicBarrier(THREADS_COUNT);

        for (int i = 0; i < THREADS_COUNT; i++) {
            Thread task = new Thread(() -> {
                try {
                    barrier.await();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                results.add(lazy.get());
            });
            tasks.add(task);
            task.start();
        }

        for (Thread task : tasks) {
            try {
                task.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        assertEquals(THREADS_COUNT, results.size());

        for (Object result : results) {
            assertTrue(returnValue == result);
        }

        assertTrue(supplier.getCnt() > 0);
        if (callSupplierOnlyOnce) {
            assertTrue(1 == supplier.getCnt());
        }
    }

    @Test
    public void testCreateLazyOneThread() {
        checkOneThread(LazyFactory::createLazyOneThread, new Object());
        checkOneThread(LazyFactory::createLazyOneThread, null);
    }

    @Test
    public void testCreateLazyMultiThread() {
        checkOneThread(LazyFactory::createLazyMultiThread, new Object());
        checkOneThread(LazyFactory::createLazyMultiThread, null);
    }

    @Test
    public void testCreateLazyLockFree() {
        checkOneThread(LazyFactory::createLazyLockFree, new Object());
        checkOneThread(LazyFactory::createLazyLockFree, null);
    }

    @Test
    public void testConcurrencyCreateLazyMultiThread() {
        checkMultiThread(LazyFactory::createLazyMultiThread, true, new Object());
    }

    @Test
    public void testConcurrencyCreateLazyLockFree() {
        checkMultiThread(LazyFactory::createLazyLockFree, false, new Object());
    }
}
