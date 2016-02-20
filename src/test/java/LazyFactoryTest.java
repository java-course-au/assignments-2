/**
 * Created by n_buga on 15.02.16.
 */

import org.junit.Test;

import java.util.Random;
import java.util.function.Supplier;

public class LazyFactoryTest {

    @Test
    public void simpleTest() {

        final Random random = new Random();
        Lazy<Integer>[] listLazy = new Lazy[4];
        Supplier<Integer> supplier = new Supplier<Integer>() {
            @Override
            public Integer get() {
                return random.nextInt(1000);
            }
        };

        for (int i = 0; i < 4; i++) {
            listLazy[i] = LazyFactory.createLazySimple(supplier);
        }

        for (int j = 0; j < 4; j++) {
            for (int k = 0; k < 4; k++) {
                assert (listLazy[j].get().equals(listLazy[j].get()));
            }
        }
    }

    @Test
    public void threadsTest() {

        final Random random = new Random();
        Supplier<Integer> supplier = new Supplier<Integer>() {
            @Override
            public Integer get() {
                return random.nextInt(1000);
            }
        };

        final Lazy<Integer> lazy = LazyFactory.createLazyThreads(supplier);
        Thread[] threads = new Thread[4];
        final Integer[] result = new Integer[4];

        for (int j = 0; j < 4; j++) {
            final int tmp = j;
            threads[j] = new Thread(new Runnable() {
                @Override
                public void run() {
                    result[tmp] = lazy.get();
                }
            });
        }

        for (int j = 0; j < 4; j++) {
            threads[j].start();
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (int j = 0; j < 3; j++) {
            assert (result[j].equals(result[j + 1]));
        }
    }

    @Test
    public void lockFreeTest() {

        final Random random = new Random();
        Supplier<Integer> supplier = new Supplier<Integer>() {
            @Override
            public Integer get() {
                return random.nextInt(1000);
            }
        };

        final Lazy<Integer> lazy = LazyFactory.createLazyLockFree(supplier);
        Thread[] threads = new Thread[4];
        final Integer[] result = new Integer[4];

        for (int j = 0; j < 4; j++) {
            final int tmp = j;
            threads[j] = new Thread(new Runnable() {
                @Override
                public void run() {
                    result[tmp] = lazy.get();
                }
            });
        }

        for (int j = 0; j < 4; j++) {
            threads[j].start();
        }

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (int j = 0; j < 3; j++) {
            assert (result[j].equals(result[j + 1]));
        }
    }
}
