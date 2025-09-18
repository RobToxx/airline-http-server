import java.net.InetSocketAddress;
import java.util.List;

import com.sun.net.httpserver.HttpServer;

import data.DatabaseConnection;
import data.PostgreSQLConnection;
import model.Flight;
import model.Seat;
import repository.FlightRepository;
import repository.SessionRepository;
import repository.UserDAO;
import service.AuthService;
import service.FlightService;
import controller.AuthController;
import controller.FlightController;

import generators.*;

import util.Result;

public class Main {
    
    public static void main(String[] args) {

        DatabaseConnection connection = PostgreSQLConnection.create(
            "jdbc:postgresql://192.168.56.101:5432/", 
            "postgres", 
            "Admin1234$"
        ).expect("Unable to connect to the database.");

        /*
        GenerationRepository genRepo = new GenerationRepository(connection);

        genRepo.removeAll();

        List<Airplane> airplanes = AirplanesGenerator.generate(20);
        List<Flight> flights = FlightsGenerator.generate(250, airplanes);
        List<Seat> seats = SeatGenerator.generate(14, 6, 11);

        genRepo.addAirplanes(airplanes);
        genRepo.addFlights(flights);
        for (Airplane airplane : airplanes) genRepo.addSeats(airplane.id(), seats);
        */

        FlightRepository flightRepository = new FlightRepository(connection);
        SessionRepository sessionRepository = new SessionRepository(connection);


        FlightService flightService = new FlightService(flightRepository);
        AuthService authService = new AuthService(new UserDAO(connection), sessionRepository);

        FlightController flightController = new FlightController(flightService);
        AuthController authController = new AuthController(authService);

        HttpServer server = Result.of(()->{
            return HttpServer.create(new InetSocketAddress(8000), 0);
        }).expect("Unable to start the server.");

        // Rutas
        authController.registerRoutes(server);
        flightController.registerRoutes(server);

        server.setExecutor(null);
        server.start();
        
        System.out.println("Listening in 8000");
    }
}
