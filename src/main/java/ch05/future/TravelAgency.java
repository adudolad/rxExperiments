package ch05.future;

import java.util.concurrent.CompletableFuture;

import ch04.booking.Flight;

import rx.Observable;

public interface TravelAgency {
    Flight search(User user, GeoLocation geoLocation);

    CompletableFuture<Flight> searchAsync(User user, GeoLocation geoLocation);

    Observable<Flight> rxSearch(User user, GeoLocation geoLocation);

    Observable<Flight> rxSearch2(User user, GeoLocation geoLocation);
}
