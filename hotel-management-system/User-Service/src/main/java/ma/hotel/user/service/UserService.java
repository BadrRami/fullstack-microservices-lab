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
import ma.hotel.user.dao.UserDAO;
public class UserService {

    private final UserDAO userDAO = new UserDAO();

    public List<UserResponse> getAllUsers() {
        List<User> users = userDAO.findAll();
        return toUserResponseList(users);
    }

    public UserResponse getUserById(Long id) {

        User user = userDAO.findById(id);

        if (user == null) {
            return null;
        }

        return toUserResponse(user);
    }

    public boolean emailExists(String email) {
        return userDAO.existsByEmail(email);
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

    // 5. L'ID sera généré automatiquement par PostgreSQL

    // 6. Créer l'utilisateur
    User user = new User(
            null, // id généré automatiquement
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

    // 7. Enregistrer l'utilisateur dans PostgreSQL
    userDAO.save(user);

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
    return userDAO.existsByEmailAndIdNot(email, userId);
}

public UserResponse updateUser(Long id, UpdateUserRequest request) {

    if (request == null) {
        throw new IllegalArgumentException("Les données de modification sont obligatoires.");
    }

    User user = userDAO.findById(id);
    if (user == null) {
        throw new IllegalArgumentException("Utilisateur non trouvé.");
    }

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

    user.setFirstName(request.getFirstName().trim());
    user.setLastName(request.getLastName().trim());
    user.setEmail(request.getEmail().trim().toLowerCase());
    user.setPhone(request.getPhone().trim());
    user.setUpdatedAt(LocalDateTime.now());

    // 7. Modifier les propriétés autorisées
    if (!userDAO.update(user)) {
        throw new IllegalArgumentException("Échec de la mise à jour de l'utilisateur.");
    }

    return toUserResponse(user);

}


public void deactivateUser(Long id) {

    User user = userDAO.findById(id);

    if (user == null) {
        throw new IllegalArgumentException("Utilisateur non trouvé.");
    }

    if (user.getStatus() == UserStatus.BLOCKED) {
        throw new IllegalArgumentException(
            "Ce compte est bloqué et ne peut pas être désactivé par son propriétaire."
        );
    }

    if (user.getStatus() == UserStatus.INACTIVE) {
        throw new IllegalArgumentException(
            "Ce compte est déjà inactif."
        );
    }

    if (!userDAO.delete(id)) {
        throw new IllegalArgumentException(
            "Échec de la désactivation de l'utilisateur."
        );
    }
}
public void activateUser(String email, String password) {

    User user = userDAO.findByEmail(email);

    if (user == null || !user.getPassword().equals(password)) {
        throw new IllegalArgumentException(
            "Email ou mot de passe incorrect."
        );
    }

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

    if (!userDAO.updateStatus(
            user.getId(),
            user.getStatus(),
            user.getUpdatedAt()
    )) {
        throw new IllegalArgumentException(
            "Échec de la réactivation du compte."
        );
    }
}

}
