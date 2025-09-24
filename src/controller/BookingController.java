package controller;

import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import model.Booking;
import model.FlightBooking;
import model.PassengerType;
import model.Reservation;
import service.BookingService;
import util.LocalDateTimeAdapter;
import util.Result;

public class BookingController {

	private BookingService bookingService;
    private Gson gson;

	public BookingController(BookingService bookingService) {

		this.bookingService = bookingService;
        this.gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .setPrettyPrinting()
            .create();
	}

	public void registerRoutes(HttpServer server) {

        server.createContext("/seat/book", this::handleBook);
        server.createContext("/seat/reserve", this::handleReserve);
        server.createContext("/seat/unreserve", this::handleUnreserve);
        server.createContext("/user/books", this::handleUserBooks);
    }

    private void handleBook(HttpExchange exchange) {

        addCorsHeaders(exchange);

        Result<Void> result = processBook(exchange);
        
        if (result instanceof Result.Failure failure) {

            System.err.println("Error handling request /seat/book: " + failure.exception().getMessage());

        } else {

        	System.out.println("Request /seat/book: "+exchange.getRemoteAddress()+": ");
        }
    }

    private Result<Void> processBook(HttpExchange exchange) {

        if (!"POST".equals(exchange.getRequestMethod())) {

            return sendResponse(
                exchange,
                405, 
                "Method Not Allowed"
            );
        }

        Scanner sc = new Scanner(exchange.getRequestBody()).useDelimiter("\\A");
        
        String requestBody = sc.hasNext() ? sc.next() : "";

        sc.close();
        
        Map<String, String> bookRequest = this.gson.fromJson(
            requestBody, 
            new TypeToken<Map<String, String>> () {}.getType()
        );

        Result<Optional<Booking>> result = bookingService.bookSeat(
            Integer.parseInt(bookRequest.get("flightId")), 
            bookRequest.get("seatId"), 
            PassengerType.valueOf(bookRequest.get("passengerType")),
            bookRequest.get("sessionId")
        );

        if (result instanceof Result.Failure f) {

            return sendInternalServerError(exchange, f.exception());
        }

        Optional<Booking> bookingOpt = result.expect();

        if (bookingOpt.isEmpty()) {

            return sendResponse(
                exchange, 
                409, 
                "Unabe to complete booking"
            );
        }

        return sendResponse(
            exchange, 
            200, 
            this.gson.toJson(bookingOpt.get())
        );
    }

    private void handleReserve(HttpExchange exchange) {

        addCorsHeaders(exchange);

        Result<Void> result = processReserve(exchange);
        
        if (result instanceof Result.Failure failure) {

            System.err.println("Error handling request /seat/reserve: " + failure.exception().getMessage());

        } else {

            System.out.println("Request /seat/reserve: "+exchange.getRemoteAddress()+": ");
        }
    }

    private Result<Void> processReserve(HttpExchange exchange) {

        if (!"POST".equals(exchange.getRequestMethod())) {

            return sendResponse(
                exchange,
                405, 
                "Method Not Allowed"
            );
        }

        Scanner sc = new Scanner(exchange.getRequestBody()).useDelimiter("\\A");
        
        String requestBody = sc.hasNext() ? sc.next() : "";

        sc.close();
        
        Map<String, String> reserveRequest = this.gson.fromJson(
            requestBody, 
            new TypeToken<Map<String, String>> () {}.getType()
        );

        System.out.println(reserveRequest);

        Result<Optional<Reservation>> result = bookingService.reserveSeat(
            Integer.parseInt(reserveRequest.get("flightId")), 
            reserveRequest.get("seatId"), 
            PassengerType.valueOf(reserveRequest.get("passengerType")),
            reserveRequest.get("sessionId")
        );

        if (result instanceof Result.Failure f) {

            return sendInternalServerError(exchange, f.exception());
        }

        Optional<Reservation> reserveOpt = result.expect();

        if (reserveOpt.isEmpty()) {

            return sendResponse(
                exchange, 
                409, 
                "Unable to complete reservation"
            );
        }

        System.out.println(reserveOpt);

        return sendResponse(
            exchange, 
            200, 
            this.gson.toJson(reserveOpt.get())
        );
    }

    private void handleUnreserve(HttpExchange exchange) {

        addCorsHeaders(exchange);

        Result<Void> result = processUnreserve(exchange);
        
        if (result instanceof Result.Failure failure) {

            System.err.println("Error handling request /seat/unreserve: " + failure.exception().getMessage());

        } else {

            System.out.println("Request /seat/unreserve: "+exchange.getRemoteAddress()+": ");
        }
    }

