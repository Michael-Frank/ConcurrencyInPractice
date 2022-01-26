package de.frank.conccurency.puzzlers;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class NeverStopThread {

    private static boolean stopRequested;

    public static void main(String[] args) throws InterruptedException {
        Thread backgroundThread = new Thread(() -> {
            log("started...");
            int i = 0;
            long stopAfter=System.currentTimeMillis()+15*1000 ;

            while (!stopRequested) {
                if(System.currentTimeMillis() >=stopAfter){
                    log("aborted itself");
                    System.exit(0);
                }else if(i%100_000L==0){
                    //log("still running");
                }
                i++;
            }
            log("stopped:" + i);

            //what the jvm compiler sees:
            //stopRequested is static, so we can optimize:
            //if(!stopRequested)
            //    while (true) i++;
        });
        backgroundThread.start();

        TimeUnit.SECONDS.sleep(3); //this gives the thread time to start and compile
        stopRequested = true;
        log("Requested backgroundThread to stop - exiting main");

    }

    private static void log(String s) {
        System.out.println(Instant.now() + " [" + Thread.currentThread().getName() +"] "+ s);
    }
}