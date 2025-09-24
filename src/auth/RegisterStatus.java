package auth;

public enum RegisterStatus {
    SUCCESS(""),
    INVALID_NAME("El nombre no puede estar vacío."),
    INVALID_EMAIL("El correo no tiene un formato válido."),
    INVALID_PASSWORD("La contraseña debe tener al menos 8 caracteres, incluir mayúscula, minúscula y un número."),
    EMAIL_EXISTS("El correo ya está registrado.");

    public final String message;

    RegisterStatus(String message) {

        this.message = message;
    }
}