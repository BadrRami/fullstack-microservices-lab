package ma.hotel.user;
import com.sun.net.httpserver.HttpServer;
import ma.hotel.user.handler.UsersHandler;
import java.io.IOException;
import java.net.InetSocketAddress;
public class Main {
    public static void main(String[] args) throws IOException {
        // Créer le serveur sur le port 8081
        HttpServer server = HttpServer.create(
                new InetSocketAddress(8081), 0
        );
        // Définir la route POST /users
        server.createContext("/users", new UsersHandler());
        // Démarrer le serveur
        server.start();
        System.out.println("User Service démarré sur le port 8081...");
    }
}