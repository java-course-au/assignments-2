package ru.spbau.mit;

import java.util.function.Function;
import org.junit.Test;

import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;

/**
 * Created by rebryk on 14/02/16.
 */

public class LazyFactoryTest {
    public class Flag {
        private boolean value = false;

        public boolean get() {
            return value;
        }

        public void set() {
            value = true;
        }
    }

    public class Counter {
        private int count = 0;

        public synchronized int get() {
            return count;
        }

        public synchronized void inc() {
            count++;
        }
    }

    public void checkLazy(final Function <Supplier<Integer>, Lazy<Integer>> function) {
        final Counter counter = new Counter();
        Lazy<Integer> lazy = function.apply(new Supplier<Integer>() {
            @Override
            public Integer get() {
                counter.inc();
                return 42;
            }
        });

        assertEquals(counter.get(), 0);
        Integer obj = lazy.get();
        assertEquals((int)obj, 42);
        assertEquals(counter.get(), 1);
        assertEquals(lazy.get(), obj);
        assertEquals(counter.get(), 1);

        final Counter counter2 = new Counter();
        lazy = function.apply(new Supplier<Integer>() {
            @Override
            public Integer get() {
                counter2.inc();
                return null;
            }
        });

        assertEquals(counter2.get(), 0);
        obj = lazy.get();
        assertEquals(obj, null);
        assertEquals(counter2.get(), 1);
        assertEquals(lazy.get(), obj);
        assertEquals(counter2.get(), 1);
    }

    public void checkMultithreadingSingle(final Function <Supplier<Integer>, Lazy<Integer>> function) {
        final Counter counter = new Counter();

        final Lazy<Integer> lazy = function.apply(new Supplier<Integer>() {
            @Override
            public Integer get() {
                counter.inc();
                return 42;
            }
        });

        final Flag started = new Flag();
        for (int i = 0; i < 25; ++i) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!started.get()) {
                        // waiting
                    }
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
        checkLazy(new Function<Supplier<Integer>, Lazy<Integer>>() {
            @Override
            public Lazy<Integer> apply(Supplier<Integer> integerSupplier) {
                return LazyFactory.createLazy(integerSupplier);
            }
        });
    }

    @Test
    public void testCreateLazyMultithreading() {
        checkLazy(new Function<Supplier<Integer>, Lazy<Integer>>() {
            @Override
            public Lazy<Integer> apply(Supplier<Integer> integerSupplier) {
                return LazyFactory.createLazyMultithreading(integerSupplier);
            }
        });
    }

    @Test
    public void testCreateLazyMultithreadingMulti() {
        checkMultithreading(new Function<Supplier<Integer>, Lazy<Integer>>() {
            @Override
            public Lazy<Integer> apply(Supplier<Integer> integerSupplier) {
                return LazyFactory.createLazyMultithreading(integerSupplier);
            }
        });
    }

    @Test
    public void testCreateLazyLockFree() {
        checkLazy(new Function<Supplier<Integer>, Lazy<Integer>>() {
            @Override
            public Lazy<Integer> apply(Supplier<Integer> integerSupplier) {
                return LazyFactory.createLazyLockFree(integerSupplier);
            }
        });
    }


    @Test
    public void testCreateLazyLockFreeMulti() {
        checkMultithreading(new Function<Supplier<Integer>, Lazy<Integer>>() {
            @Override
            public Lazy<Integer> apply(Supplier<Integer> integerSupplier) {
                return LazyFactory.createLazyLockFree(integerSupplier);
            }
        });
    }
}
