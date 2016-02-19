import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Random;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;

/**
 * Created by olga on 12.02.16.
 */
public class LazyTest {
    private <T> void checkLazy(Supplier<T> mSupplier, final int[] countGet) throws InvocationTargetException, IllegalAccessException {
        LazyFactory mLazyFactory = new LazyFactory();
        ArrayList<Lazy<Integer> > lazies = new ArrayList<Lazy<Integer>>();
        Method[] mt = mLazyFactory.getClass().getDeclaredMethods();
        for (int i = 0; i < mt.length; ++i) {
            if (mt[i].getReturnType() == Lazy.class) {
                lazies.add((Lazy<Integer>) mt[i].invoke(mLazyFactory, mSupplier));
            }
        }
        for (Lazy<Integer> currentLazy : lazies) {
            assertEquals(0, countGet[0]);
            assertEquals(currentLazy.get(), currentLazy.get());
            assertEquals(mSupplier.get(), currentLazy.get());
            assertEquals(2, countGet[0]);
            countGet[0] = 0;
        }
    }

    @Test
    public void testNull() throws InvocationTargetException, IllegalAccessException {
        final int[] countGet = {0};
        Supplier<Integer> mSupplier = new Supplier<Integer>() {
            public Integer get() {
                ++countGet[0];
                return null;
            }
        };
        checkLazy(mSupplier, countGet);
    }

    @Test
    public void test() throws InvocationTargetException, IllegalAccessException {
        final int[] countGet = {0};
        Supplier<Integer> mSupplier = new Supplier<Integer>() {
            public Integer get() {
                ++countGet[0];
                return 179179;
            }
        };
        checkLazy(mSupplier, countGet);
    }

    @Test
    public void testThread() {
        final Random random = new Random(42);

        final Supplier<Integer> mSupplier = new Supplier<Integer>() {
            public Integer get() {
                return random.nextInt(10000);
            }
        };

        LazyFactory mLazyFactory = new LazyFactory();
        final ArrayList<Lazy<Integer> > lazies = new ArrayList<Lazy<Integer>>();
        lazies.add(mLazyFactory.createMultiThreadLazy(mSupplier));
        lazies.add(mLazyFactory.createLockFreeLazy(mSupplier));

        final ArrayList<Thread> threads = new ArrayList<Thread>();
        for (int i = 0; i < 10; i++) {
            threads.add(new Thread(new Runnable() {
                public void run() {
                    for (int j = 0; j < 2; j++) {
                        assertEquals(lazies.get(j).get(), lazies.get(j).get());
                    }
                }
            }));
            threads.get(i).start();
        }
    }
}
