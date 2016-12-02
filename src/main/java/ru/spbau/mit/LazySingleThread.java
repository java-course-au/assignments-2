package ru.spbau.mit;

import java.util.function.Supplier;

/**
 * Created by rebryk on 14/02/16.
 */

public class LazySingleThread<T> implements Lazy<T> {
    private Supplier<T> supplier;
    private T data;

    public LazySingleThread(final Supplier<T> supplier) {
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
