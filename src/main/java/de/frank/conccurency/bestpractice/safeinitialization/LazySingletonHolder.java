package de.frank.conccurency.bestpractice.safeinitialization;
/**
 * Thread safe singleton init
 * However:
 *  * <li> Singleton is a deprecated design pattern! better avoid it.
 */
public class LazySingletonHolder {

    // lazy by using a internal indirection.
    // We piggy back on java's class loader synchronization guarantees that this works
    private static class Holder{
        private static final LazySingletonHolder INSTANCE = new LazySingletonHolder();

    }
    private LazySingletonHolder() {
        //init this class here
    }

    public static LazySingletonHolder instance(){
        return Holder.INSTANCE;
    }
}
