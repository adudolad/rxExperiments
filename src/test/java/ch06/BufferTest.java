package ch06;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.RandomUtils;

import org.junit.Test;

import org.junit.runner.RunWith;

import org.mockito.junit.MockitoJUnitRunner;

import lombok.extern.slf4j.Slf4j;

import rx.Observable;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class BufferTest {
    private DbRepository repository = new DbRepositoryImpl();

    @Test
    public void testBufferCommand() throws InterruptedException {
        Observable.range(1, 7)
                  .zipWith(Observable.interval(RandomUtils.nextInt(10, 70), TimeUnit.MILLISECONDS), (r, t) ->
                          r).doOnNext(x -> log.info("Event [{}] is emitted ")).buffer(3).subscribe(
                      (List<Integer> list) -> { log.info("Resulted in [{}]", list); });

        Thread.sleep(1000);
    }

    @Test
    public void testBufferWithRepetition() {
        Observable.range(1, 7).buffer(3, 1).subscribe((List<Integer> list) -> { log.info("Resulted in [{}]", list); });
    }

    @Test
    public void testBufferWithRepetition2() {
        Observable.range(1, 70).buffer(1, 2).subscribe((List<Integer> list) -> { log.info("Resulted in [{}]", list); });
    }

    @Test
    public void testKeyEvents() throws InterruptedException {
        Observable<String> keyEvents = Observable.just("1", "2", "3", "4", "5", "6", "7").zipWith(Observable.interval(
                    RandomUtils.nextInt(100, 700), TimeUnit.MILLISECONDS), (k, d) -> k);

        Observable<Integer> eventsPerSecond = keyEvents.buffer(1, TimeUnit.SECONDS).map(List::size);

        eventsPerSecond.subscribe(x -> log.info("Result is [{}]", x));

        Observable<Observable<String>> windows = keyEvents.window(1, TimeUnit.SECONDS);
        windows.flatMap(x -> x.count()).subscribe(x -> log.info("Result of window is [{}]", x));

        Thread.sleep(5000);

    }

    @Test
    public void testBufferForDb() {
        Observable<String> events = Observable.just("first", "second", "third", "fourth");

        events.subscribe(repository::storeOne);

        events.buffer(2).subscribe(repository::storeAll);
    }

    private class DbRepositoryImpl implements DbRepository {

        @Override
        public void storeOne(final String record) {
            log.info("Event stored [{}]", record);
        }

        @Override
        public void storeAll(final List<String> records) {
            log.info("Event stored [{}]", records);
        }
    }
}
