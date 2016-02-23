package ru.spbau.mit;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import org.junit.Test;

import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 * Created by rebryk on 14/02/16.
 */

public class LazyFactoryTest {
    public static Function<Supplier<Integer>, Lazy<Integer>> funcLazy = new Function<Supplier<Integer>, Lazy<Integer>>() {
        @Override
        public Lazy<Integer> apply(Supplier<Integer> integerSupplier) {
            return LazyFactory.createLazy(integerSupplier);
        }
    };

    public static Function<Supplier<Integer>, Lazy<Integer>> funcLazyMultithreading = new Function<Supplier<Integer>, Lazy<Integer>>() {
        @Override
        public Lazy<Integer> apply(Supplier<Integer> integerSupplier) {
            return LazyFactory.createLazyMultithreading(integerSupplier);
        }
    };

    public static Function<Supplier<Integer>, Lazy<Integer>> funcLazyLockFree = new Function<Supplier<Integer>, Lazy<Integer>>() {
        @Override
        public Lazy<Integer> apply(Supplier<Integer> integerSupplier) {
            return LazyFactory.createLazyLockFree(integerSupplier);
        }
    };


    public class Flag {
        private boolean value = false;

        public boolean get() {
            return value;
        }

        public void set() {
            value = true;
        }
    }

    public void checkLazy(final Function <Supplier<Integer>, Lazy<Integer>> function) {
        final AtomicInteger counter = new AtomicInteger();
        counter.set(0);

        Lazy<Integer> lazy = function.apply(() -> {counter.getAndIncrement(); return 42;});

        assertEquals(counter.get(), 0);
        Integer obj = lazy.get();
        assertEquals((int)obj, 42);
        assertEquals(counter.get(), 1);
        assertEquals(lazy.get(), obj);
        assertEquals(counter.get(), 1);

        counter.set(0);
        lazy = function.apply(() -> {counter.getAndIncrement(); return null;});

        assertEquals(counter.get(), 0);
        obj = lazy.get();
        assertEquals(obj, null);
        assertEquals(counter.get(), 1);
        assertEquals(lazy.get(), obj);
        assertEquals(counter.get(), 1);
    }

    public void checkMultithreadingSingle(final Function <Supplier<Integer>, Lazy<Integer>> function) {
        final AtomicInteger counter = new AtomicInteger();
        counter.set(0);

        final Lazy<Integer> lazy = function.apply(() -> {counter.getAndIncrement(); return 42;});
        assertEquals(counter.get(), 0);

        final Flag started = new Flag();
        for (int i = 0; i < 25; ++i) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!started.get()) {
                        // waiting
                    }
                    assertSame(lazy.get(), lazy.get());
                    assertEquals((int) lazy.get(), 42);
                    assertEquals(counter.get(), 1);
                }
            }).start();
        }
        started.set();
    }

    public void checkMultithreading(final Function <Supplier<Integer>, Lazy<Integer>> function) {
        for (int i = 0; i < 5; ++i) {
            checkMultithreadingSingle(function);
        }
    }

    @Test
    public void testCreateLazy() {
        checkLazy(funcLazy);
    }

    @Test
    public void testCreateLazyMultithreading() {
        checkLazy(funcLazyMultithreading);
    }

    @Test
    public void testCreateLazyMultithreadingMulti() {
        checkMultithreading(funcLazyMultithreading);
    }

    @Test
    public void testCreateLazyLockFree() {
        checkLazy(funcLazyLockFree);
    }

    @Test
    public void testCreateLazyLockFreeMulti() {
        checkMultithreading(funcLazyLockFree);
    }
}
