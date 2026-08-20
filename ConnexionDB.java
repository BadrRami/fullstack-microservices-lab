// import java.sql.Connection; // représente la connexion entre notre programme Java et PostgreSQL.
// import java.sql.DriverManager; // est la classe qui permet à Java de demander une connexion à la base de données.

// public class ConnexionDB {
//     public static void getConnexion() {

//         String url = "jdbc:postgresql://localhost:5432/java_postgresql_lab";
//         String user = "postgres";
//         String password = "badrrami";

//         try {

//             Connection connexion = DriverManager.getConnection(
//                 url,
//                 user,
//                 password
//             );

//             System.out.println("Connexion réussie à PostgreSQL !");

//             connexion.close();

//         } catch (Exception e) {

//             System.out.println("Erreur de connexion !");
//             e.printStackTrace(); // affiche les détails de l'erreur
//         }
//     }
// }




import java.sql.Connection;
import java.sql.DriverManager;

public class ConnexionDB {

    public static Connection getConnexion() {

        String url = "jdbc:postgresql://localhost:5432/java_postgresql_lab";
        String user = "postgres";
        String password = "badrrami";

        try {

            Connection connexion = DriverManager.getConnection(
                url,
                user,
                password
            );

            return connexion;

        } catch (Exception e) {

            System.out.println("Erreur de connexion !");
            e.printStackTrace();

            return null;
        }
    }
}
