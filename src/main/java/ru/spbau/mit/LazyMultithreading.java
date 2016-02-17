package ru.spbau.mit;

import java.util.function.Supplier;

/**
 * Created by rebryk on 14/02/16.
 */

public class LazyMultithreading<T> implements Lazy<T>{
    private volatile Supplier<T> supplier;
    private T data;

    public LazyMultithreading(final Supplier<T> supplier) {
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
