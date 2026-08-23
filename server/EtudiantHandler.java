package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import dao.EtudiantDAO;
import database.ConnexionDB;
import model.Etudiant;

import java.io.IOException;
import java.sql.Connection;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

public class EtudiantHandler implements HttpHandler {

    private EtudiantDAO etudiantDAO;
    private ObjectMapper objectMapper = new ObjectMapper();

    public EtudiantHandler() {

        Connection connexion = ConnexionDB.getConnexion();

        etudiantDAO = new EtudiantDAO(connexion);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        if (method.equals("GET")) {

            List<Etudiant> etudiants = etudiantDAO.afficherEtudiants();

            String json = objectMapper.writeValueAsString(etudiants);

            exchange.getResponseHeaders().set("Content-Type", "application/json");

            exchange.sendResponseHeaders(200, json.getBytes().length);

            exchange.getResponseBody().write(json.getBytes());

            exchange.getResponseBody().close();
          
        }else if (method.equals("POST")) {

            Etudiant etudiant = objectMapper.readValue(
                exchange.getRequestBody(),
                Etudiant.class
            );

            etudiantDAO.ajouterEtudiant(etudiant);

            exchange.sendResponseHeaders(201, -1);
            exchange.getResponseBody().close();
        }else if (method.equals("DELETE")) {

            String[] parts = path.split("/");

            int id = Integer.parseInt(parts[2]);

            etudiantDAO.supprimerEtudiant(id);

            exchange.sendResponseHeaders(204, -1);
            exchange.getResponseBody().close();
        }else if (method.equals("PUT")) {

            Etudiant etudiant = objectMapper.readValue(
                exchange.getRequestBody(),
                Etudiant.class
            );

            etudiantDAO.modifierEtudiant(etudiant);

            exchange.sendResponseHeaders(204, -1);
            exchange.getResponseBody().close();
        }
    }
}