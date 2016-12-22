package ch04;

import java.math.BigDecimal;

import org.apache.commons.lang3.tuple.Pair;

import org.junit.Before;
import org.junit.Test;

import ch04.grocery.RxGroceries;

import ch04.utils.RxExecutors;

import lombok.extern.slf4j.Slf4j;

import rx.Observable;
import rx.Scheduler;

@Slf4j
public class RxGroceriesTest {
    private RxGroceries rxGroceries;

    @Before
    public void setUp() {
        rxGroceries = new RxGroceries();
    }

    @Test
    public void testShouldPurchaseProductsSequentiallyNoSubsrcibeOn() {
        final Observable<BigDecimal> totalPrice = Observable.just("bread", "butter", "milk", "tomato", "cheese")
                                                            .flatMap(prod -> rxGroceries.rxPurchase(prod, 1))
                                                            .reduce(BigDecimal::add).single();
        totalPrice.subscribe((total) -> { log.info("Total price of your purchase is [{}]", total); });
    }

    @Test
    public void testShouldPurchaseProductsSequentiallyWithSunbscribeOn() throws InterruptedException {
        final Scheduler scheduler = RxExecutors.getPoolB();
        final Observable<BigDecimal> totalPrice = Observable.just("bread", "butter", "milk", "tomato", "cheese")
                                                            .flatMap(prod -> rxGroceries.rxPurchase(prod, 1))
                                                            .subscribeOn(scheduler).reduce(BigDecimal::add).single();
        totalPrice.subscribe((total) -> { log.info("Total price of your purchase is [{}]", total); });
        Thread.sleep(10000);
    }

    @Test
    public void testShouldPurchaseProductsConcurrently() throws InterruptedException {
        final Scheduler scheduler = RxExecutors.getPoolB();

        final Observable<BigDecimal> totalPrice = Observable.just("bread", "butter", "milk", "tomato", "cheese", "eggs",
                                                                "potato", "sugar", "salt").flatMap(prod ->
                    rxGroceries.rxPurchase(prod, 1).subscribeOn(scheduler)).reduce(BigDecimal::add).single();
        totalPrice.subscribe((total) -> { log.info("Total price of your purchase is [{}]", total); });
        Thread.sleep(10000);
    }

    @Test
    public void testShouldPurchaseProductsConcurrentlyDifferentObserveOn() throws InterruptedException {
        final Scheduler scheduler = RxExecutors.getPoolC();

        final Observable<BigDecimal> totalPrice = Observable.just("bread", "butter", "milk", "tomato", "cheese", "eggs",
                                                                "potato", "sugar", "salt").observeOn(scheduler)
                                                            .flatMap(prod -> rxGroceries.rxPurchase(prod, 1))
                                                            .reduce(BigDecimal::add).single();
        totalPrice.subscribe((total) -> { log.info("Total price of your purchase is [{}]", total); });
        Thread.sleep(10000);
    }

    @Test
    public void testShouldPurchaseSimilarProducts() throws InterruptedException {
        final Scheduler scheduler = RxExecutors.getPoolB();

        final Observable<BigDecimal> totalPrice = Observable.just("bread", "butter", "milk", "tomato", "cheese", "egg",
                                                                "potato", "egg", "egg").groupBy(prod ->
                                                                    prod).flatMap(group ->
                                                                    group.count().map(quantity ->
                                                                            Pair.of(group.getKey(), quantity)))
                                                            .flatMap(prod ->
                                                                    rxGroceries.rxPurchase(prod.getLeft(),
                                                                        prod.getRight()).subscribeOn(scheduler))
                                                            .reduce(BigDecimal::add).single();
        totalPrice.subscribe((total) -> { log.info("Total price of your purchase is [{}]", total); });

        Thread.sleep(10000);
    }
}
