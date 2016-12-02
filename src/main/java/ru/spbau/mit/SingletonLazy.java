package ru.spbau.mit;

import java.util.function.Supplier;

class SingletonLazy<T> implements Lazy<T> {
    private volatile Supplier<T> supplier;

    // No need in volatile `result` because we either:
    // 1. Read it in the first `if`, but we read changed value of volatile variable there (`supplier`) which
    //    guarantees that changes made before `supplier = null` are visible to our thread after reading `supplier`
    // 2. Return it in the end of get(), which happens either after we write to `result` or after we acquire
    //    monitor and see that `supplier` is changed (and that change was performed before other thread released
    //    the monitor)
    private T result;

    SingletonLazy(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public T get() {
        if (supplier == null) {
            return result;
        }
        synchronized (this) {
            if (supplier != null) {
                result = supplier.get();
                supplier = null;
            }
        }
        return result;
    }
}
