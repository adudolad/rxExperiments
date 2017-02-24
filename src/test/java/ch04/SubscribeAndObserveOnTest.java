package ch04;

import java.util.UUID;

import org.junit.Test;

import ch04.utils.RxExecutors;

import lombok.extern.slf4j.Slf4j;

import rx.Observable;
import rx.Scheduler;
/*
 * subscribeOn() allows choosing which Scheduler will be used to invoke OnSubscribe (lambda expression inside create()).
 *
 * Therefore, any code in inside create() is pushed to a different thread.
 * Conversely, observeOn() controls which Scheduler is used to invoke downstream Subscribers occurring after
 * observerOn().
 * The position of observeOn() is quite important. No matter what Scheduler was running operators ABOVE observeOn(),
 * everything BELOW uses the supplied Scheduler.
 */

@Slf4j
public class SubscribeAndObserveOnTest {
    @Test
    public void testSingleObserveOn() throws InterruptedException {
        final Scheduler schedulerA = RxExecutors.getPoolA();
        log.info("Starting");

// final Observable<String> obs = Observable.defer(() -> Observable.just("A", "B")).delay(1, TimeUnit.SECONDS);
        final Observable<String> obs = Observable.just("A", "B");
        log.info("Created");
        obs.doOnNext(x -> log.info("Found 1: [{}]", x)).observeOn(schedulerA).doOnNext(x ->
                   log.info("Found 2: [{}]", x)).subscribe(x -> log.info("Got 1: [{}]", x), Throwable::printStackTrace,
               () -> log.info("Completed"));
        log.info("Exiting");

        Thread.sleep(1000);
    }

    /*
     * [main]       - Starting
     * [main]       - Created
     * [main]       - Found 1: [A]
     * [main]       - Found 1: [B]
     * [main]       - Exiting
     * [Sched-B-1]  - Found 2: [A]
     * [Sched-B-2]  - Found 2: [B]
     * [Sched-C-1]  - Found 3: [A]
     * [Sched-C-2]  - Found 3: [B]
     * [Sched-A-1]  - Got 1: [A]
     * [Sched-A-2]  - Got 1: [B]
     * [Sched-A-3]  - Completed
     */
    @Test
    public void testMultipleObserveOn() throws InterruptedException {
        final Scheduler schedulerA = RxExecutors.getPoolA();
        final Scheduler schedulerB = RxExecutors.getPoolB();
        final Scheduler schedulerC = RxExecutors.getPoolC();

        log.info("Starting");

        final Observable<String> obs = Observable.just("A", "B");
        log.info("Created");
        obs.doOnNext(x -> log.info("Found 1: [{}]", x)).observeOn(schedulerB).doOnNext(x ->
                   log.info("Found 2: [{}]", x)).observeOn(schedulerC).doOnNext(x -> log.info("Found 3: [{}]", x))
           .observeOn(schedulerA).subscribe(x -> log.info("Got 1: [{}]", x), Throwable::printStackTrace,
               () -> log.info("Completed"));
        log.info("Exiting");

        Thread.sleep(1000);
    }

    /*
     * [main]       - Starting
     * [main]       - Created
     * [main]       - Exiting
     * [Sched-A-0]  - Found 1: [A]
     * [Sched-A-0]  - Found 1: [B]
     * [Sched-B-1]  - Found 2: [A]
     * [Sched-B-1]  - Found 2: [B]
     * [Sched-C-1]  - Found 3: [A]
     * [Sched-C-1]  - Got 1:   [A]
     * [Sched-C-1]  - Found 3: [B]
     * [Sched-C-1]  - Got 1:   [B]
     * [Sched-C-1]  - Completed
     */
    @Test
    public void testMultipleObserveOnAndSubscribeOn() throws InterruptedException {
        final Scheduler schedulerA = RxExecutors.getPoolA();
        final Scheduler schedulerB = RxExecutors.getPoolB();
        final Scheduler schedulerC = RxExecutors.getPoolC();

        log.info("Starting");

        final Observable<String> obs = Observable.just("A", "B");
        log.info("Created");
        obs.doOnNext(x -> log.info("Found 1: [{}]", x)).observeOn(schedulerB).doOnNext(x ->
                   log.info("Found 2: [{}]", x)).observeOn(schedulerC).doOnNext(x -> log.info("Found 3: [{}]", x))
           .subscribeOn(schedulerA).subscribe(x -> log.info("Got 1: [{}]", x), Throwable::printStackTrace,
               () -> log.info("Completed"));
        log.info("Exiting");

        Thread.sleep(1000);
    }

    /*
     * [main]       - Starting
     * [main]       - Created
     * [main]       - Exiting
     * [Sched-A-0]  - Subscribed
     * [Sched-B-1]  - Storing [B]
     * [Sched-B-0]  - Storing [A]
     * [Sched-B-3]  - Storing [D]
     * [Sched-B-2]  - Storing [C]
     * [Sched-C-1]  - Got: [225a9cb4-e7e3-4f83-8fb6-9be310af447c]
     * [Sched-C-1]  - Got: [d54fe2fc-334d-4d4d-a469-ceb88596e6f3]
     * [Sched-C-1]  - Got: [efe5c185-1938-4b47-96ed-afef54faec77]
     * [Sched-C-1]  - Got: [0622137d-565c-4c7b-a888-d4cce1293dc7]
     * [Sched-C-1]  - Completed
     */
    @Test
    public void testObserveOnAndSubscribeOnAdvanced() throws InterruptedException {
        final Scheduler schedulerA = RxExecutors.getPoolA();
        final Scheduler schedulerB = RxExecutors.getPoolB();
        final Scheduler schedulerC = RxExecutors.getPoolC();

        log.info("Starting");

        final Observable<String> obs = Observable.create(subscriber -> {
                log.info("Subscribed");
                subscriber.onNext("A");
                subscriber.onNext("B");
                subscriber.onNext("C");
                subscriber.onNext("D");
                subscriber.onCompleted();
            });

        log.info("Created");
        obs.subscribeOn(schedulerA).flatMap(x -> store(x).subscribeOn(schedulerB)).observeOn(schedulerC).subscribe(x ->
                log.info("Got: [{}]", x),
            Throwable::printStackTrace, () -> log.info("Completed"));

        obs.subscribeOn(schedulerA).flatMap(x -> store(x).subscribeOn(schedulerB)).observeOn(schedulerC).toBlocking()
           .forEach(event -> { log.info("Got: [{}]", event); });

        log.info("Exiting");

        Thread.sleep(1000);
    }

    public static Observable<UUID> store(final String s) {
        return Observable.create(subscriber -> {
                log.info("Storing [{}]", s);
                subscriber.onNext(UUID.randomUUID());
                subscriber.onCompleted();
            });
    }
}
