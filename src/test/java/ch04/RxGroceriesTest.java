package ch04;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;

import lombok.extern.slf4j.Slf4j;

import rx.Observable;

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
        final Observable<BigDecimal> totalPrice = Observable.just("bread", "butter", "milk", "tomato", "cheese")
                                                            .flatMap(prod -> rxGroceries.rxPurchase(prod, 1))
                                                            .subscribeOn(RxExecutors.getPoolA()).reduce(BigDecimal::add)
                                                            .single();
        totalPrice.subscribe((total) -> { log.info("Total price of your purchase is [{}]", total); });
        Thread.sleep(10000);
    }

    @Test
    public void testShouldPurchaseProductsConcurrently() throws InterruptedException {
        final Observable<BigDecimal> totalPrice = Observable.just("bread", "butter", "milk", "tomato", "cheese")
                                                            .flatMap(prod ->
                                                                    rxGroceries.rxPurchase(prod, 1).subscribeOn(
                                                                        RxExecutors.getPoolA())).reduce(BigDecimal::add)
                                                            .single();
        totalPrice.subscribe((total) -> { log.info("Total price of your purchase is [{}]", total); });
        Thread.sleep(10000);
    }
}
