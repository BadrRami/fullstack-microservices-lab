package ma.hotel.user.model;

public enum UserStatus {
    ACTIVE, // Le compte fonctionne normalement.
    INACTIVE, // Le compte est désactivé ou désactivé à la demande de l'utilisateur.
    BLOCKED // Le compte est bloqué par l'administration pour une raison particulière.
}