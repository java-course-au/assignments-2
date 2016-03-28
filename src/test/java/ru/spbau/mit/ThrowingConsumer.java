package ru.spbau.mit;

@FunctionalInterface
interface ThrowingConsumer<T, E extends Throwable> {
    void consume(T s) throws E;
}
