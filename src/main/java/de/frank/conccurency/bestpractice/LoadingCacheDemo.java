package de.frank.conccurency.bestpractice;

import lombok.Value;

import java.util.UUID;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;

/**
 *
 * @author Michael Frank
 * @version 1.0 26.01.2022
 */
public class LoadingCacheDemo {

    /**
     * Some mock business object - could by anything
     * Its good practice to have only immutable objects in your cache, as it makes it easier to reason about cache-consistency.
     */
    @Value//automatically produces getters, equals, hashcode, private final modifiers
    public static class MyBizObject {
        String value;
    }

    /**
     * Example of a multidimensional cache key.
     * You dont have to use String concatenation to produce a unique cache key.
     */
    @Value//automatically produces getters, equals, hashcode, private final modifiers
    public static class CacheKey {
        String a;
        String b;
    }

    /**
     * A prototype for a typical "lazy-loading-cache" encountered in many business uses-cases.
     * However, be aware that such a cache is un-bounded (can grow indefinitely)
     */
    public static class MyConcurrentBizLoadingCache {
        private final ConcurrentHashMap<CacheKey, MyBizObject> cache = new ConcurrentHashMap<>();

        public MyBizObject get(String foo, String bar) {
            CacheKey key = new CacheKey(foo, bar);

            //Attention! ConcurrentHashMap computeIfAbsent requires that it is ok to call computeNewBizValue concurrently!
            //But it guarantees that only one call result will "winn" and will become consistently globally visible
            //That also implies that computeNewBizValue() should ideally be a fast operation.
            //If the operation inside computeNewBizValue is expensive or must be synchronized, a different approach is required!
            // e.g.  ConcurrentHashMap<CacheKey, Supplier<LazySMyBizObject>> where Supplier is an implementation of a thread-safe Inititalization/Memoization.
            // e.g. Suppliers from this Repository
            //cache.computeIfAbsent(key, (theKey)->LazyFactory.Suppliers.memoize(()->computeNewBizValue(theKey)));
            return cache.computeIfAbsent(key, this::computeNewBizValue);
        }

        private MyBizObject computeNewBizValue(CacheKey key) {
            MyBizObject newObj = new MyBizObject(UUID.randomUUID().toString()/*mock value*/);
            System.out.println(Thread.currentThread().getName() + " Computing new value for: " + key + " : " + newObj);

            //simulate object creation delay for demo purposes only! In production the computation of a new cache value MUST be very fast!
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return newObj;
        }


    }

    public static void main(String[] args) {
        demonstreateMultithreadAccessToCache();
    }

    private static void demonstreateMultithreadAccessToCache() {
        final int threads = 8;
        CyclicBarrier awaitAllRunning = new CyclicBarrier(threads);
        MyConcurrentBizLoadingCache cache = new MyConcurrentBizLoadingCache();
        for (int i = 0; i < threads; i++) {
            new Thread(() -> {
                awaitSilently(awaitAllRunning);
                System.out.println(Thread.currentThread().getName() + "running");
                MyBizObject value = cache.get("foo", "bar");
                System.out.println(Thread.currentThread().getName() + "got: " + value);

            }, "t" + i).start();
        }
    }

    private static void awaitSilently(CyclicBarrier barrier) {
        try {
            barrier.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }
    }


}
