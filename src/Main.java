import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.sun.net.httpserver.HttpServer;

import data.DatabaseConnection;
import data.PostgreSQLConnection;
import repository.FlightRepository;
import repository.SeatRepository;
import repository.SessionRepository;
import repository.UserDAO;
import service.AuthService;
import service.BookingService;
import service.CacheCleaner;
import service.FlightService;
import controller.AuthController;
import controller.BookingController;
import controller.FlightController;

import generators.*;
import model.Flight;
import model.Seat;
import util.Result;

public class Main {

    public static void main(String[] args) {
        
        while (true) {
            try {
                startServer();

            } catch (Exception e) {

                System.err.println("Server crashed: " + e.getMessage());
                e.printStackTrace();

                try {
                    Thread.sleep(5000);

                } catch (InterruptedException ie) {

                    Thread.currentThread().interrupt();
                    break;
                }

                System.out.println("Restarting server...");
            }
        }
    }
    
    public static void startServer() {

        DatabaseConnection database = PostgreSQLConnection.create(
            "jdbc:postgresql://192.168.56.101:5432/", 
            "postgres", 
            "Admin1234$"
        ).expect("Unable to connect to the database.");

        /*
        GenerationRepository genRepo = new GenerationRepository(database);

        genRepo.removeAll();

        List<Airplane> airplanes = AirplanesGenerator.generate(20);
        List<Flight> flights = FlightsGenerator.generate(250, airplanes);
        List<Seat> seats = SeatGenerator.generate(14, 6, 11);

        genRepo.addAirplanes(airplanes);
        genRepo.addFlights(flights);
        for (Airplane airplane : airplanes) genRepo.addSeats(airplane.id(), seats);
        */

        FlightRepository flightRepository = new FlightRepository(database);
        SessionRepository sessionRepository = new SessionRepository(database);
        SeatRepository seatRepository = new SeatRepository(database);


        FlightService flightService = new FlightService(flightRepository, seatRepository);
        AuthService authService = new AuthService(new UserDAO(database), sessionRepository);
        BookingService bookingService = new BookingService(seatRepository, authService, flightService);

        CacheCleaner cleaner = new CacheCleaner(database);

        FlightController flightController = new FlightController(flightService);
        AuthController authController = new AuthController(authService);
        BookingController bookingController = new BookingController(bookingService);

        HttpServer server = Result.of(()->{
            return HttpServer.create(new InetSocketAddress(8000), 0);
        }).expect("Unable to start the server.");

        // Rutas
        authController.registerRoutes(server);
        flightController.registerRoutes(server);
        bookingController.registerRoutes(server);

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        // Ejecuta clean() cada 5 minutos
        scheduler.scheduleAtFixedRate(
            cleaner::clean,
            0,
            10,
            TimeUnit.SECONDS
        );

        server.setExecutor(null);
        server.start();
        
        System.out.println("Listening in 8000");

        try {
            Thread.currentThread().join();
        } catch (InterruptedException ignored) {}
    }
}
