package ch07.timeout;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.RandomStringUtils;

import org.junit.Before;
import org.junit.Test;

import lombok.extern.slf4j.Slf4j;

import rx.Observable;

import rx.observers.Subscribers;
import rx.observers.TestSubscriber;

import rx.schedulers.Schedulers;
import rx.schedulers.TestScheduler;
import rx.schedulers.TimeInterval;

@Slf4j
public class TimeoutTest {
    private TestScheduler scheduler;
    private TestSubscriber<String> subscriber;

    @Before
    public void setUo() {
        scheduler = Schedulers.test();
        subscriber = new TestSubscriber<>();
    }

    @Test
    public void testShouldCheckSimplestTimeout() throws InterruptedException {

        final TestSubscriber<TimeInterval<String>> timeSubscriber = TestSubscriber.create(Subscribers.create((x) ->
                        log.info("Time [{}], value [{}]", x.getIntervalInMilliseconds(), x.getValue())));

        final Observable<String> completionDelay = Observable.<String>empty().delay(100, TimeUnit.MILLISECONDS);

        final Observable<String> confirmation = Observable.just(RandomStringUtils.randomAlphanumeric(10))
                                                          .delay(100, TimeUnit.MILLISECONDS).doOnNext(x ->
                    log.info("New event [{}]", x)).concatWith(completionDelay);

        confirmation.timeout(() -> Observable.timer(190, TimeUnit.MILLISECONDS),
                        (data) -> Observable.timer(210, TimeUnit.MILLISECONDS)).forEach(x ->
                            log.info("Result confirmation is [{}]", x),
                        th -> {
                            if (th instanceof TimeoutException) {
                                log.error("Timeout !", th);
                            } else {
                                log.error("An erro occured", th);
                            }
                        });

        scheduler.advanceTimeBy(80, TimeUnit.MILLISECONDS);
        timeSubscriber.assertNoValues();
        timeSubscriber.assertNotCompleted();

        scheduler.advanceTimeBy(75, TimeUnit.MILLISECONDS);
        timeSubscriber.assertNoValues();
        timeSubscriber.assertNoErrors();
        timeSubscriber.assertNotCompleted();

        scheduler.advanceTimeBy(250, TimeUnit.MILLISECONDS);
        timeSubscriber.assertNoErrors();

    }

    @Test
    public void doubleTimeout() {
        final TestSubscriber<TimeInterval<String>> timeSubscriber = TestSubscriber.create(Subscribers.create((x) ->
                        log.info("Time [{}], value [{}]", x.getIntervalInMilliseconds(), x.getValue())));

        final String random = RandomStringUtils.randomAlphanumeric(10);

        final Observable<String> value = Observable.just(random).zipWith(Observable.interval(150, TimeUnit.MILLISECONDS,
                    scheduler), (data, time) -> data);

        value.timeout(90, TimeUnit.MILLISECONDS, scheduler)
             .flatMap(Observable::just,
                 th -> {
                     log.error("Timeout");
                     return value.concatWith(
                             Observable.<String>empty().zipWith(
                                 Observable.interval(100, TimeUnit.MILLISECONDS, scheduler), (data, time) -> data)
                                     .timeout(250, TimeUnit.MILLISECONDS, scheduler));
                 },
                 Observable::empty).timeInterval().subscribe(timeSubscriber);

        scheduler.advanceTimeBy(80, TimeUnit.MILLISECONDS);
        timeSubscriber.assertNoValues();
        timeSubscriber.assertNotCompleted();

        scheduler.advanceTimeBy(75, TimeUnit.MILLISECONDS);
        timeSubscriber.assertNoValues();
        timeSubscriber.assertNoErrors();
        timeSubscriber.assertNotCompleted();

        scheduler.advanceTimeBy(250, TimeUnit.MILLISECONDS);
        timeSubscriber.assertNoErrors();

    }
}
