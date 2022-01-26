package de.frank.concurrency.benchmarks;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

/*--
Contended Throughput
-------------------
Contended Throughput simulates a very high thread pressure on the shared singleton instance
Compares the access times of the Singleton instance after the code is already hot and neglects the initialization time (as it does not matter. See "Initialization time")

Score Numbers: higher is better
Benchmark                               Mode  Cnt          Score        Error  Units   Comment
broken_unsafe                          thrpt   30  1.468.820.116 ± 42.935.121  ops/s # BROKEN! but serves as our baseline
SynchronizedSingleton                  thrpt   30     22.491.489 ±  1.911.089  ops/s # Locking on every access makes that really to slow...
DCLSingleton                           thrpt   30  1.473.006.492 ± 60.529.198  ops/s # DCL works well - but error prone to implement
OptimizedDCLSingleton                  thrpt   30  1.495.271.521 ± 47.295.827  ops/s # way to complex for negligible perf gains
EagerSingleton                         thrpt   30  1.630.122.456 ± 61.854.727  ops/s # eager is fast - if you need the object anyways
InitializationOnDemandHolderSingleton  thrpt   30  1.644.520.286 ± 65.389.507  ops/s # easiest and fast thread safe lazy initialization


Un-Contended Single-Thread Throughput
----------------------------------------
Score Numbers: higher is better         Mode  Cnt        Score        Error  Units   Comment
broken_unsafe                          thrpt   30  428.463.466 ± 17.434.853  ops/s # BROKEN! but serves as our baseline
SynchronizedSingleton                  thrpt   30   68.875.445 ±  1.702.670  ops/s # Locking on every access makes that really to slow...
DCLSingleton                           thrpt   30  440.089.717 ± 18.134.915  ops/s # DCL works well - but error prone to implement
OptimizedDCLSingleton                  thrpt   30  439.840.738 ± 11.761.408  ops/s # way to complex for negligible/np perf gains
EagerSingleton                         thrpt   30  456.560.332 ± 13.569.271  ops/s # eager is fast - if you need the object anyways
InitializationOnDemandHolderSingleton  thrpt   30  453.505.504 ±  8.903.377  ops/s # easiest and fast thread safe lazy initialization

Initialization time
-------------------
For the sake of completeness here are the times for initialization (SingleShotTimes)
This is just to proof that initialization times do not matter (we are in varying by 1 microsecond here with an error of 2 microseconds!).
If you feel the need to have millions of singletons, then there is something else wrong...

Score Numbers: lower is better
Benchmark                                               Mode  Cnt     Score      Error  Units
LazySingletonJMH.DCLSingleton                             ss   30  1886,667 ± 2449,263  ns/op
LazySingletonJMH.EagerSingleton                           ss   30  1033,333 ±  162,078  ns/op
LazySingletonJMH.InitializationOnDemandHolderSingleton    ss   30  1080,000 ±  194,905  ns/op
LazySingletonJMH.OptimizedDCLSingleton                    ss   30  2026,667 ± 2632,640  ns/op
LazySingletonJMH.SynchronizedSingleton                    ss   30  1170,000 ±  169,339  ns/op
LazySingletonJMH.broken_unsafe                            ss   30  1030,000 ±  224,795  ns/op
 */

