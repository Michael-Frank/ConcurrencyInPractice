package de.frank.conccurency.puzzlers;

public class ImpossibleConditionSolution {

    static int condition = 0;

    public static void main(String[] args) {
        new Thread(() -> {
            System.out.println(Thread.currentThread().getName()+" running");
            while (true) {
                condition = 42;
                condition = 999;
            }
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