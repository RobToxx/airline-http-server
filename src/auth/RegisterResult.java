package auth;

public enum RegisterResult {
    SUCCESS,
    INVALID_NAME,
    INVALID_EMAIL,
    INVALID_PASSWORD,
    EMAIL_EXISTS
}