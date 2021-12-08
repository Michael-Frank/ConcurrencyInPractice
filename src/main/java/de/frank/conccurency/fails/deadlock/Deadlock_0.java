package de.frank.conccurency.fails.deadlock;

import static de.frank.conccurency.fails.Helper.printThreadLockInfo;

public class Deadlock_0 {


    public static void main(String args[]) {
        MyBusinessObject1 mbo1 = new MyBusinessObject1();
        MyBusinessObject2 mbo2 = new MyBusinessObject2();

        Thread t1 = new Thread(() -> {
            while (true) {
                printThreadLockInfo("waiting for", "mbo1");
                synchronized (mbo1) {
                    printThreadLockInfo("acquired", "mbo1");
                    mbo1.doStuff(mbo2);
                }
            }
        }, "t1");
        Thread t2 = new Thread(() -> {
            while (true) {
                printThreadLockInfo("waiting for", "mbo2");
                synchronized (mbo2) {
                    printThreadLockInfo("acquired","mbo2");
                    mbo1.doStuff(mbo2);
                }
            }
        }, "t2");

        t1.start();
        t2.start();
    }


    private static class MyBusinessObject1 {

        public void doStuff(MyBusinessObject2 myBusinessObject2) {

            printThreadLockInfo("waiting for", this.getClass().getSimpleName());
            synchronized (this) {
                printThreadLockInfo("acquired", this.getClass().getSimpleName());
                myBusinessObject2.doStuff();
            }
        }
    }

    private static class MyBusinessObject2 {

        public void doStuff() {
            printThreadLockInfo("waiting for", this.getClass().getSimpleName());
            synchronized (this) {
                printThreadLockInfo("acquired", this.getClass().getSimpleName());
            }
        }
    }


}
