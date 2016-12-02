package ru.spbau.mit;

import java.util.function.Supplier;

class SingleThreadedLazy<T> implements Lazy<T> {
    private Supplier<T> supplier;
    private T result;

    SingleThreadedLazy(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public T get() {
        if (supplier == null) {
            return result;
        }
        result = supplier.get();
        supplier = null;
        return result;
    }
}
