package ru.spbau.mit;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class LazyFactory {
    public static <T> Lazy<T> createLazyOneThread(final Supplier<T> supplier) {
        return new Lazy<T>() {
            private T result;
            private boolean calculated = false;

            public T get() {
                if (!calculated) {
                    result = supplier.get();
                    calculated = true;
                }
                return result;
            }
        };
    }

    public static <T> Lazy<T> createLazyMultiThread(final Supplier<T> supplier) {
        return new Lazy<T>() {
            private T result;
            private boolean calculated = false;

            public synchronized T get() {
                if (!calculated) {
                    result = supplier.get();
                    calculated = true;
                }
                return result;
            }
        };
    }

    public static <T> Lazy<T> createLazyLockFree(final Supplier<T> supplier) {
        return new Lazy<T>() {
            private AtomicReference<T> result = new AtomicReference<>(null);
            private volatile boolean calculated = false;

            public T get() {
                if (!calculated) {
                    result.compareAndSet(null, supplier.get());
                    calculated = true;
                }
                return result.get();
            }
        };
    }
}