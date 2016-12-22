package ch05.future;

import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang3.RandomUtils;

import ch04.utils.FutureUtil;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import rx.Observable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class GeoLocation {
    private long lat;
    private long lon;

    public GeoLocation getCurrentLocation() {
        int timeout = RandomUtils.nextInt(1000, 3000);
        log.info("Waiting for a GeoLocation [{}]", timeout);
        try {
            Thread.sleep(timeout);
        } catch (InterruptedException e) {
            log.error("Error while creating a GeoLocation", e);
            return null;
        }

        return GeoLocation.builder().lat(RandomUtils.nextLong(1, 100)).lon(RandomUtils.nextLong(1, 100)).build();
    }

    public CompletableFuture<GeoLocation> getCurrentLocationAsync() {
        return CompletableFuture.supplyAsync(this::getCurrentLocation);
    }

    public Observable<GeoLocation> rxGetCurrentLocation() {
        return FutureUtil.observe(getCurrentLocationAsync());
    }

    public Observable<GeoLocation> rxGetCurrentLocation2() {
        return Observable.defer(() -> Observable.fromCallable(this::getCurrentLocation));
    }
}
