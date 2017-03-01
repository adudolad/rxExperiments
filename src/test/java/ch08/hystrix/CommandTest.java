package ch08.hystrix;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import lombok.extern.slf4j.Slf4j;

import rx.Observable;

import rx.observables.BlockingObservable;

import rx.schedulers.Schedulers;

@Slf4j
public class CommandTest {
    private BlockingCmd blockingCmd;

    @Test
    public void testCallsBlockingCommand() throws InterruptedException {
        blockingCmd = new BlockingCmd(950);

        final Observable<String> retried = blockingCmd.toObservable().doOnError(ex -> log.warn("Error", ex))
                                                      .retryWhen(x -> x.delay(100, TimeUnit.MILLISECONDS)).timeout(2,
                                                          TimeUnit.SECONDS);

        retried.subscribe(x -> log.info("Result is [{}]", x));

        Thread.sleep(1500);
    }

    @Test
    public void testHasTwoTimeOuts() throws InterruptedException {
        blockingCmd = new BlockingCmd(500);

        final BlockingObservable<String> retried = blockingCmd.toObservable().timeout(500, TimeUnit.MILLISECONDS)
                                                              .doOnError(ex -> log.warn("Error", ex)).toBlocking();

        retried.subscribe(x -> log.info("Result is [{}]", x), throwable -> { log.error("ERROR", throwable); });

        Thread.sleep(1500);
    }

    @Test
    public void testHasTwoTimeOutsNotBlocking() throws InterruptedException {
        blockingCmd = new BlockingCmd(500);

        final Observable<String> retried = blockingCmd.toObservable().doOnUnsubscribe(() -> log.info("Unsubscribed"));

        final Observable<String> blockTime = Observable.just("no")
                                                       .zipWith(Observable.interval(400, TimeUnit.MILLISECONDS),
                                                           (data, time) ->
                                                               data).doOnUnsubscribe(() ->
                                                               log.info("Unsubscribed NULL"));
        ;

        blockTime.ambWith(retried).subscribe(x -> log.info("Result is [{}]", x),
            throwable -> { log.error("ERROR", throwable); });

        Thread.sleep(1500);
    }

    @Test
    public void testConnectableObservable() throws InterruptedException {
        blockingCmd = new BlockingCmd(500);

        final Observable<String> retried = blockingCmd.toObservable().share();

        retried.doOnSubscribe(() -> log.info("First Subscribed")).doOnUnsubscribe(() ->
                       log.info("First unsubscribed")).timeout(300, TimeUnit.MILLISECONDS).subscribe(x ->
                       log.info("First Result [{}]", x),
                   throwable -> log.error("ERROR"));

        retried.doOnUnsubscribe(() -> log.info("Second unsubscribed")).doOnSubscribe(() ->
                       log.info("Second Subscribed")).subscribe(x -> log.info("Result of SECOND [{}]", x));

        Thread.sleep(1500);
    }

    @Test
    public void testGetResultAfterTheFirstTimeOutAndExecuteOnUnsubscribe() throws InterruptedException {

        final Observable<String> noValue = Observable.just("NO VALUE - TIMEOUT")
                                                     .zipWith(Observable.timer(300, TimeUnit.MILLISECONDS,
                                                             Schedulers.immediate()), (d, t) ->
                                                             d).doOnSubscribe(() ->
                                                             log.info("First Subscribed")).doOnUnsubscribe(() ->
                                                             log.info("First unsubscribed"));

        // http://reactivex.io/documentation/operators/refcount.html
        final Observable<String> hystrix = new BlockingCmd(500).toObservable().share();

        hystrix.doOnSubscribe(() -> log.info("Second Subscribed")).observeOn(Schedulers.io()).doOnUnsubscribe(() ->
                log.info("Second unsubscribed")).subscribe();

        final Observable<String> single = hystrix.mergeWith(noValue);
        final String result = single.toBlocking().firstOrDefault("NO VALUE - DEFAULT");

        log.info("Result is [{}]", result);
        assertThat(result).isEqualTo("NO VALUE - TIMEOUT");

        Thread.sleep(1500);
    }

    @Test
    public void testGetResultFromHystrixCall() throws InterruptedException {

        final Observable<String> noValue = Observable.just("NO VALUE - TIMEOUT")
                                                     .zipWith(Observable.timer(300, TimeUnit.MILLISECONDS,
                                                             Schedulers.immediate()), (d, t) ->
                                                             d).doOnSubscribe(() ->
                                                             log.info("First Subscribed")).doOnUnsubscribe(() ->
                                                             log.info("First unsubscribed"));

        // http://reactivex.io/documentation/operators/refcount.html
        final Observable<String> hystrix = new BlockingCmd(200).toObservable().share();

        hystrix.doOnSubscribe(() -> log.info("Second Subscribed")).observeOn(Schedulers.io()).doOnUnsubscribe(() ->
                log.info("Second unsubscribed")).subscribe();

        final Observable<String> single = hystrix.mergeWith(noValue);
        final String result = single.toBlocking().firstOrDefault("NO VALUE - DEFAULT");

        log.info("Result is [{}]", result);
        assertThat(result).isEqualTo("GOT COMMAND RESULT !!!");

        Thread.sleep(1500);
    }
}
