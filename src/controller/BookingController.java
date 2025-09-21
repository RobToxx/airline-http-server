package controller;

import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import model.Booking;
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
                "Unabe to complete reservation"
            );
        }

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

            return null;
        });
    }

    private void addCorsHeaders(HttpExchange exchange) {

        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
    }
}