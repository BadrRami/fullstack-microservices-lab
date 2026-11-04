// package ma.hotel.user.handler;

// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.sun.net.httpserver.HttpExchange;
// import com.sun.net.httpserver.HttpHandler;

// import ma.hotel.user.dto.RegisterUserRequest;
// import ma.hotel.user.model.User;
// import ma.hotel.user.service.UserService;

// import java.io.IOException;
// import java.io.OutputStream;
// import java.nio.charset.StandardCharsets;
// import java.util.Map;

// public class UsersHandler implements HttpHandler {

//     private final ObjectMapper objectMapper = new ObjectMapper();
//     private final UserService userService = new UserService();

//     @Override
//     public void handle(HttpExchange exchange) throws IOException {

//         String path = exchange.getRequestURI().getPath();
//         String method = exchange.getRequestMethod();

//         try {

//             if (path.equals("/users/register") && method.equalsIgnoreCase("POST")) {

//                 registerUser(exchange);

//             } else {

//                 sendJson(exchange, 404, Map.of(
//                         "error", "Route introuvable"
//                 ));
//             }

//         } catch (IllegalArgumentException e) {

//             sendJson(exchange, 400, Map.of(
//                     "error", e.getMessage()
//             ));

//         } catch (Exception e) {

//             e.printStackTrace();

//             sendJson(exchange, 500, Map.of(
//                     "error", "Erreur interne du serveur"
//             ));
//         }
//     }

//     private void registerUser(HttpExchange exchange) throws IOException {

//         // Lire le corps JSON
//         RegisterUserRequest request = objectMapper.readValue(
//                 exchange.getRequestBody(),
//                 RegisterUserRequest.class
//         );

//         // Appeler le service métier
//         User user = userService.registerUser(
//                 request.getFirstName(),
//                 request.getLastName(),
//                 request.getEmail(),
//                 request.getPassword(),
//                 request.getPhone()
//         );

//         // Ne jamais retourner le mot de passe
//         Map<String, Object> response = Map.of(
//                 "id", user.getId(),
//                 "firstName", user.getFirstName(),
//                 "lastName", user.getLastName(),
//                 "email", user.getEmail(),
//                 "phone", user.getPhone(),
//                 "role", user.getRole(),
//                 "status", user.getStatus(),
//                 "createdAt", user.getCreatedAt().toString()
//         );

//         sendJson(exchange, 201, response);
//     }

//     private void sendJson(
//             HttpExchange exchange,
//             int statusCode,
//             Object data
//     ) throws IOException {

//         byte[] responseBytes = objectMapper.writeValueAsBytes(data);

//         exchange.getResponseHeaders().set(
//                 "Content-Type",
//                 "application/json; charset=UTF-8"
//         );

//         exchange.sendResponseHeaders(statusCode, responseBytes.length);

//         try (OutputStream output = exchange.getResponseBody()) {
//             output.write(responseBytes);
//         }
//     }
// }


package ma.hotel.user.handler;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ma.hotel.user.dto.RegisterUserRequest;
import ma.hotel.user.dto.UserResponse;
import ma.hotel.user.model.User;
import ma.hotel.user.service.UserService;
import java.util.Map;
import java.util.HashMap;
import com.fasterxml.jackson.databind.SerializationFeature;
public class UsersHandler implements HttpHandler{

    private final ObjectMapper objectMapper = new ObjectMapper()
    .registerModule(new JavaTimeModule())
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    private final UserService userService = new UserService();
    @Override 
    public void handle(HttpExchange exchange) throws IOException{
         System.out.println(">>> UsersHandler a reçu une requête !");
        String method = exchange.getRequestMethod();
        if(!method.equalsIgnoreCase("POST")){
            exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            exchange.close();
            return; // arrête immédiatement l'exécution de la méthode handle().
        }

        try (InputStream input = exchange.getRequestBody()) {

            System.out.println("1. Requête reçue");

            RegisterUserRequest request =
                objectMapper.readValue(input, RegisterUserRequest.class);

            System.out.println("2. JSON converti en objet");

            User user = userService.registerUser(
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                request.getPassword(),
                request.getPhone()
            );

            System.out.println("3. Utilisateur créé");

            UserResponse response = userService.toUserResponse(user);

            System.out.println("4. Conversion UserResponse terminée");

            String jsonResponse = objectMapper.writeValueAsString(response);

            System.out.println("5. Conversion JSON terminée");

            byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);

            exchange.getResponseHeaders().set(
                "Content-Type",
                "application/json; charset=UTF-8"
            );

            exchange.sendResponseHeaders(201, responseBytes.length);

            try (OutputStream output = exchange.getResponseBody()) {
                output.write(responseBytes);
            }

            exchange.close();

        }catch (JsonProcessingException e) {

            sendError(exchange, 400, "JSON invalide");

        } catch (IllegalArgumentException e) {

            sendError(exchange, 400, e.getMessage());

        } catch (Exception e) {

            e.printStackTrace();
            sendError(exchange, 500, "Erreur interne du serveur");
        }

        
    }

    // 3. Méthode sendError()
    private void sendError(
        HttpExchange exchange,
        int statusCode,
        String message
    ) throws IOException {

        Map<String, String> error = new HashMap<>();
        error.put("error", message);

        byte[] responseBytes =
            objectMapper.writeValueAsBytes(error);

        exchange.getResponseHeaders().set(
            "Content-Type",
            "application/json; charset=UTF-8"
        );

        exchange.sendResponseHeaders(statusCode, responseBytes.length);

        try (OutputStream output = exchange.getResponseBody()) {
            output.write(responseBytes);
        }
        exchange.close();
    }
}