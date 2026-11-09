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
import java.util.List;
import ma.hotel.user.dto.UpdateUserRequest;
import com.fasterxml.jackson.databind.SerializationFeature;
import ma.hotel.user.dto.ReactivateUserRequest;
public class UsersHandler implements HttpHandler{

    private final ObjectMapper objectMapper = new ObjectMapper()
    .registerModule(new JavaTimeModule())
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    //private final UserService userService = new UserService();

    private final UserService userService;

    // Constructeur utilisé par l'application
    public UsersHandler() { // Il permet de conserver le fonctionnement actuel de ton application.
        this(new UserService());
    }

    // Constructeur utilisé pour injecter une dépendance
    public UsersHandler(UserService userService) { // Il nous permettra d'écrire dans les tests
        this.userService = userService;
    }


    @Override 
    public void handle(HttpExchange exchange) throws IOException{
        System.out.println(">>> UsersHandler a reçu une requête !");
        System.out.println("Méthode : " + exchange.getRequestMethod());
System.out.println("URI : " + exchange.getRequestURI());
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        switch(method){
            case "POST":
                if (path.equals("/users/register")) {

                    // Traitement de l'inscription
                    handleRegisterUser(exchange);
                } else if (path.equals("/users/reactivate")) {

                    // Traitement de la réactivation
                    handleReactivateUser(exchange);

                } else {

                    sendError(exchange, 404, "Route introuvable");

                }
        
            break;
        case "GET":
            if (path.matches("/users/\\d+")) {
                handleGetUserById(exchange, path);
            } else if (path.equals("/users")) {
                handleGetUsers(exchange);
            } else {
                sendError(exchange, 404, "Route introuvable");
            }
            break;
        case "PUT":
            if(path.matches("/users/\\d+")){
                handleUpdateUser(exchange, path);
            }else{
                sendError(exchange, 404, "Route introuvable");
            }
            break;
        case "DELETE":
            if (path.matches("/users/\\d+")) {
                handleDeleteUser(exchange, path);
            } else {
                sendError(exchange, 404, "Route introuvable");
            }
            break;
        default:
            exchange.sendResponseHeaders(405, -1);
            exchange.close();
            break;
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

    private void handleRegisterUser(HttpExchange exchange)
        throws IOException {
        // Ton code actuel d'inscription
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
    private void handleGetUsers(HttpExchange exchange) throws IOException {

    try {
        System.out.println("1. Début récupération des utilisateurs");

        List<UserResponse> users = userService.getAllUsers();

        System.out.println("2. Utilisateurs récupérés : " + users.size());

        String jsonResponse = objectMapper.writeValueAsString(users);

        System.out.println("3. Conversion JSON terminée");

        byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set(
            "Content-Type",
            "application/json; charset=UTF-8"
        );

        exchange.sendResponseHeaders(200, responseBytes.length);

        System.out.println("4. Envoi de la réponse HTTP");

        try (OutputStream output = exchange.getResponseBody()) {
            output.write(responseBytes);
        }

        System.out.println("5. Réponse envoyée avec succès");

    } catch (Exception e) {
        e.printStackTrace();

        sendError(exchange, 500, "Erreur lors de la récupération des utilisateurs");
    }
}

    private void handleGetUserById(HttpExchange exchange, String path)
        throws IOException {
        String[] parts = path.split("/");
        String userId = parts[parts.length - 1];

        Long id = Long.parseLong(userId);

        UserResponse userResponse = userService.getUserById(id);

        if (userResponse == null) {
            sendError(exchange, 404, "Utilisateur non trouvé");
            return;
        }

        byte[] responseBytes = objectMapper.writeValueAsBytes(userResponse);
        exchange.getResponseHeaders().set(
            "Content-Type",
            "application/json; charset=UTF-8"
        );
        exchange.sendResponseHeaders(200, responseBytes.length);

        try (OutputStream output = exchange.getResponseBody()) {
            output.write(responseBytes);
        }
        exchange.close();
    }

    private void handleUpdateUser(HttpExchange exchange, String path)
        throws IOException {

        try {
            // 1. Récupérer l'ID depuis l'URL
            String[] parts = path.split("/");
            String userId = parts[parts.length - 1];
            Long id = Long.parseLong(userId);

            // 2. Lire le JSON et le convertir en UpdateUserRequest
            try (InputStream input = exchange.getRequestBody()) {

                UpdateUserRequest request =
                    objectMapper.readValue(input, UpdateUserRequest.class);

                // 3. Appeler le service pour modifier l'utilisateur
                UserResponse response =
                    userService.updateUser(id, request);

                // 4. Convertir UserResponse en JSON
                byte[] responseBytes =
                    objectMapper.writeValueAsBytes(response);

                // 5. Définir le Content-Type
                exchange.getResponseHeaders().set(
                    "Content-Type",
                    "application/json; charset=UTF-8"
                );

                // 6. Envoyer le statut HTTP 200
                exchange.sendResponseHeaders(200, responseBytes.length);

                // 7. Envoyer le JSON au client
                try (OutputStream output = exchange.getResponseBody()) {
                    output.write(responseBytes);
                }
            }

            exchange.close();

        } catch (JsonProcessingException e) {

            sendError(exchange, 400, "JSON invalide");

        } catch (IllegalArgumentException e) {

            if ("Utilisateur non trouvé.".equals(e.getMessage())) {
                sendError(exchange, 404, e.getMessage());
            } else {
                sendError(exchange, 400, e.getMessage());
            }

        } catch (Exception e) {

            e.printStackTrace();
            sendError(exchange, 500, "Erreur interne du serveur");
        }
    }

    private void handleDeleteUser(HttpExchange exchange, String path)
        throws IOException {

        try {
            String[] parts = path.split("/");
            String userId = parts[parts.length - 1];

            Long id = Long.parseLong(userId);

            userService.deactivateUser(id);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Compte désactivé avec succès.");

            byte[] responseBytes = objectMapper.writeValueAsBytes(response);

            exchange.getResponseHeaders().set(
                "Content-Type",
                "application/json; charset=UTF-8"
            );

            exchange.sendResponseHeaders(200, responseBytes.length);

            try (OutputStream output = exchange.getResponseBody()) {
                output.write(responseBytes);
            }

            exchange.close();

        } catch (IllegalArgumentException e) {

            if ("Utilisateur non trouvé.".equals(e.getMessage())) {
                sendError(exchange, 404, e.getMessage());
            } else {
                sendError(exchange, 400, e.getMessage());
            }

        } catch (Exception e) {
            e.printStackTrace();
            sendError(exchange, 500, "Erreur interne du serveur");
        }
    }

    private void handleReactivateUser(HttpExchange exchange)
        throws IOException {

    try (InputStream input = exchange.getRequestBody()) {

        ReactivateUserRequest request =
            objectMapper.readValue(input, ReactivateUserRequest.class);

        userService.activateUser(
            request.getEmail(),
            request.getPassword()
        );

        Map<String, String> response = new HashMap<>();
        response.put("message", "Compte réactivé avec succès.");

        byte[] responseBytes =
            objectMapper.writeValueAsBytes(response);

        exchange.getResponseHeaders().set(
            "Content-Type",
            "application/json; charset=UTF-8"
        );

        exchange.sendResponseHeaders(200, responseBytes.length);

        try (OutputStream output = exchange.getResponseBody()) {
            output.write(responseBytes);
        }

    } catch (JsonProcessingException e) {

        sendError(exchange, 400, "JSON invalide");

    } catch (IllegalArgumentException e) {

        sendError(exchange, 400, e.getMessage());

    } catch (Exception e) {

        e.printStackTrace();
        sendError(exchange, 500, "Erreur interne du serveur");
    }

    exchange.close();
}
}