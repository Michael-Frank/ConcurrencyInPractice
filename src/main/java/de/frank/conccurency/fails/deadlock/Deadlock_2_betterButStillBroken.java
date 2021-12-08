package de.frank.conccurency.fails.deadlock;

import static de.frank.conccurency.fails.Helper.printThreadLockInfo;

public class Deadlock_2_betterButStillBroken {


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
        private final Object keepThisObjectConsistentLock = new Object(); //do not expose locks to outside world

        public void doStuff(MyBusinessObject2 myBusinessObject2) {
            printThreadLockInfo("waiting for", "keepThisObjectConsistentLock");
            synchronized (keepThisObjectConsistentLock) {
                printThreadLockInfo("acquired", "keepThisObjectConsistentLock");
                myBusinessObject2.doStuff();
            }
        }

        /**
         * We can still create a dead-lock, if the two objects are manipulating each other
         */
        public void doStuff2() {
            printThreadLockInfo("waiting for", "keepThisObjectConsistentLock");
            synchronized (keepThisObjectConsistentLock) {
                printThreadLockInfo("acquired", "keepThisObjectConsistentLock");
            }
        }
    }

    private static class MyBusinessObject2 {
        private final Object keepThisObjectConsistentLock = new Object(); //do not expose locks to outside world

        public void doStuff() {
            printThreadLockInfo("waiting for", "keepThisObjectConsistentLock");
            synchronized (keepThisObjectConsistentLock) {
                printThreadLockInfo("acquired", "keepThisObjectConsistentLock");
            }
        }

        /**
         * We can still create a dead-lock, if the two objects are manipulating each other
         */
        public void doStuff2(MyBusinessObject1 myBusinessObject1) {
            printThreadLockInfo("waiting for", "keepThisObjectConsistentLock");
            synchronized (keepThisObjectConsistentLock) {
                printThreadLockInfo("acquired", "keepThisObjectConsistentLock");
                myBusinessObject1.doStuff2();
            }
        }
    }


}
