package ch04.booking;

import java.util.concurrent.Callable;

import org.apache.commons.lang3.RandomUtils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import lombok.extern.slf4j.Slf4j;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
@ToString
public class Flight implements Callable<Flight> {
    private String number;

    @Override
    public Flight call() throws Exception {
        Thread.sleep(RandomUtils.nextInt(70, 130));

        Flight flight = Flight.builder().number("LOT " + RandomUtils.nextInt(100, 1000)).build();
        log.info("New flight has been created [{}]", flight);
        return flight;
    }
}
