package ru.spbau.mit;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Supplier;

/**
 * Created by LDVSOFT on 09.02.2016.
 */
public abstract class LazyFactory {
    private static final class SingleThreadLazy<T> implements Lazy<T> {
        private Supplier<? extends T> supplier;
        private T data;

        private SingleThreadLazy(Supplier<? extends T> supplier) {
            this.supplier = supplier;
        }

        @Override
        public T get() {
            if (supplier != null) {
                data = supplier.get();
                supplier = null;
            }
            return data;
        }
    }

    private static final class ConcurrentLazy<T> implements Lazy<T> {
        private volatile Supplier<? extends T> supplier;
        private T data;

        private ConcurrentLazy(Supplier<? extends T> supplier) {
            this.supplier = supplier;
        }

        @Override
        public T get() {
            if (supplier != null) {
                synchronized (this) {
                    if (supplier != null) {
                        data = supplier.get();
                        supplier = null;
                    }
                }
            }
            return data;
        }
    }

    private static final class LockFreeLazy<T> implements Lazy<T> {
        private volatile Supplier<? extends T> supplier;
        @SuppressWarnings("unused")
        private volatile Object data = NONE;

        private static final AtomicReferenceFieldUpdater<LockFreeLazy, Object> DATA_UPDATER
                = AtomicReferenceFieldUpdater.newUpdater(LockFreeLazy.class, Object.class, "data");
        private static final Object NONE = new Object();

        private LockFreeLazy(Supplier<? extends T> supplier) {
            this.supplier = supplier;
        }

        @Override
        public T get() {
            if (DATA_UPDATER.get(this) == NONE) {
                T result = (T) supplier.get();
                DATA_UPDATER.compareAndSet(this, NONE, result);
            }

            return (T) DATA_UPDATER.get(this);
        }
    }

    public static <T> Lazy<T> getSingleThreadLazy(Supplier<? extends T> supplier) {
        return new SingleThreadLazy<T>(supplier);
    }

    public static <T> Lazy<T> getConcurrentLazy(final Supplier<? extends T> supplier) {
        return new ConcurrentLazy<T>(supplier);
    }

    public static <T> Lazy<T> getLockFreeLazy(Supplier<? extends T> supplier) {
        return new LockFreeLazy<>(supplier);
    }
}
