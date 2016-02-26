import org.junit.Test;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Supplier;

import static junit.framework.Assert.assertTrue;

public class LazyFactoryTest {

    private static final int NUM_THREADS = 10;
    private final Supplier<Object> LAZY_SUPPLIER = () -> {
        new SupplierFunctions().doCheckLazyLaziness();
        return null;
    };


    // the two following functions accept a factory, which producec Lazy's,
// and a function, on which this Lazy will be tested
    private <T> void doTestOneThread(final Function<Supplier<T>, Lazy<T>> createLazy,
                                     final Callable<T> supplierFunction, int cnt) {
        // checks if the function always returns the same object
        Lazy<T> lazy = createLazy.apply(() -> {
            T result;
            try {
                result = supplierFunction.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return result;
        });
        T result = lazy.get();

        for (int i = 0; i < cnt; i++) {
            assertTrue(result == lazy.get());
        }
    }

    private <T> void doTestMultithread(final Function<Supplier<T>, Lazy<T>> createLazy,
                                       final Callable<T> supplierFunction, int cnt) throws InterruptedException {

        final Lazy<T> lazy = createLazy.apply(() -> {
            T result;
            try {
                result = supplierFunction.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return result;
        });

        class MyRunnable implements Runnable {

            public MultipleExecutionsException exception = null;
            private T result = lazy.get();

            @Override
            public void run() {
                try {
                    assertTrue(result == lazy.get());
                } catch (MultipleExecutionsException e) {
                    exception = e;
                }
            }
        }

        ArrayList<Thread> lazyExecutors = new ArrayList<>();
        ArrayList<MyRunnable> myRunnables = new ArrayList<>();

        for (int i = 0; i < cnt; i++) {
            myRunnables.add(new MyRunnable());
            Thread lazyExecutor = new Thread(myRunnables.get(i));
            lazyExecutors.add(lazyExecutor);
            lazyExecutor.start();
        }
        for (int i = 0; i < cnt; i++) {
            lazyExecutors.get(i).join();
            if (myRunnables.get(i).exception != null) {
                throw myRunnables.get(i).exception;
            }
        }
    }

    // does test one-threaded Lazy
    @Test
    public void testCreateLazyOneThread() throws Exception {
        doTestOneThread(LazyFactory::createLazyOneThread, new SupplierFunctions()::doCheckRandObjects, NUM_THREADS);
    }

    @Test
    public void testCreateLazyOneThreadReturnsNull() {
        doTestOneThread(LazyFactory::createLazyOneThread, new SupplierFunctions()::doCheckReturnNull, NUM_THREADS);
    }

    @Test
    public void testCreateLazyOneThreadLazyLaziness() throws Exception {
        LazyFactory.createLazyOneThread(LAZY_SUPPLIER);
    }

    @Test
    public void testCreateLazyOneThreadMultipleExecutionsWithNull() throws Exception {
        doTestOneThread(LazyFactory::createLazyOneThread, new SupplierFunctions()::doCheckMultipleExecutionsWithNull,
                NUM_THREADS);
    }

    // does test multithreaded Lazy
    @Test
    public void testCreateLazyMultithreadMultipleExecution() throws InterruptedException {
        doTestMultithread(LazyFactory::createLazyMultithread, new SupplierFunctions()::doCheckMultipleExecutions,
                NUM_THREADS);
    }

    @Test
    public void testCreateLazyMultithread() throws InterruptedException {
        doTestMultithread(LazyFactory::createLazyMultithread, new SupplierFunctions()::doCheckRandObjects, NUM_THREADS);
    }

    @Test
    public void testCreateLazyMultithreadReturnsNull() throws InterruptedException {
        doTestMultithread(LazyFactory::createLazyMultithread, new SupplierFunctions()::doCheckReturnNull, NUM_THREADS);
    }

    @Test
    public void testCreateLazyMultithreadLazyLaziness() throws Exception {
        LazyFactory.createLazyMultithread(LAZY_SUPPLIER);
    }

    @Test
    public void testCreateLazyMultithreadMultipleExecutionsWithNull() throws Exception {
        doTestOneThread(LazyFactory::createLazyMultithread, new SupplierFunctions()::doCheckMultipleExecutionsWithNull,
                NUM_THREADS);
    }

    // does test lockfree Lazy
    @Test
    public void testCreateLazyLockfreeMultipleExecution() throws InterruptedException {
        doTestMultithread(LazyFactory::createLazyLockfree, new SupplierFunctions()::doCheckMultipleExecutions,
                NUM_THREADS);
    }

    @Test
    public void testCreateLazyLockfree() throws InterruptedException {
        doTestMultithread(LazyFactory::createLazyLockfree, new SupplierFunctions()::doCheckRandObjects, NUM_THREADS);
    }

    @Test
    public void testCreateLazyLockfreeReturnsNull() throws InterruptedException {
        doTestMultithread(LazyFactory::createLazyLockfree, new SupplierFunctions()::doCheckReturnNull, NUM_THREADS);
    }

    @Test
    public void testCreateLazyLockfreeLazyLaziness() throws Exception {
        LazyFactory.createLazyLockfree(LAZY_SUPPLIER);
    }

    @Test
    public void testCreateLazyLockfreeMultipleExecutionsWithNull() throws Exception {
        doTestOneThread(LazyFactory::createLazyLockfree, new SupplierFunctions()::doCheckMultipleExecutionsWithNull,
                NUM_THREADS);
    }

    class MultipleExecutionsException extends RuntimeException {
    }

    class NotLazyException extends RuntimeException {
    }

    class SupplierFunctions {

        public Boolean executed = false;

        public Object doCheckMultipleExecutions() {
            if (executed) {
                throw new MultipleExecutionsException();
            }
            executed = true;
            return new Object();
        }

        public Object doCheckMultipleExecutionsWithNull() {
            doCheckMultipleExecutions();
            return null;
        }

        public Object doCheckReturnNull() {
            return null;
        }

        public Object doCheckRandObjects() {
            Random gen = new Random();
            if (gen.nextInt() % 2 == 1) {
                return new Object();
            }
            return null;
        }

        public Object doCheckLazyLaziness() {
            throw new NotLazyException();
        }
    }
}