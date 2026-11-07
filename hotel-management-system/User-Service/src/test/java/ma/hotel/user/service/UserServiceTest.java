package ma.hotel.user.service;

import ma.hotel.user.dao.UserDAO;
import ma.hotel.user.model.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.mindrot.jbcrypt.BCrypt;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserDAO userDAO;

    private UserService userService;

    @BeforeEach
    void setUp() {
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
        assertNotNull(user);
        assertEquals("Badr", user.getFirstName());
        assertEquals("badr@example.com", user.getEmail());

        // Vérifier que le mot de passe est bien hashé
        assertNotEquals("password123", user.getPassword());

        assertTrue(
                BCrypt.checkpw("password123", user.getPassword())
        );

        // Vérifier que save() a été appelé une seule fois
        verify(userDAO, times(1)).save(any(User.class));
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
}