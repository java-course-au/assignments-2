package ru.spbau.mit;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Supplier;

/**
 * Created by rozplokhas on 14.02.16.
 */
public final class LazyFactory {
    private LazyFactory() {
    }

    private static class SimpleLazy<T> implements Lazy<T> {
        private T result;
        private Supplier<T> supplier = null;

        SimpleLazy(Supplier<T> supplier) {
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

    public static <T> Lazy<T> createSimpleLazy(Supplier<T> supplier) {
        return new SimpleLazy<>(supplier);
    }

    private static class MultithreadLazy<T> implements Lazy<T> {
        private volatile T result;
        private volatile Supplier<T> supplier;

        MultithreadLazy(Supplier<T> supplier) {
            this.supplier = supplier;
        }

        public T get() {
            if (supplier != null) {
                synchronized (this) {
                    if (supplier != null) {
                        result = supplier.get();
                        supplier = null;
                    }
                }
            }

            return result;
        }
    }

    public static <T> Lazy<T> createMultithreadLazy(Supplier<T> supplier) {
        return new MultithreadLazy<>(supplier);
    }

    private static class LockFreeLazy<T> implements Lazy<T> {
        private static final Object NONE = new Object();
        private static final AtomicReferenceFieldUpdater<LockFreeLazy, Object> UPDATER =
                AtomicReferenceFieldUpdater.newUpdater(LockFreeLazy.class, Object.class, "result");

        private volatile Object result = NONE;
        private Supplier<T> supplier;

        LockFreeLazy(Supplier<T> supplier) {
            this.supplier =  supplier;
        }

        @SuppressWarnings("unchecked")
        public T get() {
            if (result == NONE) {
                UPDATER.compareAndSet(this, NONE, supplier.get());
            }

            return (T) result;
        }
    }

    public static <T> Lazy<T> createLockFreeLazy(final Supplier<T> supplier) {
        return new LockFreeLazy<>(supplier);
    }
}
