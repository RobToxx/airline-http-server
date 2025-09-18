package controller;

import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import model.Flight;
import model.FlightDetails;
import service.FlightService;
import util.LocalDateTimeAdapter;
import util.Result;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class FlightController {

    private FlightService flightService;
    private Gson gson;

    public FlightController(FlightService flightService) {

        this.flightService = flightService;
        this.gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .setPrettyPrinting()
            .create();
    }

    public void registerRoutes(HttpServer server) {

        server.createContext("/flight/search", this::handleSearch);
        server.createContext("/flight", this::handleFlight);
    }

    private void handleSearch(HttpExchange exchange) {

        addCorsHeaders(exchange);

        Result<Void> result = processSearch(exchange);
        
        if (result instanceof Result.Failure failure) {

            System.err.println("Error handling request /flight/search: " + failure.exception().getMessage());
        }
    }

    private void handleFlight(HttpExchange exchange) {
        
        addCorsHeaders(exchange);

        Result<Void> result = processFlight(exchange);

        if (result instanceof Result.Failure failure) {

            System.err.println("Error handling request /flight: " + failure.exception().getMessage());
        }
    }

    private Result<Void> processSearch(HttpExchange exchange) {

        if (!"GET".equals(exchange.getRequestMethod())) {

            return sendResponse(exchange, 405, "Method Not Allowed");
        }

        System.out.println("Request /flight/search: "+exchange.getRemoteAddress()+": ");

        Result<Map<String, String>> paramsResult = parseQuery(exchange.getRequestURI().getQuery());

        if (paramsResult instanceof Result.Failure) {

            return sendResponse(exchange, 405, "Invalid Query Structure");
        }

        Result<List<Flight>> result = flightService.search(paramsResult.expect());

        switch (result) {
            case Result.Success<List<Flight>> s -> {

                return sendResponse(
                    exchange, 
                    200, 
                    this.gson.toJson(s.value())
                );
            }
            case Result.Failure<List<Flight>> f -> {
                return sendInternalServerError(exchange, f.exception());
            }
        }
    }

    private Result<Void> processFlight(HttpExchange exchange) {

        if (!"GET".equals(exchange.getRequestMethod())) {

            return sendResponse(exchange, 405, "Method Not Allowed");
        }

        System.out.println("Request /flight: "+exchange.getRemoteAddress().toString()+": ");

        Result<Map<String, String>> paramsResult = parseQuery(exchange.getRequestURI().getQuery());

        if (paramsResult instanceof Result.Failure) {

            return sendResponse(exchange, 400, "Invalid Query Structure");
        }

        if (!paramsResult.expect().containsKey("id")) {

            return sendResponse(exchange, 400, "Missing flight id");
        }

        int id = Integer.parseInt(paramsResult.expect().get("id"));

        Result<Optional<FlightDetails>> result = flightService.getFlightDetails(id);

        if (result instanceof Result.Failure f) {

            return sendInternalServerError(exchange, f.exception());
        }

        Optional<FlightDetails> flightDetails = result.expect();

        if (flightDetails.isEmpty()) {

            return sendResponse(
                exchange, 
                404, 
                "Flight not found"
            );
        }

        return sendResponse(
            exchange, 
            200, 
            this.gson.toJson(flightDetails.get())
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
