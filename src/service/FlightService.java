package service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import model.Flight;
import model.FlightDetails;
import model.Seat;
import repository.FlightRepository;
import model.FlightFilter;
import util.Result;

public class FlightService {

    private final FlightRepository repository;

    public FlightService(FlightRepository repository) {

        this.repository = repository;
    }

    public Result<List<Flight>> search(Map<String, String> queryParams) {
        return Result.of(() -> {
            FlightFilter filter = FlightFilter.fromQuery(queryParams);
            
            return repository.findFlights(filter)
                .orElseThrow()
                .orElseGet(List::of);
        });
    }

    public Result<Optional<FlightDetails>> getFlightDetails(int flightId) {

        return Result.of(() -> {
            Optional<Flight> flightOpt = repository.getFlight(flightId).orElseThrow();

            if (flightOpt.isEmpty()) return Optional.empty();

            Flight flight = flightOpt.get();

            Optional<List<Seat>> seats = repository.getSeatsForFlight(flight.id()).orElseThrow();

            return Optional.of(
                new FlightDetails(flight, seats.get())
            );
        });
    }
}
