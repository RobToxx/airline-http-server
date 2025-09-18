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

import auth.RegisterResult;
import auth.Session;
import service.AuthService;
import util.LocalDateTimeAdapter;
import util.Result;

public class AuthController {

	private AuthService authService;
    private Gson gson;

	public AuthController(AuthService authService) {

		this.authService = authService;
        this.gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .setPrettyPrinting()
            .create();
	}

	public void registerRoutes(HttpServer server) {

        server.createContext("/user/login", this::handleLogin);
        server.createContext("/user/logout", this::handleLogout);
        server.createContext("/user/register", this::handleRegister);
    }

    private void handleLogin(HttpExchange exchange) {

        addCorsHeaders(exchange);

        Result<Void> result = processLogin(exchange);
        
        if (result instanceof Result.Failure failure) {

            System.err.println("Error handling request /user/login: " + failure.exception().getMessage());

        } else {

        	System.out.println("Request /user/login: "+exchange.getRemoteAddress()+": ");
        }
    }

    private Result<Void> processLogin(HttpExchange exchange) {

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
        
        Map<String, String> loginRequest = this.gson.fromJson(
            requestBody, 
            new TypeToken<Map<String, String>> () {}.getType()
        );

        Result<Optional<Session>> loginResult = this.authService.login(
            loginRequest.get("email"), 
            loginRequest.get("password")
        );

        if (loginResult instanceof Result.Failure f) {

            return sendInternalServerError(exchange, f.exception());
        }

        Optional<Session> sessionOpt = loginResult.expect();

        if (sessionOpt.isEmpty()) {

             return sendResponse(
                exchange, 
                401, 
                "Invalid email or password"
            );
        }

        Session session = sessionOpt.get();

        exchange.getResponseHeaders().add(
            "Set-Cookie", 
            String.format(
                "SESSION_ID=%s; HttpOnly; Path=/; Max-Age=%d",
                session.id(),
                2
            )
        );

        return sendResponse(
            exchange, 
            200, 
            this.gson.toJson(session)
        );
    }

    private void handleLogout(HttpExchange exchange) {

        addCorsHeaders(exchange);

        Result<Void> result = processLogout(exchange);
        
        if (result instanceof Result.Failure failure) {

            System.err.println("Error handling request /user/logout: " + failure.exception().getMessage());

        } else {

            System.out.println("Request /user/logout: "+exchange.getRemoteAddress()+": ");
        }
    }

    private Result<Void> processLogout(HttpExchange exchange) {

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
        
        Map<String, String> logoutRequest = this.gson.fromJson(
            requestBody, 
            new TypeToken<Map<String, String>> () {}.getType()
        );

        Result<Boolean> logoutResult = this.authService.logout(
            logoutRequest.get("sessionId")
        );

        if (logoutResult instanceof Result.Failure f) {

            return sendInternalServerError(exchange, f.exception());
        }

        if (!logoutResult.expect()) {

             return sendResponse(
                exchange, 
                404, 
                "Session not found"
            );
        }

        exchange.getResponseHeaders().add(
            "Set-Cookie", 
            String.format(
                "SESSION_ID=%s; HttpOnly; Path=/; Max-Age=%d",
                logoutRequest.get("sessionId"),
                0
            )
        );

        return sendResponse(
            exchange, 
            200, 
            "{}"
        );
    }

    private void handleRegister(HttpExchange exchange) {

        addCorsHeaders(exchange);

        Result<Void> result = processRegister(exchange);
        
        if (result instanceof Result.Failure failure) {

            System.err.println("Error handling request /user/register: " + failure.exception().getMessage());

        } else {

            System.out.println("Request /user/register: "+exchange.getRemoteAddress()+": ");
        }
    }

    private Result<Void> processRegister(HttpExchange exchange) {

        if (!"POST".equals(exchange.getRequestMethod())) {

            System.out.println("Test1");

            return sendResponse(
                exchange,
                405, 
                "Method Not Allowed"
            );
        }

        Scanner sc = new Scanner(exchange.getRequestBody()).useDelimiter("\\A");
        
        String requestBody = sc.hasNext() ? sc.next() : "";

        sc.close();

        Map<String, String> registerRequest = this.gson.fromJson(
            requestBody, 
            new TypeToken<Map<String, String>> () {}.getType()
        );

        System.out.println(1);

        Result<RegisterResult> result = this.authService.register(
            registerRequest.get("name"), 
            registerRequest.get("email"), 
            registerRequest.get("password")
        );

        System.out.println(2);

        if (result instanceof Result.Failure f) {

            return sendInternalServerError(exchange, f.exception());
        }

        RegisterResult registerResult = result.expect();

        if (!registerResult.equals(RegisterResult.SUCCESS)) {

             return sendResponse(
                exchange, 
                400, 
                "Invalid register parameters: "+registerResult.name()
            );
        }

        System.out.println(1);

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