package server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import dao.EtudiantDAO;
import database.ConnexionDB;
import model.Etudiant;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.List;

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

        try {

            if (method.equals("GET")) {

                String[] parts = path.split("/");

                if (parts.length == 2) {

                    List<Etudiant> etudiants = etudiantDAO.afficherEtudiants();

                    String json = objectMapper.writeValueAsString(etudiants);

                    envoyerReponse(exchange, 200, json);

                } else if (parts.length == 3) {

                    int id;

                    try {
                        id = Integer.parseInt(parts[2]);
                    } catch (NumberFormatException e) {
                        envoyerErreur(exchange, 400, "L'ID doit être un nombre.");
                        return;
                    }

                    if (id <= 0) {
                        envoyerErreur(exchange, 400, "L'ID doit être supérieur à 0.");
                        return;
                    }

                    Etudiant etudiant = etudiantDAO.chercherEtudiantParId(id);

                    if (etudiant == null) {
                        envoyerErreur(exchange, 404, "Étudiant introuvable.");
                        return;
                    }

                    String json = objectMapper.writeValueAsString(etudiant);

                    envoyerReponse(exchange, 200, json);

                } else {

                    envoyerErreur(exchange, 400, "URL invalide.");
                }
            }else if (method.equals("POST")) {

                Etudiant etudiant = objectMapper.readValue(
                        exchange.getRequestBody(),
                        Etudiant.class
                );

                if (!donneesValides(etudiant)) {
                    envoyerErreur(exchange, 400, "Les données de l'étudiant sont invalides.");
                    return;
                }

                etudiantDAO.ajouterEtudiant(etudiant);

                envoyerReponse(
                        exchange,
                        201,
                        "{\"message\":\"Étudiant ajouté avec succès\"}"
                );
            }

            else if (method.equals("PUT")) {

                Etudiant etudiant = objectMapper.readValue(
                        exchange.getRequestBody(),
                        Etudiant.class
                );

                if (etudiant.getId() <= 0) {
                    envoyerErreur(exchange, 400, "L'ID de l'étudiant est invalide.");
                    return;
                }

                if (!donneesValides(etudiant)) {
                    envoyerErreur(exchange, 400, "Les données de l'étudiant sont invalides.");
                    return;
                }

                boolean modifie = etudiantDAO.modifierEtudiant(etudiant);

                if (!modifie) {
                    envoyerErreur(exchange, 404, "Étudiant introuvable.");
                    return;
                }

                envoyerReponse(
                        exchange,
                        200,
                        "{\"message\":\"Étudiant modifié avec succès\"}"
                );
            }

            else if (method.equals("DELETE")) {

                String[] parts = path.split("/");

                if (parts.length != 3 || parts[2].isEmpty()) {
                    envoyerErreur(exchange, 400, "ID étudiant manquant.");
                    return;
                }

                int id;

                try {
                    id = Integer.parseInt(parts[2]);
                } catch (NumberFormatException e) {
                    envoyerErreur(exchange, 400, "L'ID doit être un nombre.");
                    return;
                }

                if (id <= 0) {
                    envoyerErreur(exchange, 400, "L'ID doit être supérieur à 0.");
                    return;
                }

                boolean supprime = etudiantDAO.supprimerEtudiant(id);

                if (!supprime) {
                    envoyerErreur(exchange, 404, "Étudiant introuvable.");
                    return;
                }

                envoyerReponse(
                        exchange,
                        200,
                        "{\"message\":\"Étudiant supprimé avec succès\"}"
                );
            }

            else {
                envoyerErreur(
                        exchange,
                        405,
                        "Méthode HTTP non autorisée."
                );
            }

        } catch (Exception e) {

            e.printStackTrace();

            envoyerErreur(
                    exchange,
                    500,
                    "Une erreur interne du serveur est survenue."
            );
        }
    }

    private boolean donneesValides(Etudiant etudiant) {

        if (etudiant == null) {
            return false;
        }

        if (etudiant.getNom() == null || etudiant.getNom().isBlank()) {
            return false;
        }

        if (etudiant.getPrenom() == null || etudiant.getPrenom().isBlank()) {
            return false;
        }

        if (etudiant.getAge() <= 0) {
            return false;
        }

        if (etudiant.getEmail() == null || etudiant.getEmail().isBlank()) {
            return false;
        }

        return true;
    }

    private void envoyerReponse(
            HttpExchange exchange,
            int statusCode,
            String contenu
    ) throws IOException {

        byte[] response = contenu.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set(
                "Content-Type",
                "application/json; charset=UTF-8"
        );

        exchange.sendResponseHeaders(statusCode, response.length);

        exchange.getResponseBody().write(response);
        exchange.getResponseBody().close();
    }

    private void envoyerErreur(
            HttpExchange exchange,
            int statusCode,
            String message
    ) throws IOException {

        String json = objectMapper.writeValueAsString(
                new MessageErreur(message)
        );

        envoyerReponse(exchange, statusCode, json);
    }

    private static class MessageErreur {

        public String message;

        public MessageErreur(String message) {
            this.message = message;
        }
    }
}