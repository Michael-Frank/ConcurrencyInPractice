package de.frank.conccurency.fails;

import lombok.Getter;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class BrokenDCL {
    public static void main(String[] args) {
        int threadCount = 16;
        CyclicBarrier waitForAllThreadsStarted = new CyclicBarrier(threadCount);
        for (int i = 0; i < threadCount; i++) {
            Thread t = new Thread(() -> stressTest(waitForAllThreadsStarted));
            t.start();
        }
        for (int i = 0; i < threadCount; i++) {
            Thread t = new Thread(() -> stressTest(null));
            t.start();
        }
    }

    private static void stressTest(CyclicBarrier waitForAllThreadsStarted) {
        try {
            if (waitForAllThreadsStarted != null)
                waitForAllThreadsStarted.await();

            //the object to test
            InitMeOnce objectUnderTest = InitMeOnce.getInstance();

            //check
            verifyInitializedConsitently(objectUnderTest);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        }
    }

    private static void verifyInitializedConsitently(InitMeOnce lo) {
        int res;
        if (lo != null) {
            res = lo.x00 + lo.x01 + lo.x02 + lo.x03;
        } else {
            res = -1;
        }
        if (res != 4 * 42) {
            System.out.println("Failure to initialize consistently!: " + res);
            System.exit(1);
        }
    }


    @Getter
    public static class InitMeOnce {
        private static volatile InitMeOnce instance;// specifically non-volatile
        private static final Object INIT_LOCK = new Object();
        private int x00, x01, x02, x03;

        public InitMeOnce() {

        }

        public InitMeOnce(int x) {
            x00 = x;
            x01 = x;
            x02 = x;
            x03 = x;
        }

        private static final boolean SLEEP = true;

        public static InitMeOnce getInstance() throws InterruptedException {
            if (instance == null) {
                synchronized (INIT_LOCK) {
                    if (instance == null) {
                        instance = new InitMeOnce();
                        if (SLEEP) Thread.sleep(1);
                        instance.x00 = 42;
                        if (SLEEP) Thread.sleep(1);
                        instance.x01 = 42;
                        if (SLEEP) Thread.sleep(1);
                        instance.x02 = 42;
                        if (SLEEP) Thread.sleep(1);
                        instance.x03 = 42;
                    }
                }
            }
            return instance;
        }
    }
}
