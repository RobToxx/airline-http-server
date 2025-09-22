package generators;

import model.Flight;
import model.Seat;

import java.sql.Timestamp;
import java.util.List;

import data.DatabaseConnection;

public class GenerationRepository {

    private final DatabaseConnection database;

    public GenerationRepository(DatabaseConnection database) {
        
        this.database = database;
    }

    public void removeAll() {

        String sql = "TRUNCATE flights, seats, airplanes RESTART IDENTITY CASCADE;";

        this.database.modify(
            sql,
            s -> {}
        ).expect();
    }

    public void addFlights(List<Flight> flights) {

        StringBuilder sql = new StringBuilder("INSERT INTO flights VALUES");

        for (int i = 0; i < flights.size(); i++) {

            sql.append("(DEFAULT, ?, ?, ?, ?),");
        }

        sql.deleteCharAt(sql.length() - 1);

        this.database.modify(
            sql.toString(), 
            statement -> {
                for (int i = 0; i < flights.size(); i++) {

                    int x = i*4;

                    statement.setInt(x+1, flights.get(i).airplaneId());
                    statement.setString(x+2, flights.get(i).origin());
                    statement.setString(x+3, flights.get(i).destination());
                    statement.setTimestamp(x+4, Timestamp.valueOf(flights.get(i).departure()));
                }
            }
        ).expect();
    }

    public void addAirplanes(List<Airplane> airplanes) {

        StringBuilder sql = new StringBuilder("INSERT INTO airplanes VALUES");

        for (int i = 0; i < airplanes.size(); i++) {

            sql.append("(DEFAULT, ?),");
        }

        sql.deleteCharAt(sql.length() - 1);

        this.database.modify(
            sql.toString(), 
            statement -> {
                for (int i = 0; i < airplanes.size(); i++) {

                    statement.setString(i+1, airplanes.get(i).model());
                }
            }
        ).expect();
    }

    public void addSeats(int airplaneId, List<Seat> seats) {

        StringBuilder sql = new StringBuilder("INSERT INTO seats VALUES");

        for (int i = 0; i < seats.size(); i++) {

            sql.append("(?, ?, ?),");
        }

        sql.deleteCharAt(sql.length() - 1);

        this.database.modify(
            sql.toString(), 
            statement -> {
                for (int i = 0; i < seats.size(); i++) {

                    int x = i*3;

                    statement.setInt(x+1, airplaneId);
                    statement.setString(x+2, seats.get(i).id());
                    statement.setString(x+3, seats.get(i).seatClass().toString());
                }
            }
        ).expect();
    }
}
