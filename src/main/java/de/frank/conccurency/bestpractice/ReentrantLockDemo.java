package de.frank.conccurency.bestpractice;

import java.util.concurrent.locks.ReentrantLock;
/**
 * How to use ReentrantLock correctly
 * @author Michael Frank
 * @version 1.0 26.01.2022
 */
public class ReentrantLockDemo {

    ReentrantLock lock = new ReentrantLock();
    int counter = 0;

    public void correctVariant1() {
        lock.lock();
        try {
            // Critical section here
            counter++;
        } finally {
            lock.unlock();
        }
    }

    public void WRONG() {
        try {
            //in case lock() throws an Unchecked exception, we would incorrectly unlock the lock in the finally
            //without having acquired the lock
            //This will lead to an IllegalMonitorStateException thrown by unlock();
            lock.lock();
            // Critical section here
            counter++;
        } finally {
            lock.unlock();

        }
    }

    public void correctVariant2() {
        try {
            lock.lock();
            //Critical section here
            counter++;
        } finally {
            //in case lock() throws an Unchecked exception, we have not successfully acquired a lock
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

}
