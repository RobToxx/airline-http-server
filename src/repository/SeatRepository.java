package repository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import data.DatabaseConnection;
import model.Booking;
import model.Reservation;
import model.Seat;
import util.Result;

public class SeatRepository {
	
	private final DatabaseConnection database;

	public SeatRepository(DatabaseConnection database) {

        this.database = database;
    }

    public Result<Void> book(Booking booking) {

    	String sql = """
    		INSERT INTO bookings
    		VALUES (DEFAULT, ?, ?, ?, ?, ?, ?)
    	""";

    	return this.database.modify(
    		sql, 
    		statement -> {
    			statement.setInt(1, booking.userId());
    			statement.setInt(2, booking.flightid());
    			statement.setInt(3, booking.airplaneId());
    			statement.setString(4, booking.seatId());
    			statement.setTimestamp(5, Timestamp.valueOf(booking.purchaseDate()));
    			statement.setBigDecimal(6, booking.price());
    		}
    	).andThen(value -> null);
    }

    public Result<Void> reserve(Reservation reservation) {

    	String sql = """
    		INSERT INTO reservations
    		VALUES (?, ?, ?, ?, ?, ?)
    	""";

    	return this.database.modify(
    		sql, 
    		statement -> {
                statement.setString(1, reservation.id());
    			statement.setInt(2, reservation.userId());
    			statement.setInt(3, reservation.flightid());
    			statement.setInt(4, reservation.airplaneId());
    			statement.setString(5, reservation.seatId());
    			statement.setTimestamp(6, Timestamp.valueOf(reservation.expirationDate()));
    		}
    	).andThen(value -> null);
    }

    public Result<Boolean> unreserve(String reservationId) {

    	String sql = """
    		DELETE FROM reservations
    		WHERE id = ?
    	""";

    	return this.database.modify(
    		sql, 
    		statement -> {
    			statement.setString(1, reservationId);
    		}
    	);
    }

    public Result<Optional<Integer>> getReservationOwnerId(int flightId, String seatId) {

        String sql = """
            SELECT users.id
            FROM users
            JOIN reservations 
                ON users.id = reservations.user_id
            WHERE flight_id = ?
                AND seat_id = ?
        """;

        return database.query(
            sql, 
            statement -> {
                statement.setInt(1, flightId);
                statement.setString(2, seatId);
            }, 
            resultSet -> resultSet.getInt(1)
        );
    }

    public Result<Optional<List<Seat>>> getSeatsForFlight(int id) {

        String sql =  """
            SELECT
                seats.id,
                seats.class,
                CASE
                    WHEN bookings.seat_id IS NOT NULL THEN 'BOOKED'
                    WHEN reservations.seat_id IS NOT NULL THEN 'RESERVED'
                    ELSE 'AVAILABLE'
                END AS status
            FROM seats
            JOIN flights 
                ON flights.airplane_id = seats.airplane_id
            LEFT JOIN bookings 
                ON bookings.flight_id = flights.id 
                AND bookings.seat_id = seats.id
            LEFT JOIN reservations
                ON reservations.flight_id = flights.id
                AND reservations.seat_id = seats.id
            WHERE flights.id = ?
            ORDER BY seats.id
        """;

        return database.query(
            sql, 
            statement -> {
                statement.setInt(1, id);
            }, 
            resultSet -> {
                List<Seat> seats = new ArrayList<>();

                do {
                    seats.add(new Seat(
                        resultSet.getString("id"),
                        Seat.Class.valueOf(resultSet.getString("class")),
                        Seat.Status.valueOf(resultSet.getString("status"))
                    ));
                } while (resultSet.next());
                
                return seats;
            }
        );
    }
}