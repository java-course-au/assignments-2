import org.junit.Test;

import java.util.ArrayList;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.Assert.*;

/**
 * Created by olga on 12.02.16.
 */
public class LazyTest {
    private void checkNull(Function<Supplier, Lazy> factory) {
        final int[] countGet = {0};
        Supplier<String> nullSupplier = new Supplier<String>() {
            @Override
            public String get() {
                ++countGet[0];
                return null;
            }
        };

        Lazy lazy = factory.apply(nullSupplier);

        assertEquals(0, countGet[0]);
        assertSame(lazy.get(), lazy.get());
        assertEquals(nullSupplier.get(), lazy.get());
        assertEquals(2, countGet[0]);
    }

    private void checkSameObject(Function<Supplier, Lazy> factory) {
        final int[] countGet = {0};
        Supplier<int[]> arraySupplier = new Supplier<int[]>() {
            @Override
            public int[] get() {
                ++countGet[0];
                return new int[]{1, 2, 2};
            }
        };

        Lazy lazy = factory.apply(arraySupplier);

        assertEquals(0, countGet[0]);
        assertSame(lazy.get(), lazy.get());
        assertArrayEquals(arraySupplier.get(), (int[]) lazy.get());
        assertEquals(2, countGet[0]);
    }

    private void checkOneThreadContract(Function<Supplier, Lazy> factory) {
        checkNull(factory);
        checkSameObject(factory);
    }

    private void checkMultiThreadContract(Function<Supplier, Lazy> factory, Boolean checkOneCallGet) {
        final int[] countGet = {0};

        final Supplier<String> mSupplier = new Supplier<String>() {
            public String get() {
                ++countGet[0];
                return "abacaba";
            }
        };

        final Lazy lazy = factory.apply(mSupplier);

        final ArrayList<Thread> threads = new ArrayList<Thread>();

        final String[] result = {null};
        final int threadsCount = 10;
        for (int i = 0; i < threadsCount; i++) {
            threads.add(new Thread(new Runnable() {
                public void run() {
                    if (result[0] != null) {
                        assertSame(result[0], lazy.get());
                    }
                    assertSame(lazy.get(), lazy.get());
                    result[0] = (String) lazy.get();
                    if (checkOneCallGet) {
                        assertEquals(countGet[0], 1);
                    }
                }
            }));
            threads.get(i).start();
        }
    }

    @Test
    public void testOneThreadLazy() {
        checkOneThreadContract(LazyFactory::createOneThreadLazy);
    }

    @Test
    public void testMultiThreadLazy() {
        checkOneThreadContract(LazyFactory::createMultiThreadLazy);
        checkMultiThreadContract(LazyFactory::createMultiThreadLazy, true);
    }

    @Test
    public void testLockFreeLazy() {
        checkOneThreadContract(LazyFactory::createLockFreeLazy);
        checkMultiThreadContract(LazyFactory::createLockFreeLazy, false);
    }
}
