package ch05.future;

import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang3.RandomUtils;

import ch04.booking.Flight;

import ch04.utils.FutureUtil;

import lombok.Builder;
import lombok.Data;

import lombok.extern.slf4j.Slf4j;

import rx.Observable;

@Data
@Builder
@Slf4j
public class TravelAgencyMit implements TravelAgency {
    private String name;

    @Override
    public Flight search(final User user, final GeoLocation geoLocation) {
        int timeout = RandomUtils.nextInt(2000, 2100);
        log.info("Sleeping [{}]", timeout);
        try {
            Thread.sleep(timeout);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Flight flight = new Flight();
        try {
            log.info("Calling for a new flight");
            return flight.call();
        } catch (Exception e) {
            log.error("An ERROR", e);
            return null;
        }
    }

    public CompletableFuture<Flight> searchAsync(final User user, final GeoLocation geoLocation) {
        return CompletableFuture.supplyAsync(() -> search(user, geoLocation));
    }

    public Observable<Flight> rxSearch(final User user, final GeoLocation geoLocation) {
        return FutureUtil.observe(searchAsync(user, geoLocation));
    }

    public Observable<Flight> rxSearch2(final User user, final GeoLocation geoLocation) {
        return Observable.defer(() -> Observable.fromCallable(() -> search(user, geoLocation)));
    }
}
