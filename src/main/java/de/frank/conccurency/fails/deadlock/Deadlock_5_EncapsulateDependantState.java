package de.frank.conccurency.fails.deadlock;

import lombok.Getter;

import java.util.function.Consumer;

import static de.frank.conccurency.fails.Helper.printThreadLockInfo;

public class Deadlock_5_EncapsulateDependantState {

    /**
     * Keep in mind: the repositories job is to keep ITS internal data in a consistent state.
     * It cannot guarantee consistency wither other data.
     */
    public static class MyBusinessStateRepository {
        private final Object updateMbo1AndMbo2ConsistentlyLock = new Object();

        private MyBusinessState state = new MyBusinessState();

        @Getter
        public static class MyBusinessState {
            private final MyBusinessObject1 mbo1 = new MyBusinessObject1();
            private final MyBusinessObject2 mbo2 = new MyBusinessObject2();
        }

        /**
         * Ensure MyBusinessState can be updated by the caller, and hide the required synchronization.
         * ATTENTION! however, if there is a "MyBusinessState get()" method, a caller may still alter the internal state
         *
         * @param stateUpdater
         */
        public void update(Consumer<MyBusinessState> stateUpdater) {
            printThreadLockInfo("waiting for", "updateMbo1AndMbo2ConsistentlyLock");
            synchronized (updateMbo1AndMbo2ConsistentlyLock) {
                printThreadLockInfo("acquired for", "updateMbo1AndMbo2ConsistentlyLock");
                stateUpdater.accept(state);
            }
        }

        public MyBusinessState get() {
            //Ah no, just adding a get() still wont work. Simple returning the state would destroy the encapsulation
            // by letting the internal state escape
            // Either return a defensive copy or make the state "immutable"
            throw new UnsupportedOperationException("Not thread safe");
        }
    }


    public static void main(String args[]) {
        MyBusinessStateRepository repo = new MyBusinessStateRepository();
        Thread t1 = new Thread(() -> {
            while (true) {
                repo.update(state -> state.getMbo1().doStuff(state.getMbo2()));
            }
        }, "t1");
        Thread t2 = new Thread(() -> {
            while (true) {
                repo.update(state -> state.getMbo2().doStuff2(state.getMbo1()));
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
