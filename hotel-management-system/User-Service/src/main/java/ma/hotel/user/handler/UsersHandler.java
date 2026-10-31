package ma.hotel.user.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import ma.hotel.user.dto.RegisterUserRequest;
import ma.hotel.user.model.User;
import ma.hotel.user.service.UserService;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class UsersHandler implements HttpHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final UserService userService = new UserService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        try {

            if (path.equals("/users/register") && method.equalsIgnoreCase("POST")) {

                registerUser(exchange);

            } else {

                sendJson(exchange, 404, Map.of(
                        "error", "Route introuvable"
                ));
            }

        } catch (IllegalArgumentException e) {

            sendJson(exchange, 400, Map.of(
                    "error", e.getMessage()
            ));

        } catch (Exception e) {

            e.printStackTrace();

            sendJson(exchange, 500, Map.of(
                    "error", "Erreur interne du serveur"
            ));
        }
    }

    private void registerUser(HttpExchange exchange) throws IOException {

        // Lire le corps JSON
        RegisterUserRequest request = objectMapper.readValue(
                exchange.getRequestBody(),
                RegisterUserRequest.class
        );

        // Appeler le service métier
        User user = userService.registerUser(
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                request.getPassword(),
                request.getPhone()
        );

        // Ne jamais retourner le mot de passe
        Map<String, Object> response = Map.of(
                "id", user.getId(),
                "firstName", user.getFirstName(),
                "lastName", user.getLastName(),
                "email", user.getEmail(),
                "phone", user.getPhone(),
                "role", user.getRole(),
                "status", user.getStatus(),
                "createdAt", user.getCreatedAt().toString()
        );

        sendJson(exchange, 201, response);
    }

    private void sendJson(
            HttpExchange exchange,
            int statusCode,
            Object data
    ) throws IOException {

        byte[] responseBytes = objectMapper.writeValueAsBytes(data);

        exchange.getResponseHeaders().set(
                "Content-Type",
                "application/json; charset=UTF-8"
        );

        exchange.sendResponseHeaders(statusCode, responseBytes.length);

        try (OutputStream output = exchange.getResponseBody()) {
            output.write(responseBytes);
        }
    }
}