/**
 * JMH comparison of various Singleton implementation idioms.
 *
 * @author Michael Frank
 * @version 1.0 15.12.2021
 */
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@State(Scope.Benchmark) // Important to be Scope.Benchmark
@Fork(3)
@BenchmarkMode({Mode.Throughput})
@OutputTimeUnit(TimeUnit.SECONDS)
@Threads(1)//make this high enough to produce some contention (e.g. 2x cpu core count)
public class LazySingletonJMH {


    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()//
                .include(LazySingletonJMH.class.getName())//
                /*
                .result(String.format("%s_%s.json",
                        DateTimeFormatter.ISO_INSTANT.format(Instant.now()),
                        LazySingltonJMH.class.getSimpleName()))
                        */
                //.addProfiler(GCProfiler.class)//
                .build();
        new Runner(opt).run();
    }


    @Benchmark
    public Object broken_unsafe() {
        return NonThreadSafeSingleton.getInstance();
    }

    @Benchmark
    public Object SynchronizedSingleton() {
        return SynchronizedSingleton.getInstance();
    }

    @Benchmark
    public Object EagerSingleton() {
        return EagerSingleton.getInstance();
    }

    @Benchmark
    public Object DCLSingleton() {
        return DCLSingleton.getInstance();
    }

    @Benchmark
    public Object OptimizedDCLSingleton() {
        return OptimizedDCLSingleton.getInstance();
    }

    @Benchmark
    public Object InitializationOnDemandHolderSingleton() {
        return InitializationOnDemandHolderSingleton.getInstance();
    }


    public static class NonThreadSafeSingleton {
        private static NonThreadSafeSingleton INSTANCE;
        int a;
        int b;

        private NonThreadSafeSingleton() {
            this.a = 42;
            this.b = 42;
        }

        public static NonThreadSafeSingleton getInstance() {
            if (INSTANCE == null) {
                INSTANCE = new NonThreadSafeSingleton();
            }
            return INSTANCE;
        }

    }

    public static class SynchronizedSingleton {
        private static volatile SynchronizedSingleton INSTANCE;
        int a;
        int b;

        private SynchronizedSingleton() {
            this.a = 42;
            this.b = 42;
        }

        public static synchronized SynchronizedSingleton getInstance() {
            if (INSTANCE == null) {
                INSTANCE = new SynchronizedSingleton();
            }
            return INSTANCE;
        }

    }


    public static class EagerSingleton {
        //Relies on Java'S memory model final semantics - but ist EAGER loading
        // for lazy variant use InitializationOnDemandHolderSingleton
        private static final EagerSingleton INSTANCE = new EagerSingleton();
        int a;
        int b;

        private EagerSingleton() {
            this.a = 42;
            this.b = 42;
        }

        public static EagerSingleton getInstance() {
            return INSTANCE;
        }

    }


    public static class DCLSingleton {
        private static volatile DCLSingleton INSTANCE;
        int a;
        int b;

        private DCLSingleton() {
            this.a = 42;
            this.b = 42;
        }

        public static DCLSingleton getInstance() {
            if (INSTANCE == null) {
                synchronized (DCLSingleton.class) {
                    if (INSTANCE == null) {
                        DCLSingleton tmp = new DCLSingleton();
                        INSTANCE = tmp; // Attention! only set visible after initialization is fully completed!
                    }
                }
            }
            return INSTANCE;
        }

    }

    public static class OptimizedDCLSingleton {
        private static final Object INSTANCE_LOCK = new Object();
        private static volatile OptimizedDCLSingleton INSTANCE;
        int a;
        int b;

        private OptimizedDCLSingleton() {
            this.a = 42;
            this.b = 42;
        }


        public static OptimizedDCLSingleton getInstance() {
            //Reduce number of volatile reads in fast path, by using a local copy
            OptimizedDCLSingleton tmp = INSTANCE;
            if (tmp == null) {
                synchronized (INSTANCE_LOCK) {
                    // Attention: MUST re-read after acquiring the lock!
                    tmp = INSTANCE;
                    if (tmp == null) {
                        tmp = new OptimizedDCLSingleton();
                        INSTANCE = tmp; // Attention! only set visible after initialization is fully completed!
                    }
                }
            }
            return tmp;
        }
    }

    //If the helper object is static (one per class loader), an alternative is the initialization-on-demand holder idiom
    public static class InitializationOnDemandHolderSingleton {
        //This relies on the fact that nested classes are not loaded until they are referenced.
        private static final class LazyHolder {
            private static final InitializationOnDemandHolderSingleton INSTANCE = new InitializationOnDemandHolderSingleton();
        }

        int a;
        int b;

        private InitializationOnDemandHolderSingleton() {
            this.a = 42;
            this.b = 42;
        }

        public static InitializationOnDemandHolderSingleton getInstance() {
            return LazyHolder.INSTANCE;
        }

    }
}
