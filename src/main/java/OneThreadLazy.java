import java.util.function.Supplier;

/**
 * Created by olga on 11.02.16.
 */
public class OneThreadLazy<T> implements Lazy<T> {
    private static final Object NONE = new Object();
    private Supplier<T> supplier;
    private T result = (T) NONE;

    OneThreadLazy(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public T get() {
        if (result == (T) NONE) {
            result = supplier.get();
            supplier = null;
        }
        return result;
    }
}
