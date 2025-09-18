package generators;

import java.util.ArrayList;
import java.util.List;

import model.Seat;

public class SeatGenerator {

	public static List<Seat> generate(int rows, int cols, int firstClassRowStart) {
        List<Seat> seats = new ArrayList<>();

        for (int row = 1; row <= rows; row++) {
            for (int col = 0; col < cols; col++) {
                char seatLetter = (char) ('A' + col);

                Seat.Class seatClass = (row < firstClassRowStart)
                        ? Seat.Class.ECONOMY
                        : Seat.Class.FIRST;

                seats.add(
                    new Seat(
                        row + String.valueOf(seatLetter),
                        seatClass,
                        Seat.Status.AVAILABLE
                    )
                );
            }
        }

        return seats;
    }
}