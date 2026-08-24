// package server;

// import com.sun.net.httpserver.HttpServer;
// import java.io.IOException;
// import java.net.InetSocketAddress;

// public class ServeurHTTP {

//     public static void main(String[] args) throws IOException {

//         HttpServer server = HttpServer.create(
//                 new InetSocketAddress(8081),
//                 0
//         );

//         System.out.println("Serveur HTTP démarré sur le port 8081");

//         server.createContext("/etudiants", new EtudiantHandler());

//         server.start();
//     }
// }

package server;

import com.sun.net.httpserver.HttpServer;

import dao.EtudiantDAO;
import database.ConnexionDB;
import service.EtudiantService;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.Connection;

public class ServeurHTTP {

    public static void main(String[] args) throws IOException {

        HttpServer server = HttpServer.create(
                new InetSocketAddress(8081),
                0
        );

        Connection connexion = ConnexionDB.getConnexion();

        EtudiantDAO etudiantDAO = new EtudiantDAO(connexion);

        EtudiantService etudiantService =
                new EtudiantService(etudiantDAO);

        EtudiantHandler etudiantHandler =
                new EtudiantHandler(etudiantService);

        server.createContext(
                "/etudiants",
                etudiantHandler
        );

        System.out.println(
                "Serveur HTTP démarré sur le port 8081"
        );

        server.start();
    }
}