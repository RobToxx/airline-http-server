package auth;

public enum RegisterStatus {
    SUCCESS,
    INVALID_NAME,
    INVALID_EMAIL,
    INVALID_PASSWORD,
    EMAIL_EXISTS
}