package auth;

import java.time.LocalDateTime;

public record Session (
	String id,
	int userId,
	LocalDateTime expiresAt
) {

}