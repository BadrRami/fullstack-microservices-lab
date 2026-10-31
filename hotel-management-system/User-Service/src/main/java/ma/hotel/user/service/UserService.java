package ma.hotel.user.service;

import ma.hotel.user.model.User;
import ma.hotel.user.model.UserStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import ma.hotel.user.model.Role;

public class UserService {

    private final List<User> users = new ArrayList<>();

    public List<User> getAllUsers() {
        return users;
    }

    public User getUserById(Long id) {

        for (User user : users) {
            if (user.getId().equals(id)) {
                return user;
            }
        }

        return null;
    }

    public boolean emailExists(String email) {

        for (User user : users) {
            if (user.getEmail().equalsIgnoreCase(email)) {
                return true;
            }
        }

        return false;
    }

    public User registerUser(
        String firstName,
        String lastName,
        String email,
        String password,
        String phone
) {

    // 1. Vérifier les champs obligatoires
    if (firstName == null || firstName.isBlank()
            || lastName == null || lastName.isBlank()
            || email == null || email.isBlank()
            || password == null || password.isBlank()
            || phone == null || phone.isBlank()) {

        throw new IllegalArgumentException(
                "Tous les champs sont obligatoires."
        );
    }

    // 2. Vérifier le format de l'email
    String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

    if (!Pattern.matches(emailRegex, email)) {
        throw new IllegalArgumentException(
                "Format d'email invalide."
        );
    }

    // 3. Vérifier si l'email existe déjà
    if (emailExists(email)) {
        throw new IllegalArgumentException(
                "Cette adresse email est déjà utilisée."
        );
    }

    // 4. Vérifier la longueur du mot de passe
    if (password.length() < 8) {
        throw new IllegalArgumentException(
                "Le mot de passe doit contenir au moins 8 caractères."
        );
    }

    // 5. Générer un nouvel ID
    Long id = users.size() + 1L;

    // 6. Créer l'utilisateur
    User user = new User(
            id,
            firstName.trim(),
            lastName.trim(),
            email.trim().toLowerCase(),
            password,
            phone.trim(),
            Role.CLIENT,
            UserStatus.ACTIVE,
            LocalDateTime.now()
    );

    // 7. Ajouter l'utilisateur à la liste
    users.add(user);

    return user;
}
}