package ch04;

import static ch04.SubscribeAndObserveOnTest.store;

import org.junit.Test;

import ch04.utils.RxExecutors;

import lombok.extern.slf4j.Slf4j;

import rx.Observable;
import rx.Scheduler;

@Slf4j
public class BlockingTest {

    private Observable<String> obs;

    /*
     * [Test worker]   ch04.BlockingTest - Starting
     * [Test worker]   ch04.BlockingTest - Created
     * [Sched-A-0]   ch04.BlockingTest - Subscribed
     * [Sched-B-1]   ch04.SubscribeAndObserveOnTest - Storing [B]
     * [Sched-B-3]   ch04.SubscribeAndObserveOnTest - Storing [D]
     * [Sched-B-2]   ch04.SubscribeAndObserveOnTest - Storing [C]
     * [Sched-B-0]   ch04.SubscribeAndObserveOnTest - Storing [A]
     * [Sched-C-1]   ch04.BlockingTest - Got: [011521c5-5890-4b27-8522-40f37848c5b8]
     * [Sched-C-1]   ch04.BlockingTest - Got: [63ae2a1b-b3ed-4075-b316-23d6611d76ce]
     * [Sched-C-1]   ch04.BlockingTest - Got: [15c236f3-ec93-4dd4-a052-9535aa7c6d30]
     * [Sched-C-1]   ch04.BlockingTest - Got: [d5a892d2-2539-4dea-8af2-65cd5f9da29c]
     * [Test worker]   ch04.BlockingTest - Exiting
     */
    @Test
    public void testBlockingForEach() throws InterruptedException {
        final Scheduler schedulerA = RxExecutors.getPoolA();
        final Scheduler schedulerB = RxExecutors.getPoolB();
        final Scheduler schedulerC = RxExecutors.getPoolC();

        log.info("Starting");
        obs = Observable.create(subscriber -> {
                log.info("Subscribed");
                subscriber.onNext("A");
                subscriber.onNext("B");
                subscriber.onNext("C");
                subscriber.onNext("D");
                subscriber.onCompleted();
            });
        log.info("Created");

        obs.subscribeOn(schedulerA).flatMap(x -> store(x).subscribeOn(schedulerB)).observeOn(schedulerC).toBlocking()
           .forEach(event -> { log.info("Got: [{}]", event); });

        log.info("Exiting");
    }

    /*
     * [Test worker]  ch04.BlockingTest - Starting
     * [Test worker]  ch04.BlockingTest - Created
     * [Sched-A-0]    ch04.BlockingTest - Subscribed
     * [Sched-B-0]    ch04.SubscribeAndObserveOnTest - Storing [A]
     * [Test worker]  ch04.BlockingTest - Exiting
     */
    @Test
    public void testBlockingSingle() throws InterruptedException {
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

        obs.subscribeOn(schedulerA).flatMap(x -> store(x).subscribeOn(schedulerB)).observeOn(schedulerC).toBlocking()
           .subscribe();

        log.info("Exiting");
    }
}
