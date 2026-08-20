import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Scanner;
public class Etudiant {

    public static void afficherEtudiants(Connection connexion) {

        try {

            String sql = "SELECT * FROM etudiants";

            PreparedStatement ps = connexion.prepareStatement(sql);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                System.out.println(
                    "ID: " + rs.getInt("id") +
                    ", Nom: " + rs.getString("nom") +
                    ", Prénom: " + rs.getString("prenom") +
                    ", Âge: " + rs.getInt("age") +
                    ", Email: " + rs.getString("email") 

                );
            }

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    public static void AjouterEtudiant(Connection connexion){
        Scanner input = new Scanner(System.in);

        System.out.print("Entrez le nom de l'étudiant: ");
        String nom = input.nextLine();

        System.out.print("Entrez le prénom de l'étudiant: ");
        String prenom = input.nextLine();

        System.out.print("Entrez l'âge de l'étudiant: ");
        int age = input.nextInt();

        System.out.print("Entrez l'email de l'étudiant: ");
        String email = input.nextLine();

        String sql = "INSERT INTO etudiants (nom, prenom, age, email) VALUES (?, ?, ?, ?)";
        try{
            PreparedStatement ps = connexion.prepareStatement(sql);
            ps.setString(1, nom);
            ps.setString(2, prenom);
            ps.setInt(3, age);
            ps.setString(4, email);
            ps.executeUpdate();
            System.out.println("Étudiant ajouté avec succès !");
        } catch (Exception e) {
            System.out.println("Erreur lors de l'insertion !");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        Connection connexion = ConnexionDB.getConnexion();

        if (connexion != null) {

            System.out.println("Connexion réussie à PostgreSQL !");

            afficherEtudiants(connexion);

            try {
                connexion.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}