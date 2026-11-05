package ma.hotel.user.service;

import ma.hotel.user.model.User;
import ma.hotel.user.model.UserStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import ma.hotel.user.dto.UserResponse;
import ma.hotel.user.model.Role;
import ma.hotel.user.dto.UpdateUserRequest;

public class UserService {

    private final List<User> users = new ArrayList<>();

    public List<UserResponse> getAllUsers() {
        return toUserResponseList(users);
    }

    public UserResponse getUserById(Long id) {

        for (User user : users) {
            if (user.getId().equals(id)) {
                return toUserResponse(user);
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
            LocalDateTime.now(),
            null // updatedAt initialisé à null
    );

    // 7. Ajouter l'utilisateur à la liste
    users.add(user);

    return user;
}
public UserResponse toUserResponse(User user) {
    UserResponse response = new UserResponse();

response.setId(user.getId());
response.setFirstName(user.getFirstName());
response.setLastName(user.getLastName());
response.setEmail(user.getEmail());
response.setPhone(user.getPhone());
response.setRole(user.getRole());
response.setStatus(user.getStatus());
response.setCreatedAt(user.getCreatedAt());
response.setUpdatedAt(user.getUpdatedAt());

return response;
}

public List<UserResponse> toUserResponseList(List<User> users) {

    List<UserResponse> responses = new ArrayList<>();

    for (User user : users) {
        UserResponse response = toUserResponse(user);
        responses.add(response);
    }

    return responses;
}

private boolean emailExistsForAnotherUser(String email, Long userId) {
    for (User user : users) {
        if (user.getEmail().equalsIgnoreCase(email)
                && !user.getId().equals(userId)) {
            return true;
        }
    }
    return false;
}

public UserResponse updateUser(Long id, UpdateUserRequest request) {

    if (request == null) {
        throw new IllegalArgumentException("Les données de modification sont obligatoires.");
    }

    // Rechercher l'utilisateur
    for (User user : users) {
        if (user.getId().equals(id)) {

            // 1. Vérifier le prénom
            if (request.getFirstName() == null || request.getFirstName().isBlank()) {
                throw new IllegalArgumentException("Le prénom est obligatoire.");
            }

            // 2. Vérifier le nom
            if (request.getLastName() == null || request.getLastName().isBlank()) {
                throw new IllegalArgumentException("Le nom est obligatoire.");
            }

            // 3. Vérifier l'email
            if (request.getEmail() == null || request.getEmail().isBlank()) {
                throw new IllegalArgumentException("L'email est obligatoire.");
            }

            // 4. Vérifier le format de l'email
            String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

            if (!Pattern.matches(emailRegex, request.getEmail())) {
                throw new IllegalArgumentException("Format d'email invalide.");
            }

            // 5. Vérifier si l'email appartient à un autre utilisateur
            if (emailExistsForAnotherUser(request.getEmail(), id)) {
                throw new IllegalArgumentException("Cette adresse email est déjà utilisée.");
            }

            // 6. Vérifier le téléphone
            if (request.getPhone() == null || request.getPhone().isBlank()) {
                throw new IllegalArgumentException("Le numéro de téléphone est obligatoire.");
            }

            // 7. Modifier les propriétés autorisées
            user.setFirstName(request.getFirstName().trim());
            user.setLastName(request.getLastName().trim());
            user.setEmail(request.getEmail().trim().toLowerCase());
            user.setPhone(request.getPhone().trim());

            // 8. Actualiser updatedAt
            user.setUpdatedAt(LocalDateTime.now());

            // 9. Retourner UserResponse
            return toUserResponse(user);
        }
    }

    // Utilisateur introuvable
    throw new IllegalArgumentException("Utilisateur non trouvé.");
}
public void deactivateUser(Long id) {

    for (User user : users) {

        if (user.getId().equals(id)) {

            if (user.getStatus() == UserStatus.BLOCKED) {
                throw new IllegalArgumentException(
                    "Ce compte est bloqué et ne peut pas être désactivé par son propriétaire."
                );
            }

            user.setStatus(UserStatus.INACTIVE);
            user.setUpdatedAt(LocalDateTime.now());

            return;
        }
    }

    throw new IllegalArgumentException("Utilisateur non trouvé.");

}
public void activateUser(String email, String password) {

    for (User user : users) {

        if (user.getEmail().equalsIgnoreCase(email)
                && user.getPassword().equals(password)) {

            if (user.getStatus() == UserStatus.BLOCKED) {
                throw new IllegalArgumentException(
                    "Ce compte est bloqué et ne peut pas être activé."
                );
            }

            if (user.getStatus() == UserStatus.ACTIVE) {
                throw new IllegalArgumentException(
                    "Ce compte est déjà actif."
                );
            }

            user.setStatus(UserStatus.ACTIVE);
            user.setUpdatedAt(LocalDateTime.now());

            return;
        }
    }

    throw new IllegalArgumentException(
        "Email ou mot de passe incorrect."
    );
}
}
