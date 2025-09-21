package service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import auth.User;
import model.Booking;
import model.FlightDetails;
import model.PassengerType;
import model.Reservation;
import model.Seat;
import model.Seat.Status;
import repository.SeatRepository;
import util.Result;

public class BookingService {

    private final SeatRepository seatRepository;
    private final AuthService authService;
    private final FlightService flightService;

    public BookingService(SeatRepository seatRepository, AuthService authService, FlightService flightService) {

        this.seatRepository = seatRepository;
        this.authService = authService;
        this.flightService = flightService;
    }

    public Result<Optional<Booking>> bookSeat(int flightId, String seatId, PassengerType passengerType, String sessionId) {
    	
        return Result.of(() -> {

            Optional<User> userOpt = authService.validateSession(sessionId).orElseThrow();

            if (userOpt.isEmpty()) {

                return Optional.empty();
            }

            User user = userOpt.get();

            Optional<FlightDetails> flightOpt = flightService.getFlightDetails(flightId).orElseThrow();

            if (flightOpt.isEmpty()) {

                return Optional.empty();
            }

            FlightDetails flightDetails = flightOpt.get();

            Optional<Seat> seatOpt = flightDetails.seats().stream()
                .filter(seat -> seat.id().equals(seatId))
                .findAny();

            if (seatOpt.isEmpty()) {

                return Optional.empty();
            }

            Seat seat = seatOpt.get();

            if (seat.status() == Status.BOOKED) {

                return Optional.empty();
            }

            Optional<Integer> ownerIdOpt = seatRepository.getReservationOwnerId(flightId, seatId).orElseThrow();

            if (ownerIdOpt.isPresent() && ownerIdOpt.get() != user.id()) {

                return Optional.empty();
            }

            Booking booking = new Booking(
                user.id(),
                flightId,
                flightDetails.flight().airplaneId(),
                seatId,
                LocalDateTime.now(),
                passengerType,
                seat.seatClass() == Seat.Class.FIRST? new BigDecimal("120000.00") : passengerType.price
            );

            seatRepository.book(booking).throwIfFailure();

            return Optional.of(booking);
        });
    }

    public Result<Optional<Reservation>> reserveSeat(int flightId, String seatId, PassengerType passengerType, String sessionId) {
        
        return Result.of(() -> {

            Optional<User> userOpt = authService.validateSession(sessionId).orElseThrow();

            if (userOpt.isEmpty()) {

                return Optional.empty();
            }

            User user = userOpt.get();

            Optional<FlightDetails> flightOpt = flightService.getFlightDetails(flightId).orElseThrow();

            if (flightOpt.isEmpty()) {

                return Optional.empty();
            }

            FlightDetails flightDetails = flightOpt.get();

            Optional<Seat> seatOpt = flightDetails.seats().stream()
                .filter(seat -> seat.id().equals(seatId))
                .findAny();

            if (seatOpt.isEmpty()) {

                return Optional.empty();
            }

            Seat seat = seatOpt.get();

            if (seat.status() != Status.AVAILABLE) {

                return Optional.empty();
            }

            Reservation reservation = new Reservation(
                UUID.randomUUID().toString(),
                user.id(),
                flightId,
                flightDetails.flight().airplaneId(),
                seatId,
                LocalDateTime.now().minusMinutes(30),
                passengerType,
                seat.seatClass() == Seat.Class.FIRST? new BigDecimal("120000.00") : passengerType.price
            );

            seatRepository.reserve(reservation).throwIfFailure();

            return Optional.of(reservation);
        });
    }

    public Result<Boolean> unreserveSeat(String reservationId) {

        return seatRepository.unreserve(reservationId);
    }
}