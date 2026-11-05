package ma.hotel.user;

import ma.hotel.user.config.DatabaseConnection;
import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConnectionTest {

    public static void main(String[] args) {

        try (Connection connection = DatabaseConnection.getConnection()) {

            if (connection != null) {
                System.out.println("Connexion PostgreSQL réussie !");
                System.out.println("Base de données : "
                        + connection.getCatalog());
            }

        } catch (SQLException e) {
            System.out.println("Échec de la connexion PostgreSQL.");
            e.printStackTrace();
        }
    }
}