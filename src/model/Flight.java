package model;

import java.time.LocalDateTime;

public final record Flight(
	int id,
	String origin,
	String destination,
	LocalDateTime departure,
	int airplaneId
) {
	
}