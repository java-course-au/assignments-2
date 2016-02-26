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

    private <T> void checkContractsMultithread(final Function<Supplier<Object>, Lazy<Object>> createLazy)
            throws InterruptedException {
        try {
            doTestMultithread(createLazy, new SupplierFunctions()::doCheckMultipleExecutions, NUM_THREADS);
        } catch (MultipleExecutionsException e) {
            throw new RuntimeException("While checking multiple executions multithread", e);
        }

        doTestMultithread(createLazy, new SupplierFunctions()::doCheckRandObjects, NUM_THREADS);

        doTestMultithread(createLazy, new SupplierFunctions()::doCheckReturnNull, NUM_THREADS);

        try {
            createLazy.apply(LAZY_SUPPLIER);
        } catch (NotLazyException e) {
            throw new RuntimeException("Not lazy multithread", e);
        }

        try {
            doTestMultithread(createLazy, new SupplierFunctions()::doCheckMultipleExecutionsWithNull,
                    NUM_THREADS);
        } catch (MultipleExecutionsException e) {
            throw new RuntimeException("Multiple executions multithread", e);
        }
    }

    private <T> void checkContractsOneThread(final Function<Supplier<Object>, Lazy<Object>> createLazy) {
        doTestOneThread(createLazy, new SupplierFunctions()::doCheckRandObjects, NUM_THREADS);

        doTestOneThread(createLazy, new SupplierFunctions()::doCheckReturnNull, NUM_THREADS);

        try {
            createLazy.apply(LAZY_SUPPLIER);
        } catch (NotLazyException e) {
            throw new RuntimeException("Not lazy one thread", e);
        }

        try {
            doTestOneThread(LazyFactory::createLazyOneThread, new SupplierFunctions()::doCheckMultipleExecutionsWithNull,
                    NUM_THREADS);
        } catch (MultipleExecutionsException e) {
            throw new RuntimeException("Multiple executions one thread", e);
        }
    }

    // does test one-threaded Lazy
    @Test
    public void testOneThread() {
        checkContractsOneThread(LazyFactory::createLazyOneThread);
    }

    // does test multithreaded Lazy
    @Test
    public void testMultithread() throws InterruptedException {
        checkContractsMultithread(LazyFactory::createLazyMultithread);
    }

    // does test lockfree Lazy
    @Test
    public void testLockfree() throws InterruptedException {
        checkContractsMultithread(LazyFactory::createLazyLockfree);
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