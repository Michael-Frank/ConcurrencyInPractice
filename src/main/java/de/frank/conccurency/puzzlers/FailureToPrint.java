package de.frank.conccurency.puzzlers;

import java.time.Instant;
import java.util.concurrent.locks.LockSupport;

/**
 * Many may expect this program to simply print 42 after a short delay. However, in reality, the delay may be much longer. It may even hang forever, or even print zero!
 * The cause of these anomalies is the lack of proper memory visibility and reordering. Let's evaluate them in more detail.
 * Source: https://www.baeldung.com/java-volatile
 */
public class FailureToPrint {

    private static int number;
    private static boolean ready;

    private static class Reader extends Thread {

        @Override
        public void run() {
            while (!ready) {
                //busy wait will work

                // Thread yield is just a thread scheduler hint. yield MIGHT busy wait for some time or the thread MIGHT
                // be de-scheudled. If its de-scheduled, this showcase wont work...
                //Thread.yield();
            }

            System.out.println(Instant.now() + " Read the number: " + number);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        new Reader().start();
        Thread.sleep(1000);//let the reader thread start ...
        //The processor may flush its write buffer in any order other than the program order
        //The processor may apply out-of-order execution technique
        //The JIT compiler may optimize via reordering

        ready = true;
        number = 42;
        System.out.println(Instant.now() + " Variables are set");
    }
}