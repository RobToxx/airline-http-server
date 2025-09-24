package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record FlightBooking (
	Flight flight,
	List<SeatBook> seats
) {

	public record SeatBook(
		String seatId,
		Seat.Class seatClass,
		LocalDateTime purchaseDate,
		PassengerType passengerType,
		BigDecimal price
	) {

	}
}