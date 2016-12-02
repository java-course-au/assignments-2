package ru.spbau.mit;

import java.util.function.Supplier;

public final class LazyFactory {
    private LazyFactory() {
    }

    public static <T> Lazy<T> createSingleThreadedLazy(Supplier<T> supplier) {
        return new SingleThreadedLazy<T>(supplier);
    }

    public static <T> Lazy<T> createSingletonLazy(Supplier<T> supplier) {
        return new SingletonLazy<T>(supplier);
    }

    public static <T> Lazy<T> createLockFreeLazy(Supplier<T> supplier) {
        return new LockFreeLazy<T>(supplier);
    }
}
