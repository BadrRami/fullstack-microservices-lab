package dao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import model.Etudiant;

public class EtudiantDAO {

    private Connection connexion;

    public EtudiantDAO(Connection connexion) {
        this.connexion = connexion;
    }

    public List<Etudiant> afficherEtudiants() {

        List<Etudiant> etudiants = new ArrayList<>();

        String sql = "SELECT * FROM etudiants";

        try (PreparedStatement ps = connexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                Etudiant etudiant = new Etudiant(
                    rs.getInt("id"),
                    rs.getString("nom"),
                    rs.getString("prenom"),
                    rs.getInt("age"),
                    rs.getString("email")
                );

                etudiants.add(etudiant);
            }

        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des étudiants !");
            e.printStackTrace();
        }

        return etudiants;
    }

    public void ajouterEtudiant(Etudiant etudiant) {

        String sql = "INSERT INTO etudiants (nom, prenom, age, email) VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = connexion.prepareStatement(sql)) {

            ps.setString(1, etudiant.getNom());
            ps.setString(2, etudiant.getPrenom());
            ps.setInt(3, etudiant.getAge());
            ps.setString(4, etudiant.getEmail());

            ps.executeUpdate();

            System.out.println("Étudiant ajouté avec succès !");

        } catch (SQLException e) {
            System.out.println("Erreur lors de l'insertion !");
            e.printStackTrace();
        }
    }

    public boolean modifierEtudiant(Etudiant etudiant) {

        String sql = "UPDATE etudiants SET nom = ?, prenom = ?, age = ?, email = ? WHERE id = ?";

        try (PreparedStatement ps = connexion.prepareStatement(sql)) {

            ps.setString(1, etudiant.getNom());
            ps.setString(2, etudiant.getPrenom());
            ps.setInt(3, etudiant.getAge());
            ps.setString(4, etudiant.getEmail());
            ps.setInt(5, etudiant.getId());

            int lignesModifiees = ps.executeUpdate();

            return lignesModifiees > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean supprimerEtudiant(int id) {

        String sql = "DELETE FROM etudiants WHERE id = ?";

        try (PreparedStatement ps = connexion.prepareStatement(sql)) {

            ps.setInt(1, id);

            int lignesSupprimees = ps.executeUpdate();

            return lignesSupprimees > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public Etudiant chercherEtudiantParId(int id) {

        String sql = "SELECT * FROM etudiants WHERE id = ?";

        try (PreparedStatement ps = connexion.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    return new Etudiant(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getInt("age"),
                        rs.getString("email")
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
    
}