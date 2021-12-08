package de.frank.conccurency.puzzlers;

import java.util.concurrent.TimeUnit;

public class NeverStopThread {

    private static boolean stopRequested;

    public static void main(String[] args) throws InterruptedException {
        Thread backgroundThread = new Thread(() -> {
            int i = 0;
            while (!stopRequested) {
                i++;
            }
            System.out.println("backgroundThread stopped:" + i);

            //what the jvm compiler sees:
            //stopRequested is static, so we can optimize:
            //if(!stopRequested)
            //    while (true) i++;
        });
        backgroundThread.start();
        TimeUnit.SECONDS.sleep(1);
        stopRequested = true;
        System.out.println("requested stop - exiting main");


    }
}