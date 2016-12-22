package ch04.booking;

public interface BookingService {
    Flight lookupFlight(String flightNumber);

    Passenger findPassenger(int id);

    Ticket bookTicket(Flight flight, Passenger passenger);

    void sendEmailConfirmation(Ticket ticket);
}
