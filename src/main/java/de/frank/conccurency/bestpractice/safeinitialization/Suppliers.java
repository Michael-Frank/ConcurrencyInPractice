package de.frank.conccurency.bestpractice.safeinitialization;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.LazyInitializer;

import java.util.function.Supplier;

@UtilityClass
public class Suppliers {
    public <T> Supplier<T> memoize(Supplier<T> delegate) {
        var memoized = new LazyInitializer<T>() {
            protected T initialize() {
                return delegate.get();
            }
        };
        return () -> {
            try {
                return memoized.get();
            } catch (ConcurrentException e) {
                throw new RuntimeException(e);
            }
        };
    }
}
