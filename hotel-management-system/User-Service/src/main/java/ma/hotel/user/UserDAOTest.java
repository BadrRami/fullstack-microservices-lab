package ma.hotel.user;

import ma.hotel.user.dao.UserDAO;
import ma.hotel.user.model.Role;
import ma.hotel.user.model.User;
import ma.hotel.user.model.UserStatus;

import java.io.ObjectInputFilter.Status;
import java.time.LocalDateTime;
import java.util.List;

public class UserDAOTest {

    public static void main(String[] args) {

    UserDAO userDAO = new UserDAO();

    User user = userDAO.findById(1L); // Exemple d'ID



boolean deleted = userDAO.delete(1L);

if (deleted) {
    System.out.println("Utilisateur désactivé avec succès !");
} else {
    System.out.println("Utilisateur introuvable ou déjà inactif.");
}
    }
}