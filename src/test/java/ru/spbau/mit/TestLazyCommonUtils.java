package ru.spbau.mit;

import org.junit.Assert;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.function.Supplier;

import static junit.framework.TestCase.fail;

public final class TestLazyCommonUtils {
    private TestLazyCommonUtils() {
    }

    public enum LazyCheckerMode {
        ONE_ITERATION,
        SEVERAL_ITERATIONS,
        SEVERAL_ITERATIONS_DIFF
    }

    private static final class LazySameChecker implements Supplier<Object> {
        private int iteration = 0;
        private LazyCheckerMode testMode;
        private Object retValue;
        private boolean valid = false;

        private static final int MODULO = 10;

        private LazySameChecker(LazyCheckerMode testMode, Object retValue) {
            this.testMode = testMode;
            this.retValue = retValue;
        }

        @Override
        public synchronized Object get() {
            if (!valid || (testMode == LazyCheckerMode.ONE_ITERATION && iteration > 0)) {
                fail();
            }
            iteration++;
            if (testMode == LazyCheckerMode.ONE_ITERATION || testMode == LazyCheckerMode.SEVERAL_ITERATIONS
                    || iteration % MODULO == 0) {
                return retValue;
            }
            return null;
        }

        public void setValid() {
            valid = true;
        }
    }

    private static final int NRVALUE = 10000;

    private static Set<Object> putTasksToExecutorServiceAndCalculate(
            final Lazy<Object> lazy, ExecutorService service) {
        final Set<Object> set = new HashSet<>();
        final List<Future<Object>> futures = new ArrayList<Future<Object>>();
        for (int i = 0; i < NRVALUE; i++) {
            futures.add(service.submit(() -> lazy.get()));
        }
        futures.forEach(f -> {
            try {
                set.add(f.get());
            } catch (Exception e) {
            }
        });
        return set;
    }

    public static void check(
            Function<Supplier<Object>, Lazy<Object>> lazyMaker, int threadsNum,
            LazyCheckerMode testMode, Object value) {
        ExecutorService service = Executors.newFixedThreadPool(threadsNum);
        LazySameChecker checker = new LazySameChecker(testMode, value);
        final Lazy<Object> lazy = lazyMaker.apply(checker);
        checker.setValid();
        Set<Object> set = putTasksToExecutorServiceAndCalculate(lazy, service);
        Assert.assertEquals(set.size(), 1);
        if (testMode == LazyCheckerMode.ONE_ITERATION || testMode == LazyCheckerMode.SEVERAL_ITERATIONS) {
            assert (value == set.iterator().next());
        }
    }


}
