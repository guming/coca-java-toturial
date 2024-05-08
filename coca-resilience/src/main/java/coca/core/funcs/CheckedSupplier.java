package coca.core.funcs;

import java.util.function.Supplier;

@FunctionalInterface
public interface CheckedSupplier<T> {
    T get() throws Throwable;
    default <V> CheckedSupplier<V> andThen(CheckedFunction<? super  T, ? extends V> after){
        return () -> after.apply(get());
    }
    default Supplier<T> unchecked() {
        return () -> {
            try {
                return get();
            } catch(Throwable t) {
                return sneakyThrow(t);
            }
        };
    }
    static <T extends Throwable, R> R sneakyThrow(Throwable t) throws T {
        throw (T) t;
    }
}
