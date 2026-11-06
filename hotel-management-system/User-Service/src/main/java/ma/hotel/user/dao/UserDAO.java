package ma.hotel.user.dao;

import ma.hotel.user.config.DatabaseConnection;
import ma.hotel.user.model.User;
import ma.hotel.user.model.UserStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import ma.hotel.user.model.Role;
public class UserDAO {
    public void save(User user) {

        String sql = """
                INSERT INTO users (
                    first_name,
                    last_name,
                    email,
                    password,
                    phone,
                    role,
                    status,
                    created_at,
                    updated_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection(); // Ouvre une connexion avec PostgreSQL.
            PreparedStatement statement =
        connection.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);) { // Prépare la requête SQL et retourne les clés générées.

            statement.setString(1, user.getFirstName());  // Affecte une valeur de type String.
            statement.setString(2, user.getLastName());
            statement.setString(3, user.getEmail());
            statement.setString(4, user.getPassword());
            statement.setString(5, user.getPhone());
            statement.setString(6, user.getRole().name()); // Notre Role est un enum Java. La méthode .name() récupère sa valeur sous forme de texte, par exemple CLIENT.
            statement.setString(7, user.getStatus().name());
            statement.setObject(8, user.getCreatedAt()); // Transmet ici les dates Java.
            statement.setObject(9, user.getUpdatedAt());

            statement.executeUpdate(); // 	Exécute l'insertion dans la base.
            try (ResultSet resultSet = statement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    user.setId(resultSet.getLong(1));
                }
            }
            //getGeneratedKeys():  Récupère les clés générées
            //resultSet.next():	Vérifie qu'une clé existe et avance sur le résultat
            //getLong(1)	Récupère la première colonne, ici l'ID

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'enregistrement de l'utilisateur.", e);
        }
    }

    public List<User> findAll() {
        List<User> users = new ArrayList<>();

        String sql = "SELECT * FROM users";

        try (Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery()) {

            //statement.executeQuery() : exécute la requête SQL préparée et demande à PostgreSQL de retourner les résultats.
            //ResultSet resultSet : récupère ces résultats et les stocke dans un objet Java appelé ResultSet.

            while (resultSet.next()) {
                User user = new User();

                user.setId(resultSet.getLong("id"));
                user.setFirstName(resultSet.getString("first_name"));
                user.setLastName(resultSet.getString("last_name"));
                user.setEmail(resultSet.getString("email"));
                user.setPassword(resultSet.getString("password"));
                user.setPhone(resultSet.getString("phone"));
                user.setRole(Role.valueOf(resultSet.getString("role")));
                user.setStatus(UserStatus.valueOf(resultSet.getString("status")));
                user.setCreatedAt(resultSet.getObject("created_at", LocalDateTime.class));
                user.setUpdatedAt(resultSet.getObject("updated_at", LocalDateTime.class));

                users.add(user);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des utilisateurs.", e);
        }

        return users;
    }

    public User findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    User user = new User();

                    user.setId(resultSet.getLong("id"));
                    user.setFirstName(resultSet.getString("first_name"));
                    user.setLastName(resultSet.getString("last_name"));
                    user.setEmail(resultSet.getString("email"));
                    user.setPassword(resultSet.getString("password"));
                    user.setPhone(resultSet.getString("phone"));
                    user.setRole(Role.valueOf(resultSet.getString("role")));
                    user.setStatus(UserStatus.valueOf(resultSet.getString("status")));
                    user.setCreatedAt(resultSet.getObject("created_at", LocalDateTime.class));
                    user.setUpdatedAt(resultSet.getObject("updated_at", LocalDateTime.class));

                    return user;
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erreur lors de la récupération de l'utilisateur par ID.", e
            );
        }

        return null;
    }
    public boolean update(User user) {
        String sql = """
                UPDATE users
                SET first_name = ?,
                    last_name = ?,
                    email = ?,
                    phone = ?,
                    updated_at = ?
                WHERE id = ?
                """;

        try(Connection connection = DatabaseConnection.getConnection();
    PreparedStatement statement = connection.prepareStatement(sql)){

            statement.setString(1, user.getFirstName());
            statement.setString(2, user.getLastName());
            statement.setString(3, user.getEmail());
            statement.setString(4, user.getPhone());
            statement.setObject(5, user.getUpdatedAt());
            statement.setLong(6, user.getId());

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                return true;
            }

        }catch(SQLException e){
            throw new RuntimeException("Erreur lors de la mise à jour de l'utilisateur.", e);

        }
        return false;
    }

    public boolean delete(Long id) {
        String sql = "UPDATE users SET status = 'INACTIVE', updated_at = ? WHERE id = ? AND status = 'ACTIVE'";

        try (Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setObject(1, LocalDateTime.now());
            statement.setLong(2, id);

            int rowsAffected = statement.executeUpdate();

            return rowsAffected > 0;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erreur lors de la désactivation de l'utilisateur.", e
            );
        }
    }

    public boolean existsByEmail(String email) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM users
                    WHERE LOWER(email) = LOWER(?)
                )
                """;

        try (Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, email);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getBoolean(1);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erreur lors de la vérification de l'existence de l'email.", e
            );
        }

        return false;
    }

    public boolean existsByEmailAndIdNot(String email, Long userId){
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM users
                    WHERE LOWER(email) = LOWER(?)
                    AND id <> ?
                )
                """;
        try (Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, email);
            statement.setLong(2, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getBoolean(1);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erreur lors de la vérification de l'existence de l'email.", e
            );
        }

        return false;
    }
    public User findByEmail(String email) {
    String sql = """
            SELECT *
            FROM users
            WHERE LOWER(email) = LOWER(?)
            """;

    try (Connection connection = DatabaseConnection.getConnection();
         PreparedStatement statement = connection.prepareStatement(sql)) {

        statement.setString(1, email);

        try (ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                User user = new User();

                user.setId(resultSet.getLong("id"));
                user.setFirstName(resultSet.getString("first_name"));
                user.setLastName(resultSet.getString("last_name"));
                user.setEmail(resultSet.getString("email"));
                user.setPassword(resultSet.getString("password"));
                user.setPhone(resultSet.getString("phone"));
                user.setRole(Role.valueOf(resultSet.getString("role")));
                user.setStatus(UserStatus.valueOf(resultSet.getString("status")));
                user.setCreatedAt(resultSet.getObject("created_at", LocalDateTime.class));
                user.setUpdatedAt(resultSet.getObject("updated_at", LocalDateTime.class));

                return user;
            }
        }

    } catch (SQLException e) {
        throw new RuntimeException(
                "Erreur lors de la récupération de l'utilisateur par email.", e
        );
    }

    return null;
}

public boolean updateStatus(Long id, UserStatus status, LocalDateTime updatedAt) {

    String sql = """
            UPDATE users
            SET status = ?, updated_at = ?
            WHERE id = ?
            """;

    try (Connection connection = DatabaseConnection.getConnection();
         PreparedStatement statement = connection.prepareStatement(sql)) {

        statement.setString(1, status.name());
        statement.setObject(2, updatedAt);
        statement.setLong(3, id);

        int rowsAffected = statement.executeUpdate();
        return rowsAffected > 0;

    } catch (SQLException e) {
        throw new RuntimeException(
                "Erreur lors de la mise à jour du statut de l'utilisateur.", e
        );
    }
}

}

/*
Méthode             	Utilisation
executeQuery()      	SELECT : récupérer des résultats
executeUpdate()     	INSERT, UPDATE, DELETE : modifier les données
execute()           	Exécuter différents types de requêtes SQL
*/