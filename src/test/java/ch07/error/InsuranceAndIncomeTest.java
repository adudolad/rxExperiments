package ch07.error;

import org.apache.commons.lang3.RandomUtils;

import org.junit.Before;
import org.junit.Test;

import lombok.extern.slf4j.Slf4j;

import rx.Observable;

@Slf4j
public class InsuranceAndIncomeTest {

    private Observable<Person> person;

    @Before
    public void setUp() {
        person = Observable.just(Person.builder().name("Mike").build(), Person.builder().name("Juli").build(),
                Person.builder().name("Billy").build());

    }

    @Test
    public void testCalculatesFallback() {
        Observable<Income> income = person.flatMap(this::determineIncome).doOnError(x -> log.error("Error ", x))
                                          .onErrorResumeNext(person.flatMap(this::guessIncome));
        income.subscribe(x -> log.info("Result is [{}]", x));
    }

    @Test
    public void testCalculatesFallbackWithConcatMap() {
        person.flatMap(this::determineIncome)
              .flatMap(Observable::just, th -> person.flatMap(this::guessIncome), Observable::empty).subscribe(x ->
                      log.info("Result is [{}]", x));
    }

    @Test
    public void testAdvancedOnErrorResume() {
        Observable<Income> income = person.flatMap(this::determineIncome).onErrorResumeNext(th -> {
                if (th instanceof NullPointerException) {
                    return Observable.error(th);
                } else {
                    return person.flatMap(this::guessIncome);
                }
            });

        income.subscribe(x -> log.info("Result is [{}]", x));
    }

    private Observable<Income> determineIncome(final Person person) {
        int random = RandomUtils.nextInt(1, 5);
        log.info("Random value is [{}]", random);
        if (random > 2) {
            throw new RuntimeException("Service error");
        }

        return Observable.just(Income.builder().gross(1000 * person.getName().length()).build());
    }

    private Observable<Income> guessIncome(final Person person) {
        final long avrIncomeByName = 777;

        return Observable.just(Income.builder().gross(avrIncomeByName).build());
    }
}
