package de.frank.conccurency.bestpractice.safeinitialization;

/**
 * Thread safe singleton init
 * However:
 * <li> This variant is eager, not lazy. Better use {@link LazySingletonHolder}
 * <li> Singleton is a deprecated design pattern! better avoid it.
 */
public class EagerSingleton {

    // not lazy
    // piggy back on java's class loader synchronization guarantees that this works
    private static final EagerSingleton INSTANCE = new EagerSingleton();

    private EagerSingleton() {
       //init this class here
    }

    public static EagerSingleton instance(){
        return INSTANCE;
    }
}
