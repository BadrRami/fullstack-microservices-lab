package server;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;

public class ServeurHTTP {

    public static void main(String[] args) throws IOException {

        HttpServer server = HttpServer.create(
                new InetSocketAddress(8081),
                0
        );

        System.out.println("Serveur HTTP démarré sur le port 8081");

        server.createContext("/etudiants", new EtudiantHandler());

        server.start();
    }
}