package ru.spbau.mit;

import ru.spbau.mit.utils.AbstractLazyFactory;
import ru.spbau.mit.utils.TestSupplier;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import ru.spbau.mit.utils.TestThread;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Supplier;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class MultithreadedLazyTest {
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {(AbstractLazyFactory) LazyFactory::createSingletonLazy, false},
                {(AbstractLazyFactory) LazyFactory::createLockFreeLazy, true}
        });
    }

    private final AbstractLazyFactory factory;
    private final boolean shouldHaveConflicts;

    public MultithreadedLazyTest(AbstractLazyFactory factory, boolean shouldHaveConflicts) {
        this.factory = factory;
        this.shouldHaveConflicts = shouldHaveConflicts;
    }

    private final int THREADS_COUNT = 2000;
    private final int LAZIES_COUNT = 2000;

    private Integer[][] runThreads(TestSupplier[] suppliers) throws InterruptedException {
        final TestThread[] ths = new TestThread[THREADS_COUNT];

        final ArrayList<Lazy<Integer>> lazies = new ArrayList<>();
        final Integer[][] results = new Integer[ths.length][LAZIES_COUNT];

        for (TestSupplier supplier : suppliers) {
            lazies.add(factory.createLazy(supplier));
        }

        for (int i = 0; i < ths.length; i++) {
            final int id = i;
            ths[i] = new TestThread() {
                @Override
                public void run() throws Exception {
                    ArrayList<Integer> order = new ArrayList<Integer>();
                    for (int i = 0; i < LAZIES_COUNT; i++) {
                        order.add(i);
                    }
                    for (Integer lazy : order) {
                        results[id][lazy] = lazies.get(lazy).get();
                    }
                }
            };
        }

        for (TestSupplier s : suppliers) {
            assertEquals(0, s.getCallsCount());
        }
        for (TestThread th : ths) {
            th.start();
        }
        for (TestThread th : ths) {
            th.join();
        }
        return results;
    }

    private void test(Supplier<TestSupplier> supplierFactory, boolean shouldHaveNull) throws InterruptedException {
        final TestSupplier[] suppliers = new TestSupplier[LAZIES_COUNT];
        for (int i = 0; i < suppliers.length; i++) {
            suppliers[i] = supplierFactory.get();
        }
        Integer[][] results = runThreads(suppliers);

        boolean haveNull = false;
        for (int lazy = 0; lazy < LAZIES_COUNT; lazy++) {
            for (int th = 0; th < THREADS_COUNT; th++) {
                assertEquals(results[0][lazy], results[th][lazy]);
            }
            if (results[0][lazy] == null) {
                haveNull = true;
            }
        }
        assertEquals(shouldHaveNull, haveNull);

        boolean haveConflicts = false;
        for (TestSupplier s : suppliers) {
            assertTrue(s.getCallsCount() >= 1);
            haveConflicts = haveConflicts || s.getCallsCount() > 1;
            assertTrue(s.getCallsCount() < THREADS_COUNT); // check for caching
        }
        assertEquals(shouldHaveConflicts, haveConflicts);
    }

    @Test
    public void testComputation() throws InterruptedException {
        test(TestSupplier::new, false);
    }

    @Test
    public void testNullFirstTen() throws InterruptedException {
        test(() -> new TestSupplier(10), true);
    }

    @Test
    public void testNullFirst() throws InterruptedException {
        test(() -> new TestSupplier(1), true);
    }
}
