package ma.hotel.user.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;

public class UsersHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        // Vérifier la méthode HTTP
        if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            exchange.sendResponseHeaders(405, -1);
            exchange.close();
            return;
        }

        // Préparer la réponse JSON
        String response = """
                [
                    {
                        "id": 1,
                        "nom": "Badr",
                        "email": "badr@example.com"
                    },
                    {
                        "id": 2,
                        "nom": "Ahmed",
                        "email": "ahmed@example.com"
                    }
                ]
                """;

        // Définir le type de contenu
        exchange.getResponseHeaders()
                .set("Content-Type", "application/json; charset=UTF-8");

        // Envoyer le code HTTP 200 et la taille de la réponse
        byte[] responseBytes = response.getBytes("UTF-8");
        exchange.sendResponseHeaders(200, responseBytes.length);

        // Envoyer le contenu au client
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(responseBytes);
        }
    }
}