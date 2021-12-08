package de.frank.conccurency.bestpractice;

import lombok.Value;

import java.util.UUID;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;


public class LoadingCacheDemo {

    public static void main(String[] args) {
        final int threads = 8;
        CyclicBarrier awaitAllRunning = new CyclicBarrier(threads);
        BizLoadingCache cache = new BizLoadingCache();
        for (int i = 0; i < threads; i++) {
            new Thread(() -> {
                awaitSilently(awaitAllRunning);
                System.out.println(Thread.currentThread().getName() + "running");
                BizLoadingCache.MyBizObject value = cache.get("foo", "bar");
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

    public static class BizLoadingCache {
        private final ConcurrentHashMap<CacheKey, MyBizObject> cache = new ConcurrentHashMap<>();

        public MyBizObject get(String foo, String bar) {
            CacheKey key = new CacheKey(foo, bar);
            return cache.computeIfAbsent(key, this::computeNewBizValue);
        }

        private MyBizObject computeNewBizValue(CacheKey key) {
            MyBizObject newObj = new MyBizObject(UUID.randomUUID().toString());
            System.out.println(Thread.currentThread().getName() + " Computing new value for: " + key + " : " + newObj);
            try {
                Thread.sleep(5000);//simulate delay
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return newObj;
        }

        @Value//automatically produces getters, equals, hashcode, private final modifiers
        public static class CacheKey {
            String a;
            String b;
        }

        @Value//automatically produces getters, equals, hashcode, private final modifiers
        public static class MyBizObject {
            String value;
        }
    }
}
