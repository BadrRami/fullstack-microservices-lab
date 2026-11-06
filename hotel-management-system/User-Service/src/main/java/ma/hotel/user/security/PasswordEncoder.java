package ma.hotel.user.security;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordEncoder {

    // Hasher un mot de passe
    public String encode(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt(12));
    }

    // Vérifier un mot de passe
    public boolean matches(String rawPassword, String encodedPassword) {
        return BCrypt.checkpw(rawPassword, encodedPassword);
    }
}