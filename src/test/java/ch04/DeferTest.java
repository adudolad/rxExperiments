package ch04;

import static ch04.SubscribeAndObserveOnTest.store;

import org.junit.Test;

import ch04.utils.RxExecutors;

import lombok.extern.slf4j.Slf4j;

import rx.Observable;
import rx.Scheduler;

@Slf4j
public class DeferTest {

    private Observable<String> obs;
    private Observable<String> obs_deferred;

    /*
     * [Test worker]  ch04.DeferTest - Starting
     * [Test worker]  ch04.DeferTest - Created
     * [Test worker]  ch04.DeferTest - Starting deferred
     * [Test worker]  ch04.DeferTest - Created deferred
     * [Test worker]  ch04.DeferTest - Run 1
     * [Sched-A-0]    ch04.DeferTest - Subscribed
     * [Sched-B-0]    ch04.SubscribeAndObserveOnTest - Storing [A]
     * [Test worker]  ch04.DeferTest - Run 2
     * [Sched-A-1]    ch04.DeferTest - Subscribed
     * [Sched-B-1]    ch04.SubscribeAndObserveOnTest - Storing [A]
     * [Test worker]  ch04.DeferTest - Exiting
     */
    @Test
    public void testDefer() throws InterruptedException {
        final Scheduler schedulerA = RxExecutors.getPoolA();
        final Scheduler schedulerB = RxExecutors.getPoolB();
        final Scheduler schedulerC = RxExecutors.getPoolC();

        log.info("Starting");
        obs = Observable.create(subscriber -> {
                log.info("Subscribed");
                subscriber.onNext("A");
                subscriber.onCompleted();
            });
        log.info("Created");

        log.info("Starting deferred");
        obs_deferred = Observable.defer(() ->
                    Observable.create(subscriber -> {
                        log.info("Subscribed");
                        subscriber.onNext("A");
                        subscriber.onCompleted();
                    }));
        log.info("Created deferred");

        log.warn("Run 1");
        obs.subscribeOn(schedulerA).flatMap(x -> store(x).subscribeOn(schedulerB)).observeOn(schedulerC).toBlocking()
           .single();

        log.warn("Run 2");
        obs_deferred.subscribeOn(schedulerA).flatMap(x -> store(x).subscribeOn(schedulerB)).observeOn(schedulerC)
                    .toBlocking().single();

        log.info("Exiting");
    }
}
