package de.frank.conccurency.fails.deadlock;

import static de.frank.conccurency.fails.Helper.printThreadLockInfo;

public class Deadlock_1_better {


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
                mbo1.doStuff(mbo2);
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
    }

    private static class MyBusinessObject2 {
        private final Object keepThisObjectConsistentLock = new Object(); //do not expose locks to outside world

        public void doStuff() {
            printThreadLockInfo("waiting for", "keepThisObjectConsistentLock");
            synchronized (keepThisObjectConsistentLock) {
                printThreadLockInfo("acquired", "keepThisObjectConsistentLock");
            }
        }
    }


}
