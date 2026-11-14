package ma.hotel.user.handler;

import ma.hotel.user.service.UserService;
import ma.hotel.user.model.User;
import ma.hotel.user.dto.UpdateUserRequest;
import ma.hotel.user.dto.UserResponse;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sun.net.httpserver.Headers;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import ma.hotel.user.dto.UserResponse;
import ma.hotel.user.model.Role;
import ma.hotel.user.model.UserStatus;
import com.sun.net.httpserver.HttpExchange;
@ExtendWith (MockitoExtension.class) // Active l'intégration Mockito avec JUnit 5.
class UsersHandlerTest {
    @Mock 
    private UserService userService; // Crée un faux UserService.

    @Mock
    // @Mock demande à Mockito de créer un faux objet HttpExchange.
    // Nous pourrons simuler une requête HTTP sans démarrer réellement le serveur sur le port 8081.
    private HttpExchange exchange;

    private UsersHandler usersHandler;
    private ByteArrayOutputStream responseBody;

    @BeforeEach 
    void setUp(){
        // Injecter le UserService simulé dans le handler
        usersHandler = new UsersHandler(userService); // Initialise UsersHandler avec le faux UserService.
        
        responseBody = new ByteArrayOutputStream(); // Crée une mémoire temporaire dans laquelle nous allons récupérer la réponse HTTP.

        when(exchange.getResponseHeaders()).thenReturn(new Headers()); // Quand le handler demande les en-têtes de réponse, Mockito lui retourne un véritable objet Headers.
        when(exchange.getResponseBody()).thenReturn(responseBody); // Quand le handler veut écrire sa réponse, Mockito lui donne notre ByteArrayOutputStream.

    }

