package de.frank.conccurency.fails;

import java.util.Objects;

public class Helper {
    public static void printThreadLockInfo(String status, String lockObjectName) {
        System.out.printf("Thread '%s' %11s lock '%s' @ %s%n",
                Thread.currentThread().getName(),
                status,
                lockObjectName,
                StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
                        .walk(s -> s
                                .skip(1)
                                .limit(1)
                                .map(Objects::toString)
                                .findFirst()
                                .get()
                        ));/**/
    }
}
