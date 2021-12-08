package de.frank.conccurency.fails;

import lombok.Getter;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class BrokenDCL {
    public static void main(String[] args) {
        int threads = 16;
        CyclicBarrier waitForAll = new CyclicBarrier(threads);
        for (int i = 0; i < threads; i++) {
            Thread t = new Thread(() -> stressTest(waitForAll));
            t.start();
        }
        for (int i = 0; i < threads; i++) {
            Thread t = new Thread(() -> stressTest(null));
            t.start();
        }
    }

    private static void stressTest(CyclicBarrier waitForAll) {
        try {
            if (waitForAll != null)
                waitForAll.await();
            InitMeOnce lo = InitMeOnce.getInstance();
            int res;
            if (lo != null) {
                res = lo.x00 + lo.x01 + lo.x02 + lo.x03;
            } else {
                res = -1;
            }
            if (res != 4 * 42) {
                System.out.println("Failure to initialize!: " + res);
                System.exit(1);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
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
