package ru.spbau.mit;

import java.util.function.Supplier;

public interface LazyFactoryFromSupplier {
    <T> Lazy<T> createLazy(Supplier<T> supplier);
}