    @Test

void shouldRegisterUserSuccessfully() throws IOException {

    // 1. Préparer le JSON de la requête
    String jsonRequest = """
        {
            "firstName": "Badr",
            "lastName": "Rami",
            "email": "badr@example.com",
            "password": "password123",
            "phone": "0612345678"
        }
        """;

    // 2. Simuler la requête HTTP POST
    when(exchange.getRequestMethod()).thenReturn("POST");

    when(exchange.getRequestURI())
            .thenReturn(URI.create("/users/register"));

    when(exchange.getRequestBody()).thenReturn(
            new ByteArrayInputStream(
                    jsonRequest.getBytes(StandardCharsets.UTF_8)
            )
    );

    // 3. Simuler le comportement du UserService
    User mockUser = mock(User.class);

    when(userService.registerUser(
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString()
    )).thenReturn(mockUser);

    // 4. Simuler la conversion User vers UserResponse
    UserResponse mockResponse = mock(UserResponse.class);

    when(userService.toUserResponse(mockUser))
            .thenReturn(mockResponse);

    // 5. Exécuter le handler
    usersHandler.handle(exchange);

    // 6. Vérifier que le service a reçu les bonnes informations
    verify(userService).registerUser(
            eq("Badr"),
            eq("Rami"),
            eq("badr@example.com"),
            eq("password123"),
            eq("0612345678")
    );

    // 7. Vérifier le statut HTTP 201
    verify(exchange).sendResponseHeaders(eq(201), anyLong());

    // 8. Vérifier que la réponse HTTP n'est pas vide
    assertFalse(responseBody.toString(StandardCharsets.UTF_8).isEmpty());
}
@Test
void shouldReturn400WhenJsonIsInvalid() throws IOException {

    // 1. Préparer un JSON incorrect
    String jsonRequestIncorrecte = """
        {
            "firstName": "Badr",
            "lastName": "Rami",
            "email": "badr@example.com",
            "password": "password123"
            "phone": "0612345678"
        }
        """;

    // 2. Simuler la requête HTTP POST
    when(exchange.getRequestMethod()).thenReturn("POST");

    when(exchange.getRequestURI())
            .thenReturn(URI.create("/users/register"));

    when(exchange.getRequestBody()).thenReturn(
            new ByteArrayInputStream(
                    jsonRequestIncorrecte.getBytes(StandardCharsets.UTF_8)
            )
    );

    // 3. Exécuter le handler
    usersHandler.handle(exchange);

    // 4. Vérifier le statut HTTP 400
    verify(exchange).sendResponseHeaders(eq(400), anyLong());

    // 5. Vérifier le contenu de la réponse
    String response = responseBody.toString(StandardCharsets.UTF_8);

    assertTrue(response.contains("JSON invalide"));

    // 6. Vérifier que le service n'a jamais été appelé
    verify(userService, never()).registerUser(
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString()
    );
}

@Test
void shouldReturn400WhenEmailAlreadyExists() throws IOException {

    // 1. Préparer un JSON valide
    String jsonRequest = """
        {
            "firstName": "Badr",
            "lastName": "Rami",
            "email": "badr@example.com",
            "password": "password123",
            "phone": "0612345678"
        }
        """;

    // 2. Simuler la requête HTTP POST
    when(exchange.getRequestMethod()).thenReturn("POST");

    when(exchange.getRequestURI())
            .thenReturn(URI.create("/users/register"));

    when(exchange.getRequestBody()).thenReturn(
            new ByteArrayInputStream(
                    jsonRequest.getBytes(StandardCharsets.UTF_8)
            )
    );

    // 3. Simuler une exception métier du UserService
    when(userService.registerUser(
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString()
    )).thenThrow(new IllegalArgumentException("Email déjà utilisé"));

    // 4. Exécuter le handler
    usersHandler.handle(exchange);

    // 5. Vérifier que le service a été appelé
    verify(userService, times(1)).registerUser(
            eq("Badr"),
            eq("Rami"),
            eq("badr@example.com"),
            eq("password123"),
            eq("0612345678")
    );

    // 6. Vérifier le statut HTTP 400
    verify(exchange).sendResponseHeaders(eq(400), anyLong());

    // 7. Vérifier le message d'erreur
    String response = responseBody.toString(StandardCharsets.UTF_8);

    assertTrue(response.contains("Email déjà utilisé"));
}
@Test 
void shouldReturn500WhenUnexpectedExceptionOccurs() throws IOException {
    // 1. Préparer un JSON valide
    String jsonRequest = """
        {
            "firstName": "Badr",
            "lastName": "Rami",
            "email": "badr@example.com",
            "password": "password123",
            "phone": "0612345678"
        }
        """;

    // 2. Simuler la requête HTTP POST
    when(exchange.getRequestMethod()).thenReturn("POST");

    when(exchange.getRequestURI())
            .thenReturn(URI.create("/users/register"));

    when(exchange.getRequestBody()).thenReturn(
            new ByteArrayInputStream(
                    jsonRequest.getBytes(StandardCharsets.UTF_8)
            )
    );
    // 3. Simuler une exception métier du UserService
    when(userService.registerUser(
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString()
    )).thenThrow(new RuntimeException("Erreur interne"));

    usersHandler.handle(exchange);
    verify(userService, times(1)).registerUser(
        eq("Badr"),
        eq("Rami"),
        eq("badr@example.com"),
        eq("password123"),
        eq("0612345678")
    );
    verify(exchange).sendResponseHeaders(eq(500), anyLong());
    
}



@Test
void shouldReturnUsersSuccessfully() throws IOException {

    // 1. Simuler une requête HTTP GET
    when(exchange.getRequestMethod()).thenReturn("GET");

    when(exchange.getRequestURI())
            .thenReturn(URI.create("/users"));


    // 3. Préparer un utilisateur fictif
    UserResponse user = new UserResponse();

    user.setId(1L);
    user.setFirstName("Badr");
    user.setLastName("Rami");
    user.setEmail("badr@example.com");
    user.setPhone("0612345678");
    user.setRole(Role.CLIENT);
    user.setStatus(UserStatus.ACTIVE);

    // 4. Préparer une liste d'utilisateurs
    java.util.List<UserResponse> users = java.util.List.of(user);

    // 5. Simuler le comportement du UserService
    when(userService.getAllUsers()).thenReturn(users);

    // 6. Exécuter le Handler
    usersHandler.handle(exchange);

    // 7. Vérifier que le service a été appelé une seule fois
    verify(userService, times(1)).getAllUsers();

    // 8. Vérifier le statut HTTP 200
    verify(exchange).sendResponseHeaders(eq(200), anyLong());

    // 9. Vérifier le contenu de la réponse JSON
    String response = responseBody.toString(StandardCharsets.UTF_8);

    assertTrue(response.contains("badr@example.com"));
    assertTrue(response.contains("Badr"));
    assertTrue(response.contains("Rami"));
}


@Test
void shouldReturnUserSuccessfully() throws IOException {

    // 1. Simuler une requête HTTP GET
    when(exchange.getRequestMethod()).thenReturn("GET");

    when(exchange.getRequestURI())
            .thenReturn(URI.create("/users/1"));

    // 2. Préparer un utilisateur fictif
    UserResponse user = new UserResponse();

    user.setId(1L);
    user.setFirstName("Badr");
    user.setLastName("Rami");
    user.setEmail("badr@example.com");
    user.setPhone("0612345678");
    user.setRole(Role.CLIENT);
    user.setStatus(UserStatus.ACTIVE);

    // 3. Simuler le comportement du UserService
    when(userService.getUserById(1L)).thenReturn(user);

    // 4. Exécuter le Handler
    usersHandler.handle(exchange);

    // 5. Vérifier que le service a été appelé
    verify(userService, times(1)).getUserById(1L);

    // 6. Vérifier le statut HTTP 200
    verify(exchange).sendResponseHeaders(eq(200), anyLong());

    // 7. Vérifier le contenu JSON
    String response = responseBody.toString(StandardCharsets.UTF_8);

    assertTrue(response.contains("badr@example.com"));
    assertTrue(response.contains("Badr"));
    assertTrue(response.contains("Rami"));
}
@Test
void shouldReturn404WhenUserDoesNotExist() throws IOException {

    when(exchange.getRequestMethod()).thenReturn("GET");
    when(exchange.getRequestURI()).thenReturn(URI.create("/users/999"));

    when(userService.getUserById(999L)).thenReturn(null);

    usersHandler.handle(exchange);

    verify(userService).getUserById(999L);
    verify(exchange).sendResponseHeaders(eq(404), anyLong());

    String response = responseBody.toString(StandardCharsets.UTF_8);

    assertTrue(response.contains("Utilisateur non trouvé"));
}


@Test
void shouldUpdateUserSuccessfully() throws IOException {

    // 1. Préparer le JSON envoyé par le client
    String jsonRequest = """
        {
            "firstName": "Badr",
            "lastName": "Rami",
            "email": "badr.updated@example.com",
            "phone": "0612345678"
        }
        """;

    // 2. Simuler la requête HTTP PUT
    when(exchange.getRequestMethod()).thenReturn("PUT");
    when(exchange.getRequestURI()).thenReturn(URI.create("/users/1"));

    when(exchange.getRequestBody()).thenReturn(
        new ByteArrayInputStream(
            jsonRequest.getBytes(StandardCharsets.UTF_8)
        )
    );

    // 3. Préparer la réponse simulée du service
    UserResponse mockResponse = new UserResponse();
    mockResponse.setId(1L);
    mockResponse.setFirstName("Badr");
    mockResponse.setLastName("Rami");
    mockResponse.setEmail("badr.updated@example.com");
    mockResponse.setPhone("0612345678");
    mockResponse.setRole(Role.CLIENT);
    mockResponse.setStatus(UserStatus.ACTIVE);

    when(userService.updateUser(
        eq(1L),
        any(UpdateUserRequest.class)
    )).thenReturn(mockResponse);

    // 4. Exécuter le Handler
    usersHandler.handle(exchange);

    // 5. Vérifier l'appel du service
    verify(userService).updateUser(
        eq(1L),
        any(UpdateUserRequest.class)
    );

    // 6. Vérifier le statut HTTP 200
    verify(exchange).sendResponseHeaders(eq(200), anyLong());

    // 7. Vérifier le contenu de la réponse
    String response = responseBody.toString(StandardCharsets.UTF_8);

    assertTrue(response.contains("badr.updated@example.com"));
    assertTrue(response.contains("Badr"));
    assertTrue(response.contains("Rami"));
}


@Test
void shouldReturn404WhenUpdatingNonExistingUser() throws IOException {

    // 1. Préparer le JSON
    String jsonRequest = """
        {
            "firstName": "Badr",
            "lastName": "Rami",
            "email": "badr.updated@example.com",
            "phone": "0612345678"
        }
        """;

    // 2. Simuler la requête PUT
    when(exchange.getRequestMethod()).thenReturn("PUT");
    when(exchange.getRequestURI()).thenReturn(URI.create("/users/999"));

    when(exchange.getRequestBody()).thenReturn(
        new ByteArrayInputStream(
            jsonRequest.getBytes(StandardCharsets.UTF_8)
        )
    );

    // 3. Simuler l'exception du service
    when(userService.updateUser(
        eq(999L),
        any(UpdateUserRequest.class)
    )).thenThrow(
        new IllegalArgumentException("Utilisateur non trouvé.")
    );

    // 4. Exécuter le Handler
    usersHandler.handle(exchange);

    // 5. Vérifier l'appel du service
    verify(userService).updateUser(
        eq(999L),
        any(UpdateUserRequest.class)
    );

    // 6. Vérifier le statut HTTP 404
    verify(exchange).sendResponseHeaders(eq(404), anyLong());

    // 7. Vérifier le message
    String response = responseBody.toString(StandardCharsets.UTF_8);

    assertTrue(response.contains("Utilisateur non trouvé."));
}



@Test
void shouldReturn400WhenJsonIsInvalidDuringUpdate() throws IOException {

    // 1. Préparer un JSON invalide (virgule manquante)
    String jsonRequest = """
        {
            "firstName": "Badr",
            "lastName": "Rami",
            "email": "badr.updated@example.com"
            "phone": "0612345678"
        }
        """;

    // 2. Simuler une requête HTTP PUT
    when(exchange.getRequestMethod()).thenReturn("PUT");
    when(exchange.getRequestURI()).thenReturn(URI.create("/users/1"));

    // 3. Simuler le corps de la requête
    when(exchange.getRequestBody()).thenReturn(
        new ByteArrayInputStream(
            jsonRequest.getBytes(StandardCharsets.UTF_8)
        )
    );

    // 4. Exécuter le handler
    usersHandler.handle(exchange);

    // 5. Vérifier le statut HTTP 400
    verify(exchange).sendResponseHeaders(eq(400), anyLong());

    // 6. Vérifier le message d'erreur
    String response = responseBody.toString(StandardCharsets.UTF_8);
    assertTrue(response.contains("JSON invalide"));

    // 7. Vérifier que le service n'a pas été appelé
    verify(userService, never()).updateUser(
        anyLong(), any(UpdateUserRequest.class)
    );
}


}
