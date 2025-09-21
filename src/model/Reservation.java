package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Reservation (
	String id,
	int userId,
	int flightid,
	int airplaneId,
	String seatId,
	LocalDateTime expirationDate,
	PassengerType passengerType,
	BigDecimal price
) {

}