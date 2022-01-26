package de.frank.conccurency.puzzlers;

import java.util.concurrent.TimeUnit;

public class NeverStopThreadSolution {

    private static volatile boolean stopRequested;

    public static void main(String[] args) throws InterruptedException {
        Thread backgroundThread = new Thread(() -> {
            //without volatile, the jvm compiler sees that 'stopRequested' is static.
            // After 10.000 iterations of not  seeing a change to its value, the compiler optimizes to:
            //if(!stopRequested)
            //    while (true) i++;

            // however, WITH volatile, you gave the compiler the instruction to ALWAYS fetch a fresh state of
            // stopRequested from main memory
            int i = 0;
            while (!stopRequested) {
                i++;
            }
            System.out.println("backgroundThread stopped:" + i);
        });
        backgroundThread.start();
        TimeUnit.SECONDS.sleep(1);
        stopRequested = true;
        System.out.println("requested stop - exiting main");
    }
}