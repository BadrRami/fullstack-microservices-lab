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

        if (method.equals("GET")) {

            List<Etudiant> etudiants = etudiantDAO.afficherEtudiants();

            String json = objectMapper.writeValueAsString(etudiants);

        }
    }
}