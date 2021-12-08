package de.frank.conccurency.bestpractice.safeinitialization;

/**
 * Thread safe singleton init
 * However:
 *  * <li> Better use {@link LazySingletonHolder}
 *  * <li> Singleton is a deprecated design pattern! better avoid it.
 */
public class LazySingletonDCL {

    // MUST be volatile for safe publication and Double-checked-locking to be correct!
    private static volatile LazySingletonDCL instance;
    private static final Object INIT_LOCK = new Object();

    private LazySingletonDCL() {
        //init this class here
    }

    public static LazySingletonDCL instance() {
        LazySingletonDCL ref = instance; //cache volatile read for fast path
        if (ref == null) {
            synchronized (INIT_LOCK) {//do not use LazyInitializationDCL.class - other threads might also sync on this
                ref = instance;//must re-read volatile after acquiring lock
                if (ref == null) {
                    instance = ref = new LazySingletonDCL();
                }
            }
        }
        return ref;

    }
}
