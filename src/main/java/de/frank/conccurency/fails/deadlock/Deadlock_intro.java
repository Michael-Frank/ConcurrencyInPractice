package de.frank.conccurency.fails.deadlock;

import java.util.concurrent.*;

public class Deadlock_intro {
    public static void main(String[] args) throws ExecutionException, InterruptedException, TimeoutException {

        ScheduledExecutorService pool = Executors.newScheduledThreadPool(2);
        var task = pool.scheduleAtFixedRate(Deadlock_intro::doTask,0, 1, TimeUnit.MICROSECONDS);
        var task2 = pool.scheduleAtFixedRate(Deadlock_intro::doTask,0, 1, TimeUnit.MICROSECONDS);

        Thread.sleep(3000);
        pool.submit(()-> System.out.println("Print this"));
        System.out.println("Main exited");
    }

    private static void doTask() {
        System.out.println("Thread " + Thread.currentThread() + " calls method2");
        method2();
        System.out.println("Thread " + Thread.currentThread() + " calls method1");
        method1();
    }

    /*
     * This method request two locks, first String and then Integer
     */
    public static void method1() {
        synchronized (String.class) {
            System.out.println( Thread.currentThread()+"Acquired lock on String.class object");

            synchronized (Integer.class) {
                System.out.println(Thread.currentThread()+"Acquired lock on Integer.class object");
            }
        }
    }

    /*
     * This method also requests same two lock but in exactly
     * Opposite order i.e. first Integer and then String.
     * This creates potential deadlock, if one thread holds String lock
     * and other holds Integer lock and they wait for each other, forever.
     */
    public static void method2() {
        synchronized (Integer.class) {
            System.out.println(Thread.currentThread()+"Acquired lock on Integer.class object");

            synchronized (String.class) {
                System.out.println(Thread.currentThread()+"Acquired lock on String.class object");
            }
        }
    }

}
