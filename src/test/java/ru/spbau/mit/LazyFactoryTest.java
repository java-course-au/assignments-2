package ru.spbau.mit;

import org.junit.Test;

import java.util.Random;
import java.util.Set;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;

/**
 * Created by rebryk on 14/02/16.
 */

public class LazyFactoryTest {
    private static final int N_THREADS = 2;
    private static final Random RANDOM = new Random(123);

    @Test
    public void testNonThreadSafeLazy() {
        checkSingleThreadContract(LazyFactory::createLazy);
    }

    @Test
    public void testSynchronizedLazy() {
        checkSingleThreadContract(LazyFactory::createLazyMultithreading);
        checkMultiThreadContract(LazyFactory::createLazyMultithreading, false);
    }

    @Test
    public void testSafePublicationLazy() {
        checkSingleThreadContract(LazyFactory::createLazySafePublication);
        checkMultiThreadContract(LazyFactory::createLazySafePublication, true);
    }

    private void checkSingleThreadContract(Function<Supplier<?>, Lazy<?>> factory) {
        checkSingleThreadWithGivenSupplier(factory, Object::new);
        checkSingleThreadWithGivenSupplier(factory, () -> null);
    }

    private void checkSingleThreadWithGivenSupplier(Function<Supplier<?>, Lazy<?>> factory, Supplier<?> supplier) {
        TestSupplier<?> testSupplier = new TestSupplier<>(supplier, false);
        Lazy<?> lazy = factory.apply(testSupplier);

        assertFalse(testSupplier.isCalled());

        assertSame(testSupplier.getFirstResult(), lazy.get());
        assertTrue(testSupplier.isCalled());

        assertSame(testSupplier.getFirstResult(), lazy.get());
    }

    private void checkMultiThreadContract(Function<Supplier<?>, Lazy<?>> factory, boolean canBeMultipleTimes) {
        checkMultiThreadWithGivenSupplier(factory, Object::new, canBeMultipleTimes);
        checkMultiThreadWithGivenSupplier(factory, () -> null, canBeMultipleTimes);
    }

    private void checkMultiThreadWithGivenSupplier(
            Function<Supplier<?>,
            Lazy<?>> factory,
            Supplier<?> supplier,
            boolean canBeMultipleTimes
    ) {
        TestSupplier<?> testSupplier = new TestSupplier<>(supplier, canBeMultipleTimes);
        Lazy<?> lazy = factory.apply(testSupplier);
        assertFalse(testSupplier.isCalled());

        ExecutorService threadPool = Executors.newFixedThreadPool(N_THREADS);
        CyclicBarrier barrier = new CyclicBarrier(N_THREADS);

        Set<?> result = Stream.generate(() ->
                threadPool.submit(() -> {
                    try {
                        barrier.await();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    return lazy.get();
                }))
                .limit(N_THREADS).collect(Collectors.toList()).stream()
                .<Object>map(future -> {
                    try {
                        return future.get();
                    } catch (InterruptedException | ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                }).collect(Collectors.toSet());

        assertEquals(1, result.size());
        assertSame(testSupplier.getFirstResult(), result.iterator().next());
    }

    private class TestSupplier<T> implements Supplier<T> {
        private static final int MAX_SLEEP_TIME_MS = 100;
        private final T firstResult;
        private final Supplier<T> supplier;
        private final boolean canBeCalledMultipleTimes;
        private boolean isCalled;

        TestSupplier(Supplier<T> supplier, boolean canBeCalledMultipleTimes) {
            this.firstResult = supplier.get();
            this.supplier = supplier;
            this.canBeCalledMultipleTimes = canBeCalledMultipleTimes;
        }

        @Override
        public synchronized T get() {
            try {
                Thread.sleep(RANDOM.nextInt(MAX_SLEEP_TIME_MS));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (!isCalled) {
                isCalled = true;
                return firstResult;
            }

            if (!canBeCalledMultipleTimes) {
                fail("Supplier has been called more than once");
            }

            return supplier.get();
        }

        public T getFirstResult() {
            return firstResult;
        }

        public synchronized boolean isCalled() {
            return isCalled;
        }
    }
}

