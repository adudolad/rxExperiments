package ch06;

import java.math.BigDecimal;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.RandomUtils;

import org.junit.Test;

import lombok.extern.slf4j.Slf4j;

import rx.Observable;

@Slf4j
public class DebounceTest {
    @Test
    public void testStockExchangePriceWatcher() throws InterruptedException {
        Observable<BigDecimal> prices = Observable.just(new BigDecimal("100.89"), new BigDecimal("101.45"),
                                                      new BigDecimal("150.09"), new BigDecimal("145.04"),
                                                      new BigDecimal("162.89"), new BigDecimal("142.89"),
                                                      new BigDecimal("100")).zipWith(Observable.interval(50,
                                                          TimeUnit.MILLISECONDS), (d, t) ->
                                                          d);

        prices.debounce(x -> {
                  boolean goodPrice = x.compareTo(BigDecimal.valueOf(150)) > 0;
                  return Observable.empty().delay(goodPrice ? 10 : 100, TimeUnit.MILLISECONDS);
              }).subscribe(x -> log.info("Price is [{}]", x));

        Thread.sleep(5000);
    }

    @Test
    public void testStockExchangePriceWatcherWithTimeout() throws InterruptedException {
        Observable<BigDecimal> prices = Observable.just(new BigDecimal("100.89"), new BigDecimal("101.45"),
                                                      new BigDecimal("150.09"), new BigDecimal("145.04"),
                                                      new BigDecimal("162.89"), new BigDecimal("142.89"),
                                                      new BigDecimal("100"), new BigDecimal("124.56"),
                                                      new BigDecimal("149.36"), new BigDecimal("99.67")).zipWith(
                                                      Observable.interval(RandomUtils.nextInt(245, 255),
                                                          TimeUnit.MILLISECONDS), (d, t) ->
                                                          d);

        timeDebounce(prices).subscribe(x -> log.info("Price is [{}]", x));

        Thread.sleep(5000);
    }

    private Observable<BigDecimal> timeDebounce(final Observable<BigDecimal> upstream) {
        log.info("Inside timeDebounce");

        Observable<BigDecimal> onTimeout = upstream.takeLast(1).doOnNext(x ->
                                                           log.info("First value is [{}]", x)).concatWith(Observable
                                                           .defer(() ->
                                                                   timeDebounce(upstream)));

        return upstream.debounce(250, TimeUnit.MILLISECONDS).doOnNext(x -> log.info("debounce result is [{}]", x))
                       .timeout(1, TimeUnit.SECONDS, onTimeout);
    }
}
