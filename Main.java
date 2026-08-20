import java.sql.Connection;
import java.sql.DriverManager;

public class Main {

    public static void main(String[] args) {

        String url = "jdbc:postgresql://localhost:5432/java_postgresql_lab";
        String user = "postgres";
        String password = "badrrami";

        try {

            Connection connexion = DriverManager.getConnection(
                url,
                user,
                password
            );

            System.out.println("Connexion réussie à PostgreSQL !");

            connexion.close();

        } catch (Exception e) {

            System.out.println("Erreur de connexion !");
            e.printStackTrace();
        }
    }
}