package ch04;

import static org.mockito.ArgumentMatchers.anyInt;

import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.tuple.Pair;

import org.junit.Before;
import org.junit.Test;

import org.junit.runner.RunWith;

import org.mockito.Mock;

import org.mockito.junit.MockitoJUnitRunner;

import com.google.common.collect.Lists;

import ch04.people.PeopleRepository;

import ch04.utils.RxExecutors;

import lombok.extern.slf4j.Slf4j;

import rx.Observable;

import rx.observers.Subscribers;
import rx.observers.TestSubscriber;

import rx.schedulers.Schedulers;
import rx.schedulers.TestScheduler;
import rx.schedulers.TimeInterval;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class RxPeopleListTest {

    private TestSubscriber<TimeInterval<String>> timeSubscriber;
    private TestSubscriber<String> subscriber;
    private TestScheduler scheduler;
    @Mock
    private PeopleRepository peopleRepository;

    @Before
    public void setup() {
        scheduler = Schedulers.test();
        subscriber = new TestSubscriber<>();
        timeSubscriber = TestSubscriber.create(Subscribers.create(x ->
                        log.info("Value is [{}], in [{}] ms", x.getValue(), x.getIntervalInMilliseconds())));
    }

    @Test
    public void testShouldLoadListOfPeople() {
        when(peopleRepository.getPeople()).thenReturn(getResultWithDelay(7, 0));

        Observable<String> allPeople = Observable.defer(() -> Observable.from(peopleRepository.getPeople()));

        allPeople.timeInterval().subscribe(timeSubscriber);
    }

    @Test
    public void testShouldLoadListOfPeopleWithPages() throws InterruptedException {
        when(peopleRepository.getPeople(anyInt())).thenReturn(getResultWithDelay(7, 0));

        Observable<String> allPeople = allPeople(0);

        allPeople.timeInterval().subscribe((x) -> log.info("Result [{}]", x));

        Thread.sleep(7000);
    }

    private Observable<String> allPeople(final int initNumber) {

        if (initNumber == 10) {
            return Observable.empty();
        }

        return Observable.defer(() -> Observable.from(peopleRepository.getPeople(initNumber)))
                         .observeOn(RxExecutors.getPoolB()).concatWith(Observable.defer(() -> {
                                 when(peopleRepository.getPeople(anyInt())).thenReturn(
                                     getResultWithDelay(7, initNumber + 1));
                                 return allPeople(initNumber + 1);
                             }));
    }

    private List<String> getResultWithDelay(final int number, final int pages) {
        log.info("Sleeping [{}] ....", Thread.currentThread().getName());
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        log.info("Waked up [{}] ....", Thread.currentThread().getName());

        final List<String> list = Lists.newArrayList();
        for (int i = number * pages; i < (pages + 1) * number; i++) {
            list.add("man_" + i);
        }

        return list;
    }

    @Test
    public void testConcatVsMerge() {
        Observable<String> strings = Observable.just("a", "b", "c", "d").delay(100, TimeUnit.MILLISECONDS, scheduler);
        Observable<String> strings1 = Observable.just("a", "b", "c", "d").zipWith(Observable.timer(100,
                    TimeUnit.MILLISECONDS, scheduler), (data, timer) -> data);

        Observable<String> numbers = Observable.just("1", "2", "3", "4").delay(110, TimeUnit.MILLISECONDS, scheduler);

        strings1.concatWith(numbers).timeInterval().subscribeOn(scheduler).subscribe(timeSubscriber);
        strings1.concatWith(numbers).subscribeOn(scheduler).subscribe(subscriber);

        scheduler.advanceTimeBy(100, TimeUnit.MILLISECONDS);
        subscriber.assertReceivedOnNext(Lists.newArrayList("a"));

        scheduler.advanceTimeBy(20, TimeUnit.MILLISECONDS);
        subscriber.assertValues("a");

        scheduler.advanceTimeBy(90, TimeUnit.MILLISECONDS);
        subscriber.assertReceivedOnNext(Lists.newArrayList("a", "1", "2", "3", "4"));
        subscriber.onCompleted();
    }

    @Test
    public void testConcatVsMerge2() {
        Observable<String> strings = Observable.just("a", "b", "c", "d").delay(100, TimeUnit.MILLISECONDS, scheduler);
        Observable<String> strings1 = Observable.just("a", "b", "c", "d").zipWith(Observable.interval(100,
                    TimeUnit.MILLISECONDS, scheduler), (data, timer) -> data);

        Observable<String> numbers = Observable.just("1", "2", "3", "4").zipWith(Observable.interval(110,
                    TimeUnit.MILLISECONDS, scheduler), (data, timer) -> data);

        // Concat
        strings1.concatWith(numbers).timeInterval().subscribeOn(scheduler).subscribe(timeSubscriber);
        strings1.concatWith(numbers).subscribeOn(scheduler).subscribe(subscriber);

        scheduler.advanceTimeBy(100, TimeUnit.MILLISECONDS);
        subscriber.assertReceivedOnNext(Lists.newArrayList("a"));

        scheduler.advanceTimeBy(20, TimeUnit.MILLISECONDS);
        subscriber.assertReceivedOnNext(Lists.newArrayList("a"));

        scheduler.advanceTimeBy(90, TimeUnit.MILLISECONDS);
        subscriber.assertReceivedOnNext(Lists.newArrayList("a", "b"));
        subscriber.assertNotCompleted();

        scheduler.advanceTimeBy(95, TimeUnit.MILLISECONDS);
        subscriber.assertReceivedOnNext(Lists.newArrayList("a", "b", "c"));
        subscriber.assertNotCompleted();

        scheduler.advanceTimeBy(95, TimeUnit.MILLISECONDS);
        subscriber.assertReceivedOnNext(Lists.newArrayList("a", "b", "c", "d"));
        subscriber.assertNotCompleted();

        scheduler.advanceTimeBy(110, TimeUnit.MILLISECONDS);
        subscriber.assertReceivedOnNext(Lists.newArrayList("a", "b", "c", "d", "1"));
        subscriber.assertNotCompleted();

        scheduler.advanceTimeBy(1000, TimeUnit.MILLISECONDS);
        subscriber.assertReceivedOnNext(Lists.newArrayList("a", "b", "c", "d", "1", "2", "3", "4"));
        subscriber.assertCompleted();
    }

    @Test
    public void testConcatVsMerge3() {
        final Observable<String> strings = Observable.just("a", "b", "c", "d").zipWith(Observable.interval(100,
                    TimeUnit.MILLISECONDS, scheduler), (data, timer) -> data);

        final Observable<String> numbers = Observable.just("1", "2", "3", "4").zipWith(Observable.interval(110,
                    TimeUnit.MILLISECONDS, scheduler), (data, timer) -> data);

        // Merge
        strings.mergeWith(numbers).timeInterval().subscribeOn(scheduler).subscribe(timeSubscriber);
        strings.mergeWith(numbers).subscribeOn(scheduler).subscribe(subscriber);

        scheduler.advanceTimeBy(100, TimeUnit.MILLISECONDS);
        subscriber.assertReceivedOnNext(Lists.newArrayList("a"));

        scheduler.advanceTimeBy(20, TimeUnit.MILLISECONDS);
        subscriber.assertReceivedOnNext(Lists.newArrayList("a", "1"));

        scheduler.advanceTimeBy(90, TimeUnit.MILLISECONDS);
        subscriber.assertNotCompleted();
        subscriber.assertReceivedOnNext(Lists.newArrayList("a", "1", "b"));

        scheduler.advanceTimeBy(95, TimeUnit.MILLISECONDS);
        subscriber.assertReceivedOnNext(Lists.newArrayList("a", "1", "b", "2", "c"));
        subscriber.assertNotCompleted();

        scheduler.advanceTimeBy(1000, TimeUnit.MILLISECONDS);
        subscriber.assertReceivedOnNext(Lists.newArrayList("a", "1", "b", "2", "c", "3", "d", "4"));
        subscriber.assertCompleted();
    }

    @Test
    public void testZip() {

        TestSubscriber<TimeInterval<Pair<String, String>>> timeSubscriber = TestSubscriber.create(Subscribers.create(
                    x -> log.info("Value is [{}], in [{}] ms", x.getValue(), x.getIntervalInMilliseconds())));

        TestSubscriber<Pair<String, String>> subscriber = new TestSubscriber<>();

        final Observable<String> strings = Observable.just("a", "b", "c", "d").zipWith(Observable.interval(100,
                    TimeUnit.MILLISECONDS, scheduler), (data, timer) -> data);

        final Observable<String> numbers = Observable.just("1", "2", "3", "4").zipWith(Observable.interval(110,
                    TimeUnit.MILLISECONDS, scheduler), (data, timer) -> data);

        // Merge
        strings.zipWith(numbers, Pair::of).timeInterval().subscribeOn(scheduler).subscribe(timeSubscriber);
        strings.zipWith(numbers, Pair::of).subscribeOn(scheduler).subscribe(subscriber);

        scheduler.advanceTimeBy(105, TimeUnit.MILLISECONDS);
        subscriber.assertNoValues();

        scheduler.advanceTimeBy(10, TimeUnit.MILLISECONDS);
        subscriber.assertReceivedOnNext(Lists.newArrayList(Pair.of("a", "1")));

        scheduler.advanceTimeBy(115, TimeUnit.MILLISECONDS);
        subscriber.assertReceivedOnNext(Lists.newArrayList(Pair.of("a", "1"), Pair.of("b", "2")));
    }

}
