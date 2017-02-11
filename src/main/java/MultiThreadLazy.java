import java.util.function.Supplier;

/**
 * Created by olga on 11.02.16.
 */
public class MultiThreadLazy<T> implements Lazy<T> {
    private static final Object NONE = new Object();
    private Supplier<T> supplier;
    private volatile T result = (T) NONE;

    MultiThreadLazy(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public T get() {
        if (result == (T) NONE) {
            synchronized (this) {
                if (result == (T) NONE) {
                    result = supplier.get();
                    supplier = null;
                }
            }
        }
        return result;
    }
}
