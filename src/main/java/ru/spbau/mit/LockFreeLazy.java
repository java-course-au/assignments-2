package ru.spbau.mit;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Supplier;

class LockFreeLazy<T> implements Lazy<T> {
    private volatile Supplier<T> supplier;
    private volatile Object result = RESULT_UNINITIALIZED;

    private static final Object RESULT_UNINITIALIZED = new Object();
    private static final AtomicReferenceFieldUpdater<LockFreeLazy, Object> RESULT_UPDATER =
            AtomicReferenceFieldUpdater.newUpdater(LockFreeLazy.class, Object.class, "result");

    LockFreeLazy(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T get() {
        Supplier<T> currentSupplier = supplier;
        if (currentSupplier == null) {
            return (T) result;
        }
        T currentResult = currentSupplier.get();
        RESULT_UPDATER.compareAndSet(this, RESULT_UNINITIALIZED, currentResult);
        supplier = null;
        return (T) result;
    }
}
