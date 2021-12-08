package de.frank.conccurency.fails.deadlock;

import de.frank.util.DeepCopyUtil;
import lombok.Getter;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static de.frank.conccurency.fails.Helper.printThreadLockInfo;

public class Deadlock_6_final_fullyDefensive {

    /**
     * Keep in mind: the repositories job is to keep ITS internal data in a consistent state.
     * It cannot guarantee consistency wither other data.
     */
    public static class MyBusinessStateRepository {
        private final Object updateMbo1AndMbo2ConsistentlyLock = new Object();

        private AtomicReference<MyBusinessState> state =new AtomicReference<>( new MyBusinessState());

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
                MyBusinessState stateClone= DeepCopyUtil.clone(state.get());
                stateUpdater.accept(stateClone);
                state.set(stateClone); //we could clone again if we dont trust the stateUpdater
            }
        }

        /**
         * Returns a defensive copy of the sate - you can manipulate/filter the state, but it wont change the data managed by this class
         * to update data use {@link #update(Consumer)}
         * @return a deep copy of the state
         */
        public MyBusinessState get() {
            return DeepCopyUtil.clone(state.get());
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
