package ch04;

import static java.util.concurrent.Executors.newFixedThreadPool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import rx.Scheduler;

import rx.schedulers.Schedulers;

public final class RxExecutors {

    private static final int POOL_SIZE = 10;

    private RxExecutors() { }

    public static Scheduler getPoolA() {
        final ExecutorService poolA = newFixedThreadPool(POOL_SIZE, threadFactory("Sched-A-%d"));
        return Schedulers.from(poolA);
    }

    public static Scheduler getPoolB() {
        final ExecutorService poolB = newFixedThreadPool(POOL_SIZE, threadFactory("Sched-B-%d"));
        return Schedulers.from(poolB);
    }

    public static Scheduler getPoolC() {
        final ExecutorService poolC = newFixedThreadPool(POOL_SIZE, threadFactory("Sched-C-%d"));
        return Schedulers.from(poolC);
    }

    private static ThreadFactory threadFactory(final String pattern) {
        return new ThreadFactoryBuilder().setNameFormat(pattern).build();
    }
}
