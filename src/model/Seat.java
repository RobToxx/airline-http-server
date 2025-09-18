package model;

public record Seat(
	String id, 
	Seat.Class seatClass, 
	Seat.Status status
) {

	public enum Class {
		FIRST,
		ECONOMY
	}

	public enum Status {
		AVAILABLE,
		RESERVED,
		BOOKED
	}
}