package ru.spbau.mit;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Supplier;

/**
 * Created by rebryk on 14/02/16.
 */
public class LazyLockFree<T> implements Lazy<T> {
    private static final Object NONE = new Object();

    private Supplier<T> supplier;
    private volatile Object data = NONE;

    private static final AtomicReferenceFieldUpdater<LazyLockFree, Object> UPDATER
            = AtomicReferenceFieldUpdater.newUpdater(LazyLockFree.class, Object.class, "data");

    public LazyLockFree(final Supplier<T> supplier) {
        this.supplier = supplier;
    }

    @Override
    public T get() {
        if (data == NONE) {
            Supplier<T> local = supplier;
            if (local != null) {
                if (UPDATER.compareAndSet(this, NONE, local.get())) {
                    supplier = null;
                }
            }
        }
        return (T) data;
    }
}
