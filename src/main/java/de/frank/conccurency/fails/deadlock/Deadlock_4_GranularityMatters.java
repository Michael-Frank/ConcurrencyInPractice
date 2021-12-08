package de.frank.conccurency.fails.deadlock;

import static de.frank.conccurency.fails.Helper.printThreadLockInfo;

public class Deadlock_4_GranularityMatters {

    public static void main(String args[]) {
        final Object updateMbo1AndMbo2ConsistentlyLock = new Object();
        MyBusinessObject1 mbo1 = new MyBusinessObject1();
        MyBusinessObject2 mbo2 = new MyBusinessObject2();

        Thread t1 = new Thread(() -> {
            while (true) {
                printThreadLockInfo("waiting for", "updateMbo1AndMbo2ConsistentlyLock");
                synchronized (updateMbo1AndMbo2ConsistentlyLock) {
                    printThreadLockInfo("acquired", "updateMbo1AndMbo2ConsistentlyLock");
                    mbo1.doStuff(mbo2);
                }
            }
        }, "t1");
        Thread t2 = new Thread(() -> {
            while (true) {
                printThreadLockInfo("waiting for", "updateMbo1AndMbo2ConsistentlyLock");
                synchronized (updateMbo1AndMbo2ConsistentlyLock) {
                    printThreadLockInfo("acquired", "updateMbo1AndMbo2ConsistentlyLock");
                    mbo2.doStuff2(mbo1);
                }
            }
        }, "t2");

        t1.start();
        t2.start();
    }


    private static class MyBusinessObject1 {

        public void doStuff(MyBusinessObject2 myBusinessObject2) {
            myBusinessObject2.doStuff();

        }

        /**
         * We can still create a dead-lock, if the two objects are manipulating each other
         */
        public void doStuff2() {

        }
    }

    private static class MyBusinessObject2 {

        public void doStuff() {
        }

        /**
         * We can still create a dead-lock, if the two objects are manipulating each other
         */
        public void doStuff2(MyBusinessObject1 myBusinessObject1) {
            myBusinessObject1.doStuff2();
        }
    }


}
