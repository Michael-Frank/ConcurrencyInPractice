package de.frank.conccurency.bestpractice.safeinitialization;


import com.google.common.base.Suppliers;
import lombok.Value;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.LazyInitializer;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * Options to Thread safe init of an (potentially expensive) Object.
 */
@UtilityClass
public class LazyFactoryDemo {
    @Value//automatically produces getters, equals, hashcode, private final modifiers
    public static class MyBizObject {
        String value;
    }


    public static void main(String[] args) throws ConcurrentException {

        //Uses classic doubleCheckedLocking - initialize() is guaranteed to be called only once
        //However, usage pattern of apache is bonkers.....
        LazyInitializer<MyBizObject> apacheLazy = new LazyInitializer<>() {
            @Override
            protected MyBizObject initialize() {
                return initBizObject();
            }
        };
        try {
            var fromApache = apacheLazy.get();
            System.out.println(fromApache);
        } catch (ConcurrentException e) {
            throw new RuntimeException(e);
        }


        //guava - it has a nicer usage pattern then apaches Lazy.. abstract class extensions, but guava dependency is not
        // very backward compatible. Dont use it in library code - only in a project with a own main
        //also Google has its own Supplier class, shadowing the java Supplier! :-(
        com.google.common.base.Supplier<MyBizObject> guavaMemoize = com.google.common.base.Suppliers.memoize(() -> initBizObject());

        //
        Supplier<MyBizObject> customMemoizer = de.frank.conccurency.bestpractice.safeinitialization.Suppliers.memoize(() -> initBizObject());


        System.out.println(apacheLazy.get());//throws checked ConcurrentException!
        System.out.println(guavaMemoize.get());
        System.out.println(customMemoizer.get());
    }


    AtomicInteger counter = new AtomicInteger();

    private MyBizObject initBizObject() {
        return new MyBizObject("foo_" + counter.incrementAndGet());
    }


}
