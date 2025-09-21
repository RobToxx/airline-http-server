package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Booking (
	int userId,
	int flightid,
	int airplaneId,
	String seatId,
	LocalDateTime purchaseDate,
	PassengerType passengerType,
	BigDecimal price
) {

}