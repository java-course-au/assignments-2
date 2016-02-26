package ru.spbau.mit;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.hamcrest.Factory;
import org.junit.Test;

import java.util.function.Supplier;
import static org.junit.Assert.*;

/**
 * Created by rebryk on 14/02/16.
 */

public class LazyFactoryTest {
    public void checkLazy(final Function <Supplier, Lazy> function) {
        final AtomicInteger counter = new AtomicInteger();
        counter.set(0);

        Supplier<int[]> supplier = () -> {
            counter.getAndIncrement();
            return new int[]{1, 2, 3};
        };

        Lazy lazy = function.apply(supplier);
        assertEquals(counter.get(), 0);
        assertSame(lazy.get(), lazy.get());
        assertEquals(counter.get(), 1);
        assertArrayEquals((int[])lazy.get(), supplier.get());

        counter.set(0);
        lazy = function.apply(() -> {
            counter.getAndIncrement();
            return null;
        });

        assertEquals(counter.get(), 0);
        assertEquals(lazy.get(), null);
        assertEquals(counter.get(), 1);
        assertEquals(lazy.get(), lazy.get());
        assertEquals(counter.get(), 1);
    }

    public void checkMultithreadingSingle(final Function <Supplier, Lazy> function) {
        final AtomicInteger counter = new AtomicInteger();
        counter.set(0);

        Supplier<int[]> supplier = () -> {
            counter.getAndIncrement();
            return new int[]{1, 2, 3};
        };

        final Lazy lazy = function.apply(supplier);
        assertEquals(counter.get(), 0);

        CyclicBarrier barrier = new CyclicBarrier(25);
        for (int i = 0; i < 25; ++i) {
            new Thread(() -> {
                    try {
                        barrier.await();
                        assertSame(lazy.get(), lazy.get());
                        assertArrayEquals((int[]) lazy.get(), supplier.get());
                    } catch (InterruptedException | BrokenBarrierException ex) {
                        // exception
                    }
                }).start();
        }
    }

    public void checkMultithreading(final Function <Supplier, Lazy> function) {
        for (int i = 0; i < 5; ++i) {
            checkMultithreadingSingle(function);
        }
    }

    @Test
    public void testCreateLazy() {
        checkLazy(LazyFactory::createLazy);
    }

    @Test
    public void testCreateLazyMultithreading() {
        checkLazy(LazyFactory::createLazyMultithreading);
    }

    @Test
    public void testCreateLazyMultithreadingMulti() {
        checkMultithreading(LazyFactory::createLazyMultithreading);
    }

    @Test
    public void testCreateLazyLockFree() {
        checkLazy(LazyFactory::createLazyLockFree);
    }

    @Test
    public void testCreateLazyLockFreeMulti() {
        checkMultithreading(LazyFactory::createLazyLockFree);
    }
}
