package ch05.future;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import ch04.utils.RxExecutors;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;

import org.junit.runner.RunWith;

import org.mockito.junit.MockitoJUnitRunner;

import com.google.common.collect.Lists;

import ch04.booking.Flight;

import lombok.extern.slf4j.Slf4j;

import rx.Observable;
import rx.Scheduler;
import rx.observers.Subscribers;
import rx.observers.TestSubscriber;

import rx.schedulers.Schedulers;
import rx.schedulers.TestScheduler;
import rx.schedulers.TimeInterval;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class TravelAgencyTest {
    private TestSubscriber<TimeInterval<String>> timeSubscriber;
    private TestSubscriber<String> subscriber;
    private TestScheduler scheduler;

    @Before
    public void setup() {
        scheduler = Schedulers.test();
        subscriber = new TestSubscriber<>();
        timeSubscriber = TestSubscriber.create(Subscribers.create(x ->
                        log.info("Value is [{}], in [{}] ms", x.getValue(), x.getIntervalInMilliseconds())));
    }

    @Test
    public void testUsingFutureOnly() throws InterruptedException, ExecutionException {
        final List<TravelAgency> agencies = Lists.newArrayList(TravelAgencyMit.builder().name("AIM").build(),
                TravelAgencyMit.builder().name("MIT").build(), TravelAgencyMit.builder().name("POL").build());
        final ExecutorService pool = Executors.newFixedThreadPool(10);

        User user = new User().findById(8789);
        GeoLocation geoLocation = new GeoLocation().getCurrentLocation();

        ExecutorCompletionService<Flight> ecs = new ExecutorCompletionService<>(pool);

        agencies.forEach(travelAgency -> {
            log.info("Submitting a new task");
            ecs.submit(travelAgency.search(user, geoLocation));
        });

        Future<Flight> firstFlight = ecs.poll(5, TimeUnit.SECONDS);
        Flight flight = firstFlight.get();

        log.info("Flight [{}]", flight);

    }

    @Test
    public void testUsingCompletableFutureOnly() {
        final List<TravelAgency> agencies = Lists.newArrayList(TravelAgencyMit.builder().name("AIM").build(),
                TravelAgencyMit.builder().name("MIT").build(), TravelAgencyMit.builder().name("POL").build());

        CompletableFuture<User> user = new User().findByIdAsync(8789);
        CompletableFuture<GeoLocation> currentLocation = new GeoLocation().getCurrentLocationAsync();

        //J-
        CompletableFuture<Flight> flightFuture = user
                .thenCombine(currentLocation,(User us, GeoLocation loc) -> 
                        agencies
                                .stream()
                                .map(agency -> agency.searchAsync(us, loc))
                                .peek(x-> log.info("Flight [{}] in the stream", x))
                                .reduce((f1, f2) -> 
                                        f1.applyToEither(f2, Function.identity()))
                                .get())
                .thenCompose(Function.identity());
        //J+
        try {
            log.info("Found flight [{}]", flightFuture.get(5, TimeUnit.SECONDS));
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.error("An error occurred", e);
        }
    }
    
    @Test 
    public void testUsingRx () throws InterruptedException {
        final Scheduler schedulerA = RxExecutors.getPoolA();
        final Scheduler schedulerB = RxExecutors.getPoolB();
        final Scheduler schedulerC = RxExecutors.getPoolC();
        
        Observable<TravelAgency> agencies = Observable.just(TravelAgencyMit.builder().name("AIM").build(),
                TravelAgencyMit.builder().name("MIT").build(), TravelAgencyMit.builder().name("POL").build());
        
        Observable<User> user = new User().rxFindById(989);
        Observable<GeoLocation> location = new GeoLocation().rxGetCurrentLocation();
        
        Observable<Flight> flight = user.zipWith(location, (u, l) -> agencies.flatMap(travelAgency -> travelAgency.rxSearch(u, l)).first()).flatMap(x->x);
        Observable<Flight> flight2 = user.observeOn(schedulerA).zipWith(location, Pair::of).flatMap(pair-> agencies.flatMap(agency -> 
            agency.rxSearch(pair.getLeft(), pair.getRight())).subscribeOn(schedulerB)).first();
        
        flight2.subscribeOn(schedulerC).subscribe(x->log.info("The first fligth is [{}]", x));
        
        Thread.sleep(6000);
    }

    @Test
    public void testUsingRx2 () throws InterruptedException {
        final Scheduler schedulerA = RxExecutors.getPoolA();
        final Scheduler schedulerB = RxExecutors.getPoolB();
        final Scheduler schedulerC = RxExecutors.getPoolC();

        Observable<TravelAgency> agencies = Observable.just(TravelAgencyMit.builder().name("AIM").build(),
                TravelAgencyMit.builder().name("MIT").build(), TravelAgencyMit.builder().name("POL").build());

        Observable<User> user = new User().rxFindById2(989).subscribeOn(schedulerA);
        Observable<GeoLocation> location = new GeoLocation().rxGetCurrentLocation2().subscribeOn(schedulerC);
        
        Observable<Flight> flight2 = user.zipWith(location, Pair::of).flatMap(pair-> agencies.flatMap(agency ->
                agency.rxSearch2(pair.getLeft(), pair.getRight()).subscribeOn(schedulerB))).first();

        flight2.subscribe(x->log.info("The first fligth is [{}]", x));

        Thread.sleep(15000);
    }
}
