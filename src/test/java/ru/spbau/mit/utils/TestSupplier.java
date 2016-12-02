package ru.spbau.mit.utils;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class TestSupplier implements Supplier<Integer> {
    private final AtomicInteger value = new AtomicInteger(1);
    private final int nullOnFirstSteps;

    public TestSupplier() {
        nullOnFirstSteps = 0;
    }

    public TestSupplier(int nullOnFirstSteps) {
        this.nullOnFirstSteps = nullOnFirstSteps;
    }

    public Integer get() {
        Thread.yield();
        int result = value.getAndIncrement();
        Thread.yield();
        if (result <= nullOnFirstSteps) {
            return null;
        } else {
            return result;
        }
    }

    public int getCallsCount() {
        return value.get() - 1;
    }
}
