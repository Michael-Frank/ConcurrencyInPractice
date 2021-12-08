package de.frank.conccurency.fails.deadlock;

import java.util.concurrent.locks.ReentrantLock;

import static de.frank.conccurency.fails.Helper.printThreadLockInfo;

public class Deadlock_3_ReentrantLockDoesNotHelpYou {


    public static void main(String args[]) {
        MyBusinessObject1 mbo1 = new MyBusinessObject1();
        MyBusinessObject2 mbo2 = new MyBusinessObject2();

        Thread t1 = new Thread(() -> {
            while (true) {
                mbo1.doStuff(mbo2);
            }
        }, "t1");
        Thread t2 = new Thread(() -> {
            while (true) {
                mbo2.doStuff2(mbo1);
            }
        }, "t2");

        t1.start();
        t2.start();
    }


    private static class MyBusinessObject1 {
        private final ReentrantLock keepThisObjectConsistentLock = new ReentrantLock(); //do not expose locks to outside world

        public void doStuff(MyBusinessObject2 myBusinessObject2) {
            printThreadLockInfo("waiting for", "keepThisObjectConsistentLock");
            keepThisObjectConsistentLock.lock();
            try {
                printThreadLockInfo("acquired", "keepThisObjectConsistentLock");
                myBusinessObject2.doStuff();
            } finally {
                keepThisObjectConsistentLock.unlock();
            }
        }

        /**
         * We can still create a dead-lock, if the two objects are manipulating each other
         */
        public void doStuff2() {
            printThreadLockInfo("waiting for", "keepThisObjectConsistentLock");
            keepThisObjectConsistentLock.lock();
            try {
                printThreadLockInfo("acquired", "keepThisObjectConsistentLock");
            } finally {
                keepThisObjectConsistentLock.unlock();
            }
        }
    }

    private static class MyBusinessObject2 {
        private final ReentrantLock keepThisObjectConsistentLock = new ReentrantLock(); //do not expose locks to outside world

        public void doStuff() {
            printThreadLockInfo("waiting for", "keepThisObjectConsistentLock");
            keepThisObjectConsistentLock.lock();
            try {
                printThreadLockInfo("acquired", "keepThisObjectConsistentLock");
            } finally {
                keepThisObjectConsistentLock.unlock();
            }
        }

        /**
         * We can still create a dead-lock, if the two objects are manipulating each other
         */
        public void doStuff2(MyBusinessObject1 myBusinessObject1) {
            printThreadLockInfo("waiting for", "keepThisObjectConsistentLock");
            keepThisObjectConsistentLock.lock();
            try {
                printThreadLockInfo("acquired", "keepThisObjectConsistentLock");
                myBusinessObject1.doStuff2();
            } finally {
                keepThisObjectConsistentLock.unlock();
            }
        }
    }

}
