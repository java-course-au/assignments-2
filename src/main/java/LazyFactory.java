import java.util.function.Supplier;

/**
 * Created by olga on 11.02.16.
 */
class LazyFactory {
    <T> Lazy<T> createOneThreadLazy(Supplier<T> supplier) {
        return new OneThreadLazy<T>(supplier);
    }

    <T> Lazy<T> createMultiThreadLazy(Supplier<T> supplier) {
        return new MultiThreadLazy<T>(supplier);
    }

    <T> Lazy<T> createLockFreeLazy(Supplier<T> supplier) {
        return new LockFreeLazy<T>(supplier);
    }
}
