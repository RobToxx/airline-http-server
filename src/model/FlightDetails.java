package model;

import java.util.List;

public record FlightDetails (
    Flight flight,
    List<Seat> seats
) {
    
}