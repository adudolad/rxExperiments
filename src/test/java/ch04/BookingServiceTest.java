package ch04;

import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.when;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.tuple.Pair;

import org.junit.Before;
import org.junit.Test;

import org.junit.runner.RunWith;

import org.mockito.Mock;

import org.mockito.junit.MockitoJUnitRunner;

import org.mockito.stubbing.Answer;

import ch04.booking.BookingService;
import ch04.booking.Flight;
import ch04.booking.Passenger;
import ch04.booking.Ticket;

import lombok.extern.slf4j.Slf4j;

import rx.Observable;

import rx.observers.Subscribers;
import rx.observers.TestSubscriber;

import rx.schedulers.Schedulers;
import rx.schedulers.TestScheduler;
import rx.schedulers.TimeInterval;

@RunWith(MockitoJUnitRunner.class)
@Slf4j
public class BookingServiceTest {
    @Mock
    private BookingService bookingService;

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
    public void test1() throws InterruptedException {
        getResultWithDelay("LOT 783");
        getResultWithDelay(42);
        getResultWithDelay(Flight.builder().build());

        final Observable<Flight> flight = rxLookFlight("LOT 783").subscribeOn(Schedulers.io());
        final Observable<Passenger> passenger = rxFindPassender(42).subscribeOn(Schedulers.io());

        final Observable<Ticket> ticket = flight.zipWith(passenger, Pair::of).flatMap(pair ->
                    rxBookTicketCallable(pair.getLeft(), pair.getRight()).subscribeOn(Schedulers.io()));
        ticket.subscribe(x -> bookingService.sendEmailConfirmation(x));

        Thread.sleep(5000);
    }

    private Observable<Flight> rxLookFlight(final String flightNumber) {
        return Observable.defer(() -> Observable.just(bookingService.lookupFlight(flightNumber)));
    }

    private Observable<Passenger> rxFindPassender(final int id) {
        return Observable.defer(() -> Observable.just(bookingService.findPassenger(id)));
    }

    private Observable<Ticket> rxBookTicket(final Flight flight, final Passenger passenger) {
        return Observable.defer(() -> Observable.just(bookingService.bookTicket(flight, passenger)));
    }

    private Observable<Ticket> rxBookTicketCallable(final Flight flight, final Passenger passenger) {
        return Observable.fromCallable(() -> bookingService.bookTicket(flight, passenger));
    }

    private <T> void getResultWithDelay(final T param) {

        if (param instanceof String) {
            when(bookingService.lookupFlight((String) param)).thenAnswer((Answer<Flight>) invocation -> {
                    sleep(300);
                    return Flight.builder().number((String) param).build();
                });

        } else if (param instanceof Integer) {
            when(bookingService.findPassenger((Integer) param)).thenAnswer((Answer<Passenger>) invocation -> {
                    sleep(700);
                    return Passenger.builder().id((Integer) param).build();
                });
        } else if (param instanceof Flight) {
            when(bookingService.bookTicket(any(Flight.class), any(Passenger.class))).thenAnswer((Answer<Ticket>)
                invocation -> {
                    sleep(401);
                    return Ticket.builder().code(RandomStringUtils.randomAlphanumeric(15)).build();
                });
        } else if (param instanceof Ticket) { }
    }

    private void sleep(final int timeout) {
        log.info("Sleeping [{}] in [{}] ....", timeout, Thread.currentThread().getName());
        try {
            Thread.sleep(timeout);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        log.info("Waked up in [{}]", Thread.currentThread().getName());
    }
}
