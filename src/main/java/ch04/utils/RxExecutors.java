package ch04.utils;

import static java.util.concurrent.Executors.newFixedThreadPool;

import java.util.concurrent.ThreadFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import lombok.extern.slf4j.Slf4j;

import rx.Scheduler;

import rx.schedulers.Schedulers;

@Slf4j
public final class RxExecutors {

    private static final int POOL_SIZE = 10;

    private RxExecutors() { }

    public static Scheduler getPoolA() {
        return Schedulers.from(newFixedThreadPool(POOL_SIZE, threadFactory("Sched-A-%d")));
    }

    public static Scheduler getPoolB() {
        return Schedulers.from(newFixedThreadPool(POOL_SIZE, threadFactory("Sched-B-%d")));
    }

    public static Scheduler getPoolC() {
        return Schedulers.from(newFixedThreadPool(POOL_SIZE, threadFactory("Sched-C-%d")));
    }

    private static ThreadFactory threadFactory(final String pattern) {
        return new ThreadFactoryBuilder().setNameFormat(pattern).build();
    }
}
