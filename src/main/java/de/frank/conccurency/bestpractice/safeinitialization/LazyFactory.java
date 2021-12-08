package de.frank.conccurency.bestpractice.safeinitialization;

import com.google.common.base.Suppliers;
import lombok.Value;
import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.LazyInitializer;

/**
 * Thread safe init
 */
public class LazyFactory {


    //Uses classic doubleCheckedLocking - initialize() is guaranteed to be called only once
    private LazyInitializer<MyBizObject> apacheLazy = new LazyInitializer<>() {
        @Override
        protected MyBizObject initialize() {
            return initBizObject();
        }
    };

    //guava - it has a nicer usage pattern then apaches Lazy.. abstract class extensions, but guava dependency is not
    // very backward compatible. Dont use it in library code - only in a project with a main
    private com.google.common.base.Supplier<MyBizObject> guavaMemoize = Suppliers.memoize(this::initBizObject);



    private LazyFactory() {
        //no instance allowed
    }

    private MyBizObject initBizObject() {
        return new MyBizObject("foo");
    }

    public MyBizObject instanceApache() {
        try {
            return apacheLazy.get();
        } catch (ConcurrentException e) {
            throw new RuntimeException(e);
        }
    }

    public MyBizObject instanceGuava() {
        return guavaMemoize.get();
    }

    @Value//automatically produces getters, equals, hashcode, private final modifiers
    public static class MyBizObject {
        String value;
    }
}
