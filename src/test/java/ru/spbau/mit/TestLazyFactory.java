package ru.spbau.mit;

import org.junit.Test;

import java.util.function.Function;
import java.util.function.Supplier;

public class TestLazyFactory {

    public void doTestSimple(Function<Supplier<Object>, Lazy<Object>> factory, int threadsNum,
                             TestLazyCommonUtils.LazyCheckerMode testMode) {
        TestLazyCommonUtils.check(factory, threadsNum, testMode, new Object());
    }

    public void doTestNull(Function<Supplier<Object>, Lazy<Object>> factory, int threadsNum,
                           TestLazyCommonUtils.LazyCheckerMode testMode) {
        TestLazyCommonUtils.check(LazyFactory::createLazySingleThread, threadsNum, testMode, null);
    }

    public void runBasicTests(Function<Supplier<Object>, Lazy<Object>> factory, int threads,
                              TestLazyCommonUtils.LazyCheckerMode testMode) {
        doTestSimple(factory, threads, testMode);
        doTestNull(factory, threads, testMode);
    }

    private static final int MULTITHREAD_TEST_THREADS = 100;


    @Test
    public void testLazySingleThread() {
        runBasicTests(LazyFactory::createLazySingleThread, 1,
                TestLazyCommonUtils.LazyCheckerMode.ONE_ITERATION);
    }

    @Test
    public void testLazyMultiThreadSync() {
        runBasicTests(LazyFactory::createLazyMultiThreadSync, MULTITHREAD_TEST_THREADS,
                TestLazyCommonUtils.LazyCheckerMode.ONE_ITERATION);
    }

    @Test
    public void testLazyMultiThreadWaitFree() {
        runBasicTests(LazyFactory::createLazyMultiThreadWaitFree, MULTITHREAD_TEST_THREADS,
                TestLazyCommonUtils.LazyCheckerMode.SEVERAL_ITERATIONS);
        TestLazyCommonUtils.check(LazyFactory::createLazyMultiThreadWaitFree,
                MULTITHREAD_TEST_THREADS,
                TestLazyCommonUtils.LazyCheckerMode.SEVERAL_ITERATIONS_DIFF, new Object());
    }
}
