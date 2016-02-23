import java.util.function.Supplier;

/**
 * Created by olga on 11.02.16.
 */
final class LazyFactory {
    private LazyFactory() {
    }

    static <T> Lazy<T> createOneThreadLazy(Supplier<T> supplier) {
        return new OneThreadLazy<T>(supplier);
    }

    static <T> Lazy<T> createMultiThreadLazy(Supplier<T> supplier) {
        return new MultiThreadLazy<T>(supplier);
    }

    static <T> Lazy<T> createLockFreeLazy(Supplier<T> supplier) {
        return new LockFreeLazy<T>(supplier);
    }
}
