import javafx.util.Pair;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Supplier;

/**
 * Created by n_buga on 14.02.16.
 */
public class LazyFactory {
    public static <T> Lazy<T> createLazySimple(final Supplier<T> supplier) {
        return new Lazy<T>() {
            T result;
            boolean isResult = false;
            public T get() {
                if (!isResult) {
                    isResult = true;
                    return result = supplier.get();
                } else
                    return result;
            }
        };
    }
    public static <T> Lazy<T> createLazyThreads(final Supplier<T> supplier) {
        return new Lazy<T>() {
            private T result;
            private boolean isResult;
            public synchronized T get() {
                if (!isResult) {
                    isResult = true;
                    result = supplier.get();
                }
                return result;
            }
        };
    }
    public static  <T> Lazy<T> createLazyLockFree(final Supplier<T> supplier) {
        class LockFreeLazy implements Lazy {
            private volatile Object result = null;
            private AtomicReferenceFieldUpdater updaterResult = AtomicReferenceFieldUpdater.newUpdater(LockFreeLazy.class,
                    Object.class, "result");
            public T get() {
                updaterResult.compareAndSet(this, null,
                        (Object) new Pair<>(supplier.get(), true));
                return ((Pair<T, Boolean>)result).getKey();
            }
        }
        return (Lazy<T>) new LockFreeLazy();
    }
}
