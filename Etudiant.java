import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Scanner;
public class Etudiant {

    public static void afficherEtudiants(Connection connexion) {

        try {

            String sql = "SELECT * FROM etudiants";

            PreparedStatement ps = connexion.prepareStatement(sql);

            ResultSet rs = ps.executeQuery();// Exécute la requête SELECT et récupère les résultats dans un ResultSet

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
            //sert à remplacer le premier ? de la requête SQL par la valeur contenue dans nom.
            // nom = ?       → 1er ?
            // prenom = ?    → 2ème ?
            // id = ?        → 3ème ?
            ps.setString(2, prenom);
            ps.setInt(3, age);
            ps.setString(4, email);
            ps.executeUpdate();
            // Cette ligne sert à exécuter une requête qui modifie les données de la base de données.
            System.out.println("Étudiant ajouté avec succès !");
        } catch (Exception e) {
            System.out.println("Erreur lors de l'insertion !");
            e.printStackTrace();
        }
    }

    public static void ModifierEtudiant(Connection connexion) {

    Scanner input = new Scanner(System.in);

    try {

        System.out.print("Entrez l'ID de l'étudiant à modifier : ");
        int id = input.nextInt();
        input.nextLine();

        System.out.print("Entrez le nouveau nom de l'étudiant : ");
        String nom = input.nextLine();

        System.out.print("Entrez le nouveau prénom de l'étudiant : ");
        String prenom = input.nextLine();

        System.out.print("Entrez le nouvel âge de l'étudiant : ");
        int age = input.nextInt();
        input.nextLine();

        System.out.print("Entrez le nouvel email de l'étudiant : ");
        String email = input.nextLine();

        String sql = "UPDATE etudiants SET nom = ?, prenom = ?, age = ?, email = ? WHERE id = ?";

        PreparedStatement ps = connexion.prepareStatement(sql);

        ps.setString(1, nom);
        ps.setString(2, prenom);
        ps.setInt(3, age);
        ps.setString(4, email);
        ps.setInt(5, id);

        int lignesModifiees = ps.executeUpdate();

        if (lignesModifiees > 0) {
            System.out.println("Étudiant modifié avec succès !");
        } else {
            System.out.println("Aucun étudiant trouvé avec cet ID.");
        }

    } catch (Exception e) {

        System.out.println("Erreur lors de la modification !");
        e.printStackTrace();
    }
}

public static void SupprimerEtudiant(Connection connexion) {

    Scanner input = new Scanner(System.in);

    System.out.print("Entrez l'ID de l'étudiant à supprimer : ");
    int id = input.nextInt();

    String sql = "DELETE FROM etudiants WHERE id = ?";

    try {

        PreparedStatement ps = connexion.prepareStatement(sql);

        ps.setInt(1, id);

        int lignesSupprimees = ps.executeUpdate();

        if (lignesSupprimees > 0) {
            System.out.println("Étudiant supprimé avec succès !");
        } else {
            System.out.println("Aucun étudiant trouvé avec cet ID.");
        }

    } catch (Exception e) {

        System.out.println("Erreur lors de la suppression !");
        e.printStackTrace();
    }
}

    // public static void main(String[] args) {

    //     Connection connexion = ConnexionDB.getConnexion();

    //     if (connexion != null) {

    //         System.out.println("Connexion réussie à PostgreSQL !");

    //         SupprimerEtudiant(connexion);

    //         try {
    //             connexion.close();
    //         } catch (Exception e) {
    //             e.printStackTrace();
    //         }
    //     }
    // }
}