    private Result<Void> processUnreserve(HttpExchange exchange) {

        if (!"DELETE".equals(exchange.getRequestMethod())) {

            return sendResponse(
                exchange,
                405, 
                "Method Not Allowed"
            );
        }

        Scanner sc = new Scanner(exchange.getRequestBody()).useDelimiter("\\A");
        
        String requestBody = sc.hasNext() ? sc.next() : "";

        sc.close();
        
        Map<String, String> unreserveRequest = this.gson.fromJson(
            requestBody, 
            new TypeToken<Map<String, String>> () {}.getType()
        );

        Result<Boolean> result = bookingService.unreserveSeat(
            unreserveRequest.get("reservationId")
        );

        if (result instanceof Result.Failure f) {

            return sendInternalServerError(exchange, f.exception());
        }

        if (!result.expect()) {

            return sendResponse(
                exchange, 
                404, 
                "Reservation not found"
            );
        }

        return sendResponse(
            exchange, 
            200, 
            "{}"
        );
    }

    private void handleUserBooks(HttpExchange exchange) {

        addCorsHeaders(exchange);

        Result<Void> result = processUserBooks(exchange);
        
        if (result instanceof Result.Failure failure) {

            System.err.println("Error handling request /user/books: " + failure.exception().getMessage());

        } else {

            System.out.println("Request /user/books: "+exchange.getRemoteAddress()+": ");
        }
    }

    private Result<Void> processUserBooks(HttpExchange exchange) {

        if (!"GET".equals(exchange.getRequestMethod())) {

            return sendResponse(
                exchange,
                405, 
                "Method Not Allowed"
            );
        }

        Result<Map<String, String>> paramsResult = parseQuery(exchange.getRequestURI().getQuery());

        if (paramsResult instanceof Result.Failure) {

            return sendResponse(exchange, 400, "Invalid Query Structure");
        }

        var params = paramsResult.expect();

        if (!params.containsKey("sessionId")) {

            return sendResponse(exchange, 400, "Missing session id");
        }

        Result<Optional<List<FlightBooking>>> result = bookingService.getBookingsOf(
            params.get("sessionId")
        );

        if (result instanceof Result.Failure f) {

            return sendInternalServerError(exchange, f.exception());
        }

        Optional<List<FlightBooking>> flightBookingOpt = result.expect();

        if (flightBookingOpt.isEmpty()) {

            return sendResponse(exchange, 
                401, 
                "Invalid session"
            );
        }

        if (!params.containsKey("flightId")) {

            return sendResponse(
                exchange, 
                200, 
                this.gson.toJson(flightBookingOpt.get())
            );
        }

        Optional<FlightBooking> fOpt = flightBookingOpt.get().stream()
            .filter(f -> f.flight().id() == Integer.parseInt(params.get("flightId")))
            .findAny();

        if (fOpt.isEmpty()) {

            return sendResponse(
                exchange, 
                404, 
                "Reservation not found"
            );
        }

        return sendResponse(
            exchange, 
            200, 
            this.gson.toJson(
                fOpt.get()
            )
        );
    }

    private Result<Map<String, String>> parseQuery(String query) {

        return Result.of(()->{
            Map<String, String> params = new HashMap<>();

            if (query.isBlank()) return params;

            for (String param : query.split("&")) {
                
                String[] kv = param.split("=");
                params.put(kv[0], kv[1]);

            }

            return params;
        });
    }

    private Result<Void> sendInternalServerError(HttpExchange exchange, Exception exception) {

        exception.printStackTrace();

        sendResponse(
            exchange, 
            500, 
            "Internal Server Error: " + exception.getMessage()
        );

        return new Result.Failure<>(exception);
    }

    private Result<Void> sendResponse(HttpExchange exchange, int statusCode, String body) {

        return Result.of(()->{

            byte[] bytes = body.getBytes();

            exchange.sendResponseHeaders(statusCode, bytes.length);

            OutputStream os = exchange.getResponseBody();

            os.write(bytes);

            exchange.getResponseBody().close();

            return null;
        });
    }

    private void addCorsHeaders(HttpExchange exchange) {

        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS, DELETE");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {

            try {
                exchange.sendResponseHeaders(200, -1);
            } catch(Exception e) {}
            exchange.close();
        }
    }
}