package ru.spbau.mit;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Supplier;

public class LazyFactoryTest {
    private static int counter = 0;
    private Supplier<Integer> simpleSupplier = new Supplier<Integer>() {
        @Override
        public synchronized Integer get() {
            counter++;
            return counter;
        }
    };
    private Supplier<String> supplierReturnsNull = new Supplier<String>() {
        @Override
        public String get() {
            return null;
        }
    };
    private Supplier<Integer> concurrentSupplier = new Supplier<Integer>() {
        @Override
        public synchronized Integer get() {
            Thread.yield();
            counter++;
            return counter;
        }
    };
    private Supplier<Integer> supplierForMultiThread = new Supplier<Integer>() {
        private boolean wasCalled = false;

        @Override
        public synchronized Integer get() {
            if (wasCalled) {
                fail("Supplier was called more then one time");
            }
            Thread.yield();
            wasCalled = true;
            counter++;
            return counter;
        }
    };

    private List<Future<Integer>> createTasks(final Lazy<Integer> lazy) {
        List<Future<Integer>> list = new ArrayList<>();
        ExecutorService executorService = Executors.newFixedThreadPool(20);
        for (int i = 0; i < 100; i++) {
            list.add(executorService.submit(new Callable<Integer>() {
                                                private Integer result;
                                                private boolean calculated = false;

                                                @Override
                                                public Integer call() throws Exception {
                                                    for (int i = 0; i < 20; i++) {
                                                        Integer newResult = lazy.get();
                                                        if (!calculated) {
                                                            calculated = true;
                                                            result = newResult;
                                                        }
                                                        assertTrue(result == newResult);
                                                    }
                                                    return result;
                                                }
                                            }
            ));
        }

        return list;
    }

    @Test
    public void testCreateLazyOneThread_simpleSupplier() {
        Lazy<Integer> lazy = LazyFactory.createLazyOneThread(simpleSupplier);
        counter = 0;
        Integer result = lazy.get();
        assertEquals(Integer.valueOf(1), result);
        assertTrue(result == lazy.get());
    }

    @Test
    public void testCreateLazyOneThread_returnNull() {
        Lazy<String> lazy = LazyFactory.createLazyOneThread(supplierReturnsNull);
        String result = lazy.get();
        assertTrue(result == null);
        assertTrue(lazy.get() == null);
    }

    @Test
    public void testCreateLazyMultiThread_simpleSupplier() {
        Lazy<Integer> lazy = LazyFactory.createLazyMultiThread(simpleSupplier);
        counter = 0;
        Integer result = lazy.get();
        assertEquals(Integer.valueOf(1), result);
        assertTrue(result == lazy.get());
    }

    @Test
    public void testCreateLazyMultiThread_returnNull() {
        Lazy<String> lazy = LazyFactory.createLazyMultiThread(supplierReturnsNull);
        String result = lazy.get();
        assertTrue(result == null);
        assertTrue(lazy.get() == null);
    }

    @Test
    public void testCreateLazyLockFree_simpleSupplier() {
        Lazy<Integer> lazy = LazyFactory.createLazyLockFree(simpleSupplier);
        counter = 0;
        Integer result = lazy.get();
        assertEquals(Integer.valueOf(1), result);
        assertTrue(result == lazy.get());
    }

    @Test
    public void testCreateLazyLockFree_returnNull() {
        Lazy<String> lazy = LazyFactory.createLazyLockFree(supplierReturnsNull);
        String result = lazy.get();
        assertTrue(result == null);
        assertTrue(lazy.get() == null);
    }

    @Test
    public void testCreateLazyMultiThread_concurrency() {
        final Lazy<Integer> lazy = LazyFactory.createLazyMultiThread(supplierForMultiThread);
        List<Future<Integer>> list = createTasks(lazy);
        try {
            Integer result = list.get(0).get();
            for (int i = 0; i < 100; i++) {
                assertTrue(result == list.get(i).get());
            }
        } catch (ExecutionException e) {
            fail(e.getMessage());
        } catch (InterruptedException ignored) {
        }
    }

    @Test
    public void testCreateLazyLockFree_concurrency() {
        final Lazy<Integer> lazy = LazyFactory.createLazyLockFree(concurrentSupplier);
        List<Future<Integer>> list = createTasks(lazy);
        try {
            Integer result = list.get(0).get();
            for (int i = 0; i < 100; i++) {
                assertTrue(result == list.get(i).get());
            }
        } catch (ExecutionException e) {
            fail(e.getMessage());
        } catch (InterruptedException ignored) {
        }
    }
}