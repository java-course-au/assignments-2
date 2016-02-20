package ru.spbau.mit;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Supplier;

public class LazyFactory {
    private static class OneThreadLazy<T> implements Lazy<T> {
        private static final Object NONE = new Object();
        private Supplier<T> supplier;
        private Object result = NONE;

        OneThreadLazy(Supplier<T> supplier) {
            this.supplier = supplier;
        }

        @Override
        public T get() {
            if (result == NONE) {
                result = supplier.get();
                supplier = null;
            }
            return (T) result;
        }
    }

    private static class MultiThreadLazy<T> implements Lazy<T> {
        private static final Object NONE = new Object();
        private Supplier<T> supplier;
        private volatile Object result = NONE;

        MultiThreadLazy(Supplier<T> supplier) {
            this.supplier = supplier;
        }

        @Override
        public T get() {
            if (result == NONE) {
                synchronized (this) {
                    if (result == NONE) {
                        result = supplier.get();
                        supplier = null;
                    }
                }
            }
            return (T) result;
        }
    }

    private static class LockFreeLazy<T> implements Lazy<T> {
        private static final Object NONE = new Object();
        private Supplier<T> supplier;
        private final static
        AtomicReferenceFieldUpdater<LockFreeLazy, Object> updater =
                AtomicReferenceFieldUpdater.newUpdater(
                        LockFreeLazy.class, Object.class, "result");
        private volatile Object result = NONE;

        LockFreeLazy(Supplier<T> supplier) {
            this.supplier = supplier;
        }

        @Override
        public T get() {
            if (result == NONE) {
                Supplier<T> local = supplier;
                if (local != null) {
                    if (updater.compareAndSet(this, NONE, local.get())) {
                        supplier = null;
                    }
                }
            }
            return (T) result;
        }
    }

    public static <T> Lazy<T> createLazyOneThread(final Supplier<T> supplier) {
        return new OneThreadLazy<>(supplier);
    }

    public static <T> Lazy<T> createLazyMultiThread(final Supplier<T> supplier) {
        return new MultiThreadLazy<>(supplier);
    }

    public static <T> Lazy<T> createLazyLockFree(final Supplier<T> supplier) {
        return new LockFreeLazy<>(supplier);
    }
}