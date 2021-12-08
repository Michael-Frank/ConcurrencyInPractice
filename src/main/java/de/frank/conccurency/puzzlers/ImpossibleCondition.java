package de.frank.conccurency.puzzlers;

public class ImpossibleCondition {

    static int condition = 0;

    public static void main(String[] args) {
        new Thread(() -> {
            System.out.println(Thread.currentThread().getName()+" running");
            // ??? how to achieve the impossible condition
        },"attacker").start();

        new Thread(() -> {
            System.out.println(Thread.currentThread().getName()+" running");
            int i = 0;
            while (true) {
                i++;
                if (condition == 42 && condition == 999) {
                    System.out.println("impossible to happen. but still happened after: " + i + " invocations");
                    System.exit(0);
                }
            }
        },"checker").start();
    }
}
