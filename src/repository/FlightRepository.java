package repository;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import data.DatabaseConnection;
import model.Flight;
import model.FlightFilter;
import util.Result;

public class FlightRepository {

    private final DatabaseConnection database;

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

    private String removeAccents(String text) {
        return Normalizer.normalize(text, Normalizer.Form.NFD)
                     .replaceAll("\\p{M}", "");
    }

    public Result<Optional<List<Flight>>> findFlights(FlightFilter filter) {

        StringBuilder sql = new StringBuilder("SELECT * FROM flights WHERE 1=1");
        List<Object> params = new ArrayList<>();

        filter.origin().ifPresent(o -> {
            sql.append(" AND unaccent(lower(origin)) LIKE ?");
            params.add("%" + removeAccents(o.toLowerCase()) + "%");
        });

        filter.destination().ifPresent(d -> {
            sql.append(" AND unaccent(lower(destination)) LIKE ?");
            params.add("%" + removeAccents(d.toLowerCase()) + "%");
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
}
