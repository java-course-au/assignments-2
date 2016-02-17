package ru.spbau.mit;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Supplier;

public class LazyFactory {
    private static class SingleThreadLazy<T> implements Lazy<T> {
        private Supplier<T> supplier;
        private T result;

        SingleThreadLazy(Supplier<T> supplier) {
            this.supplier = supplier;
        }

        public T get() {
            if (supplier != null) {
                result = supplier.get();
                supplier = null;
            }
            return result;
        }
    }

    private static class MultipleThreadLazy<T> implements Lazy<T> {
        private Supplier<T> supplier;
        private volatile T result = (T) NONE;

        private static final Object NONE = new Object();

        MultipleThreadLazy(Supplier<T> supplier) {
            this.supplier = supplier;
        }

        public T get() {
            if (result == NONE) {
                synchronized (this) {
                    if (result == NONE) {
                        result = supplier.get();
                    }
                }
            }
            return result;
        }
    }

    private static class LockFreeMultipleThreadLazy<T> implements Lazy<T> {
        private Supplier<T> supplier;
        private volatile T result;

        private static final Object NONE = new Object();

        private static final AtomicReferenceFieldUpdater<LockFreeMultipleThreadLazy, Object> UPDATER =
                AtomicReferenceFieldUpdater.newUpdater(LockFreeMultipleThreadLazy.class,
                        Object.class, "result");

        LockFreeMultipleThreadLazy(Supplier<T> supplier) {
            this.supplier = supplier;
        }

        @Override
        public T get() {
            if (result == NONE) {
                Supplier<T> currentSupplier = supplier;
                if (currentSupplier != null) {
                    if (UPDATER.compareAndSet(this, NONE, currentSupplier.get())) {
                        supplier = null;
                    }
                }
            }
            return result;
        }
    }

    public static <T> Lazy<T> createSingleThreadLazy(Supplier<T> supplier) {
        return new SingleThreadLazy<>(supplier);
    }

    public static <T> Lazy<T> createMultipleThreadLazy(Supplier<T> supplier) {
        return new MultipleThreadLazy<>(supplier);
    }

    public static <T> Lazy<T> createLockFreeMultipleThreadLazy(Supplier<T> supplier) {
        return new LockFreeMultipleThreadLazy<>(supplier);
    }
}
