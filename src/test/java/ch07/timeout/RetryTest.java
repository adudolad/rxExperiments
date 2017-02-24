package ch07.timeout;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Test;

import lombok.extern.slf4j.Slf4j;

import rx.Observable;

@Slf4j
public class RetryTest {
    private static final int numberOfRetries = 10;

    @Test
    public void testSimpleRetry() {
        risky().timeout(1, TimeUnit.SECONDS).doOnError(th -> log.warn("Will retry", th)).retry().subscribe(x ->
                log.info("Result x"));

    }

    @Test
    public void testAdvancedRetry() {
        risky().timeout(1, TimeUnit.SECONDS).doOnError(th -> log.warn("Will retry", th)).retry((attempt, e) ->
                       attempt <= numberOfRetries && (e instanceof TimeoutException)).subscribe(x ->
                       log.info("Result x"));

    }

    @Test
    public void testRetryWhen() {
//
// risky().timeout(1, TimeUnit.SECONDS).doOnError(th -> log.warn("Will retry", th)).retryWhen(rt ->
// rt.take(numberOfRetries)).subscribe(x -> log.info("Result x"));

        risky().timeout(1, TimeUnit.SECONDS).doOnError(th -> log.warn("Will retry", th)).retryWhen(failures ->
                       failures.zipWith(Observable.range(1, numberOfRetries),
                           (err, num) ->
                               num < numberOfRetries ? Observable.timer(1, TimeUnit.SECONDS) : Observable.error(err))
                           .flatMap(x -> x)).subscribe(x -> log.info("Result x"));

    }

    private Observable<String> risky() {
        return Observable.fromCallable(() -> {
                if (Math.random() < 0.1) {
                    Thread.sleep((long) (Math.random() * 2000));
                    log.info("Success");
                    return "OK";
                } else {
                    throw new TimeoutException("I'm timed out");
                }
            });
    }
}
