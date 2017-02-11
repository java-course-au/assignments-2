import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Supplier;

public final class LazyFactory {

    private LazyFactory() {}

    private static final Object NONE = new Object();

    private static class LazyOneThread<T> implements Lazy<T> {
        private T result = (T) NONE;
        private Supplier<T> supplier;

        public LazyOneThread(Supplier<T> s) {
            supplier = s;
        }

        @Override
        public T get() {
            if (result == NONE) {
                result = supplier.get();
                supplier = null;
            }
            return result;
        }
    }


    public static <T> Lazy<T> createLazyOneThread(Supplier<T> supplier) {
        return new LazyOneThread<T>(supplier);
    }

    private static class LazyMultithread<T> implements Lazy<T> {

        private volatile T result = (T) NONE;
        private Supplier<T> supplier;

        public LazyMultithread(Supplier<T> s) {
            supplier = s;
        }

        @Override
        public T get() {
            if (result == NONE) {
                synchronized (this) {
                    if (result == NONE) {
                        result = supplier.get();
                        supplier = null;
                    }
                }
            }
            return result;
        }
    }

    public static <T> Lazy<T> createLazyMultithread(Supplier<T> supplier) {
        return new LazyMultithread<T>(supplier);
    }

    private static class LazyLockfree<T> implements Lazy<T> {

        private static final AtomicReferenceFieldUpdater<LazyLockfree, Object> UPDATER =
                AtomicReferenceFieldUpdater.newUpdater(LazyLockfree.class, Object.class, "result");

        private volatile T result = (T) NONE;
        private Supplier<T> supplier;

        public LazyLockfree(Supplier<T> s) {
            supplier = s;
        }

        @Override
        public T get() {
            if (result == NONE) {
                Supplier<T> local = supplier;
                if (local != null) {
                    if (UPDATER.compareAndSet(this, NONE, local.get())) {
                        supplier = null;
                    }
                }
            }
            return result;
        }
    }

    public static <T> Lazy<T> createLazyLockfree(Supplier<T> supplier) {
        return new LazyLockfree<>(supplier);
    }
}
