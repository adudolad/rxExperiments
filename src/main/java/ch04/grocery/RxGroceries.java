package ch04.grocery;

import java.math.BigDecimal;

import org.apache.commons.lang3.RandomUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observable;

public class RxGroceries {
    public static final Logger log = LoggerFactory.getLogger(RxGroceries.class);

    public BigDecimal doPurchase(final String productName, final int quantity) {
        log.info("Purchasing " + productName + ", quantity " + quantity);

        final BigDecimal priceForProduct = BigDecimal.valueOf(RandomUtils.nextDouble(10.0, 100.0));
        try {
            log.info("Sleeping in thread [{}]", Thread.currentThread().getName());
            Thread.sleep(1000 + quantity * RandomUtils.nextInt(100, 1000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        log.info("Done [{}], quantity [{}], price per item [{}], total price [{}]", productName, quantity,
            priceForProduct, priceForProduct.multiply(new BigDecimal(quantity)));
        return priceForProduct;
    }

    public Observable<BigDecimal> rxPurchase(final String productName, final int quantity) {
        return Observable.fromCallable(() -> doPurchase(productName, quantity));
    }
}
