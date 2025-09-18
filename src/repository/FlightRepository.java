package repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import data.DatabaseConnection;
import model.Flight;
import model.Seat;
import model.FlightFilter;
import util.Result;

public class FlightRepository {

    protected final DatabaseConnection database;

    public FlightRepository(DatabaseConnection database) {

        this.database = database;
    }

    public Result<Optional<Flight>> getFlight(int id) {

        String sql =  """
            SELECT *
            FROM flights
            WHERE id = ?
        """;

        return database.query(
            sql, 
            statement -> statement.setInt(1, id), 
            resultSet -> {
                return new Flight(
                    resultSet.getInt("id"),
                    resultSet.getString("origin"),
                    resultSet.getString("destination"),
                    resultSet.getTimestamp("departure").toLocalDateTime(),
                    resultSet.getInt("airplane_id")
                );
            }
        );
    }

    public Result<Optional<List<Flight>>> findFlights(FlightFilter filter) {

        StringBuilder sql = new StringBuilder("SELECT * FROM flights WHERE 1=1");
        List<Object> params = new ArrayList<>();

        filter.origin().ifPresent(o -> {
            sql.append(" AND origin=?");
            params.add(o);
        });

        filter.destination().ifPresent(d -> {
            sql.append(" AND destination=?");
            params.add(d);
        });

        filter.fromDate().ifPresent(fd -> {
            sql.append(" AND departure >= ?");
            params.add(java.sql.Timestamp.valueOf(fd));
        });

        filter.toDate().ifPresent(td -> {
            sql.append(" AND departure <= ?");
            params.add(java.sql.Timestamp.valueOf(td));
        });

        return database.query(
            sql.toString(),
            statement -> {
                for (int i = 0; i < params.size(); i++) {
                    statement.setObject(i + 1, params.get(i));
                }
            },
            resultSet -> {
                List<Flight> flights = new ArrayList<>();

                do {
                    flights.add(new Flight(
                        resultSet.getInt("id"),
                        resultSet.getString("origin"),
                        resultSet.getString("destination"),
                        resultSet.getTimestamp("departure").toLocalDateTime(),
                        resultSet.getInt("airplane_id")
                    ));
                } while (resultSet.next());

                return flights;
            }
        );
    }

    public Result<Optional<List<Seat>>> getSeatsForFlight(int id) {

        String sql =  """
            SELECT
                seats.id,
                seats.class,
                CASE
                    WHEN tickets.seat_id IS NOT NULL THEN 'BOOKED'
                    WHEN reservations.seat_id IS NOT NULL THEN 'RESERVED'
                    ELSE 'AVAILABLE'
                END AS status
            FROM seats
            JOIN flights 
                ON flights.airplane_id = seats.airplane_id
            LEFT JOIN tickets 
                ON tickets.flight_id = flights.id 
                AND tickets.seat_id = seats.id
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
