package ma.hotel.user.service;

import ma.hotel.user.dao.UserDAO;
import ma.hotel.user.model.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.mindrot.jbcrypt.BCrypt;

import ma.hotel.user.model.Role;
import ma.hotel.user.model.UserStatus;
import java.time.LocalDateTime;

@ExtendWith(MockitoExtension.class) // Active l'intégration Mockito avec JUnit 5.
class UserServiceTest {

    @Mock
    private UserDAO userDAO; // Crée un faux UserDAO.

    private UserService userService;

    @BeforeEach
    void setUp() { // JUnit exécute setUp() avant chaque méthode de test.
        userService = new UserService(userDAO);
    }

    @Test
    void shouldRegisterUserSuccessfully() {

        // Arrange : préparer le scénario
        when(userDAO.existsByEmail("badr@example.com"))
                .thenReturn(false);

        // Act : exécuter la méthode
        User user = userService.registerUser(
                "Badr",
                "Rami",
                "badr@example.com",
                "password123",
                "0612345678"
        );

        // Assert : vérifier le résultat
        assertNotNull(user); // Vérifie que l'inscription a retourné un utilisateur et non null.
        assertEquals("Badr", user.getFirstName()); // Vérifie que le prénom correspond à celui envoyé.
        assertEquals("badr@example.com", user.getEmail()); // Vérifie que l'email est correct.

        // Vérifier que le mot de passe est bien hashé
        assertNotEquals("password123", user.getPassword()); // Vérifie que le mot de passe stocké n'est pas en texte brut.

        assertTrue(
                BCrypt.checkpw("password123", user.getPassword()) // Elle vérifie que le hash enregistré correspond réellement au mot de passe original.
        );

        // Vérifier que save() a été appelé une seule fois
        verify(userDAO, times(1)).save(any(User.class)); // Vérifie que le DAO a bien reçu une demande d'enregistrement.

    }
    @Test
    void shouldRejectInvalidEmail() {

        assertThrows(
                IllegalArgumentException.class,
                () -> userService.registerUser(
                        "Badr",
                        "Rami",
                        "email-invalide",
                        "password123",
                        "0612345678"
                )
        );

        verify(userDAO, never()).save(any(User.class));
    }
    @Test
    void shouldRejectDuplicateEmail() {

        when(userDAO.existsByEmail("badr@example.com"))
                .thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> userService.registerUser(
                        "Badr",
                        "Rami",
                        "badr@example.com",
                        "password123",
                        "0612345678"
                )
        );

        verify(userDAO, never()).save(any(User.class));
    }
    @Test
    void shouldRejectShortPassword() {

        when(userDAO.existsByEmail("badr@example.com"))
                .thenReturn(false);

        assertThrows(
                IllegalArgumentException.class,
                () -> userService.registerUser(
                        "Badr",
                        "Rami",
                        "badr@example.com",
                        "12345",
                        "0612345678"
                )
        );

        verify(userDAO, never()).save(any(User.class));
    }

    @Test 
    void shouldActivateInactiveUserSuccessfully(){
        // Arrange : preparer le scénario
        String password = "password123";

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        User user = new User(
                1L,
                "Badr",
                "Rami",
                "badr@example.com",
                hashedPassword,
                "0612345678",
                Role.CLIENT,
                UserStatus.INACTIVE,
                LocalDateTime.now(),
                null
        );
        when(userDAO.findByEmail("badr@example.com")).thenReturn(user);
        when(userDAO.updateStatus(
                eq(1L),
                eq(UserStatus.ACTIVE),
                any(LocalDateTime.class)

        )).thenReturn(true);

        // Act : exécuter la méthode
        userService.activateUser("badr@example.com", password);

        // Assert : vérifier le résultat
        assertEquals(UserStatus.ACTIVE, user.getStatus());
        assertNotNull(user.getUpdatedAt());
        verify(userDAO, times(1)).findByEmail("badr@example.com");
        verify(userDAO, times(1)).updateStatus(
                eq(1L),
                eq(UserStatus.ACTIVE),
                any(LocalDateTime.class)
        );
    }

    @Test 
    void shouldRejectBlockedUser(){

        // Arrange : preparer le scénario
        String password = "password123";
        String email = "badr@example.com";

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        User user = new User(
                1L,
                "Badr",
                "Rami",
                email,
                hashedPassword,
                "0612345678",
                Role.CLIENT,
                UserStatus.BLOCKED,
                LocalDateTime.now(),
                null
        );
        when(userDAO.findByEmail(email)).thenReturn(user);
        
        // Act & Assert : vérifier que l'exception est levée
        assertThrows(
                IllegalArgumentException.class,
                () -> userService.activateUser(email, password)
        );

        assertEquals(UserStatus.BLOCKED, user.getStatus());
        verify(userDAO, never()).updateStatus(any(Long.class), any(UserStatus.class), any(LocalDateTime.class));
    }
}