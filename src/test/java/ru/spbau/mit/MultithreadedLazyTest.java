package ru.spbau.mit;

import ru.spbau.mit.utils.AbstractLazyFactory;
import ru.spbau.mit.utils.TestSupplier;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import ru.spbau.mit.utils.ExceptionCatchingThreadWrapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Supplier;

import static org.junit.Assert.*;

/**
 * There are three tests for each factory, each test is characterized with a strategy for Lazy object: on
 * first several calls it return nulls, and afterwards it returns different integers.
 * There are three strategies: no nulls (checks basic case), ten nulls in the beginning (checks nulls handling)
 * and a single null in the beginning (checks races between nulls and non-nulls).
 *
 * Each test is run for <code>TEST_REPEATS</code> times. The <code>test</code> method is the one who
 * prepares the test and calls <code>runThreads</code> method which creates threads and performs calculations,
 * then control goes back to the <code>test</code> method which checks results for correctness.
 *
 * During each test <code>THREADS_COUNT</code> threads are created and <code>LAZIES_COUNT</code> distinct
 * lazy expressions are created. Then all threads are started and each of them tries to calculate value of
 * each lazy expression. Results are stored into 2D-array which is returned by<code>runThreads</code>.
 * <code>results[i][j]</code> is what i-th thread received as an answer from j-th lazy object.
 *
 * After all threads are finished we check that for every lazy all results collected by different threads
 * are equal. For singleton lazy we also check that each lazy object has been evaluated at most once.
 */
@RunWith(Parameterized.class)
public class MultithreadedLazyTest {
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {(AbstractLazyFactory) LazyFactory::createSingletonLazy, true},
                {(AbstractLazyFactory) LazyFactory::createLockFreeLazy, false}
        });
    }

    private final AbstractLazyFactory factory;
    private final boolean shouldComputeExactlyOnce;

    public MultithreadedLazyTest(AbstractLazyFactory factory, boolean shouldComputeExactlyOnce) {
        this.factory = factory;
        this.shouldComputeExactlyOnce = shouldComputeExactlyOnce;
    }

    private static final int THREADS_COUNT = 1000;
    private static final int LAZIES_COUNT = 1000;
    private static final int TEST_REPEATS = 20;

    private Integer[][] runThreads(TestSupplier[] suppliers) throws InterruptedException {
        final ExceptionCatchingThreadWrapper[] ths = new ExceptionCatchingThreadWrapper[THREADS_COUNT];

        final ArrayList<Lazy<Integer>> lazies = new ArrayList<>();
        final Integer[][] results = new Integer[ths.length][LAZIES_COUNT];

        for (TestSupplier supplier : suppliers) {
            lazies.add(factory.createLazy(supplier));
        }

        for (int i = 0; i < ths.length; i++) {
            final int id = i;
            ths[i] = new ExceptionCatchingThreadWrapper() {
                @Override
                public void run() throws Exception {
                    for (int lazy = 0; lazy < LAZIES_COUNT; lazy++) {
                        results[id][lazy] = lazies.get(lazy).get();
                    }
                }
            };
        }

        for (TestSupplier s : suppliers) {
            assertEquals(0, s.getCallsCount());
        }
        for (ExceptionCatchingThreadWrapper th : ths) {
            th.start();
        }
        for (ExceptionCatchingThreadWrapper th : ths) {
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

        for (int lazy = 0; lazy < LAZIES_COUNT; lazy++) {
            for (int th = 0; th < THREADS_COUNT; th++) {
                assertEquals(results[0][lazy], results[th][lazy]);
            }
        }
        assertEquals(shouldHaveNull, Arrays.stream(results[0]).anyMatch(r -> r == null));

        for (TestSupplier s : suppliers) {
            if (shouldComputeExactlyOnce) {
                assertTrue(s.getCallsCount() == 1);
            } else {
                assertTrue(s.getCallsCount() >= 1);
                assertTrue(s.getCallsCount() < THREADS_COUNT); // check for caching
            }
        }
    }

    @Test
    public void testComputation() throws InterruptedException {
        for (int i = 0; i < TEST_REPEATS; i++) {
            test(TestSupplier::new, false);
        }
    }

    @Test
    public void testNullFirstTen() throws InterruptedException {
        for (int i = 0; i < TEST_REPEATS; i++) {
            //CHECKSTYLE.OFF: MagicNumber
            test(() -> new TestSupplier(10), true);
            //CHECKSTYLE.ON: MagicNumberr
        }
    }

    @Test
    public void testNullFirst() throws InterruptedException {
        for (int i = 0; i < TEST_REPEATS; i++) {
            test(() -> new TestSupplier(1), true);
        }
    }
}
