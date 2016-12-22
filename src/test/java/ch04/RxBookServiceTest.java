package ch04;

import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import org.junit.runner.RunWith;

import org.mockito.Mock;

import org.mockito.junit.MockitoJUnitRunner;

import com.google.common.collect.Lists;

import ch04.book.Book;
import ch04.book.BookService;

import lombok.extern.slf4j.Slf4j;

import rx.Observable;

import rx.observers.Subscribers;
import rx.observers.TestSubscriber;

import rx.schedulers.Schedulers;
import rx.schedulers.TestScheduler;
import rx.schedulers.TimeInterval;

@RunWith(MockitoJUnitRunner.class)
@Slf4j
public class RxBookServiceTest {

    @Mock
    private BookService bookService;

    private TestScheduler scheduler;
    private TestSubscriber<String> subscriber;

    @Before
    public void setUo() {
        scheduler = Schedulers.test();
        subscriber = new TestSubscriber<>();
    }

    @Test
    public void testShouldGetPersonalRecomendation() throws InterruptedException {
        when(bookService.recommendation(any(String.class))).thenReturn(Observable.just(
                Book.builder().title("your_book_1").build(), Book.builder().title("your_book_2").build()).zipWith(
                Observable.interval(300, TimeUnit.MILLISECONDS, scheduler), (data, time) -> data));

        when(bookService.bestSeller()).thenReturn(Observable.just(Book.builder().title("best_book_1").build(),
                Book.builder().title("best_book_2").build()));

        final Observable<Book> recommended = bookService.recommendation("customer1");
        final Observable<Book> bestSeller = bookService.bestSeller();
        final Observable<String> bookTitle = recommended.timeout(1000, TimeUnit.MILLISECONDS, scheduler)
                                                        .onErrorResumeNext(bestSeller).map(Book::getTitle);

        bookTitle.subscribe(subscriber);

        scheduler.advanceTimeBy(190, TimeUnit.MILLISECONDS);
        subscriber.assertNoValues();
        subscriber.assertNotCompleted();

        scheduler.advanceTimeBy(150, TimeUnit.MILLISECONDS);
        subscriber.assertNoErrors();
        subscriber.assertValues("your_book_1");

        scheduler.advanceTimeBy(370, TimeUnit.MILLISECONDS);
        subscriber.assertValues("your_book_1", "your_book_2");
    }

    @Test
    public void testShouldGetBestSellersAsFallback() throws InterruptedException {
        when(bookService.recommendation(any(String.class))).thenReturn(Observable.just(
                Book.builder().title("your_book_1").build(), Book.builder().title("your_book_2").build()).zipWith(
                Observable.interval(300, TimeUnit.MILLISECONDS, scheduler), (data, time) -> data));

        when(bookService.bestSeller()).thenReturn(Observable.just(Book.builder().title("best_book_1").build(),
                Book.builder().title("best_book_2").build()));

        final Observable<Book> recommended = bookService.recommendation("customer1");
        final Observable<Book> bestSeller = bookService.bestSeller();
        final Observable<String> bookTitle = recommended.timeout(200, TimeUnit.MILLISECONDS, scheduler)
                                                        .onErrorResumeNext(bestSeller).map(Book::getTitle);

        bookTitle.subscribe(subscriber);

        scheduler.advanceTimeBy(190, TimeUnit.MILLISECONDS);
        subscriber.assertNoValues();
        subscriber.assertNotCompleted();

        scheduler.advanceTimeBy(150, TimeUnit.MILLISECONDS);
        subscriber.assertValues("best_book_1", "best_book_2");
        subscriber.assertCompleted();
    }

    @Test
    public void testShouldGetBestSellersWithDelayAsFallback() throws InterruptedException {

        TestSubscriber<TimeInterval<String>> timeSubscriber = TestSubscriber.create(Subscribers.create((x) ->
                        log.info("Time [{}], value [{}]", x.getIntervalInMilliseconds(), x.getValue())));

        when(bookService.recommendation(any(String.class))).thenReturn(Observable.just(
                Book.builder().title("your_book_1").build(), Book.builder().title("your_book_2").build()).zipWith(
                Observable.interval(300, 100, TimeUnit.MILLISECONDS, scheduler), (data, time) -> data));

        when(bookService.bestSeller()).thenReturn(Observable.just(Book.builder().title("best_book_1").build(),
                Book.builder().title("best_book_2").build()).zipWith(
                Observable.interval(50, 50, TimeUnit.MILLISECONDS, scheduler), (data, time) -> data));

        final Observable<Book> recommended = bookService.recommendation("customer1");
        final Observable<Book> bestSeller = bookService.bestSeller();
        final Observable<TimeInterval<String>> bookTitle = recommended.timeout(() ->
                                                                              Observable.timer(350,
                                                                                  TimeUnit.MILLISECONDS, scheduler),
                                                                          date ->
                                                                              Observable.timer(70,
                                                                                  TimeUnit.MILLISECONDS, scheduler))
                                                                      .onErrorResumeNext(bestSeller).map(Book::getTitle)
                                                                      .timeInterval();

        bookTitle.subscribeOn(scheduler).subscribe(timeSubscriber);
        bookTitle.map(TimeInterval::getValue).subscribe(subscriber);

        scheduler.advanceTimeBy(290, TimeUnit.MILLISECONDS);
        subscriber.assertNoValues();
        subscriber.assertNotCompleted();

        scheduler.advanceTimeBy(50, TimeUnit.MILLISECONDS);
        subscriber.assertReceivedOnNext(Lists.newArrayList("your_book_1"));

        scheduler.advanceTimeBy(80, TimeUnit.MILLISECONDS);
        subscriber.assertReceivedOnNext(Lists.newArrayList("your_book_1", "best_book_1"));

        scheduler.advanceTimeBy(50, TimeUnit.MILLISECONDS);
        subscriber.assertValues("your_book_1", "best_book_1", "best_book_2");
        subscriber.assertCompleted();
    }
}
