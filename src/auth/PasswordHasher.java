package auth;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import util.Result;

public class PasswordHasher {

	private static final String HASH_ALGORITHM = "SHA-512";
    private static final int SALT_LENGTH = 16;

	public static String generateSalt() {

		byte[] salt = new byte[PasswordHasher.SALT_LENGTH];

		new SecureRandom().nextBytes(salt);

		return Base64.getEncoder().encodeToString(salt);
	}
	
	public static Result<String> hashPassword(String password, String salt) {

		return Result.of(() -> {
			MessageDigest md = MessageDigest.getInstance(PasswordHasher.HASH_ALGORITHM);

			md.update(salt.getBytes());

			return Base64.getEncoder().encodeToString(
				md.digest(password.getBytes())
			);
		});
	}

	public static Result<Boolean> verifyPassword(String password, String salt, String expectedHash) {

        return Result.of(() -> {
        	String hash = hashPassword(password, salt).orElseThrow();

        	return hash.equals(expectedHash);
        });
    }
}