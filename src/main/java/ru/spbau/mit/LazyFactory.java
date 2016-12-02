package ru.spbau.mit;

import java.util.function.Supplier;

/**
 * Created by rebryk on 13/02/16.
 */

final class LazyFactory {
    private LazyFactory() { }

    public static <T> Lazy<T> createLazy(final Supplier<T> supplier) {
        return new LazySingleThread<>(supplier);
    }

    public static <T> Lazy<T> createLazyMultithreading(final Supplier<T> supplier) {
        return new LazyMultithreading<>(supplier);
    }

    public static <T> Lazy<T> createLazyLockFree(final Supplier<T> supplier) {
        return new LazyLockFree<>(supplier);
    }
}
