package service;

import java.util.List;
import util.Result;

public class BookingService {

    private final SeatRepository seatRepository;

    public BookingService(SeatRepository seatRepository) {

        this.seatRepository = seatRepository;
    }

    public Result<List<Seat>> getSeatsForFlight(String flightId) {
    	
        return seatRepository.findSeatsByFlight(flightId);
    }
}