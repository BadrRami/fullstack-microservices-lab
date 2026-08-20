import java.sql.Connection;

public class Etudiant {

    public static void main(String[] args) {

        Connection connexion = ConnexionDB.getConnexion();

        if (connexion != null) {

            System.out.println("Connexion réussie à PostgreSQL !");

            try {
                connexion.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}