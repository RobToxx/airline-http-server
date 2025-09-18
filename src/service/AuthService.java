package service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import auth.PasswordHasher;
import auth.RegisterResult;
import auth.Session;
import auth.User;
import repository.SessionRepository;
import repository.UserDAO;
import util.Result;

public class AuthService {
	
	private UserDAO userDAO;
	private SessionRepository sessionRepository;

	public AuthService(UserDAO userDAO, SessionRepository sessionRepository) {

		this.userDAO = userDAO;
		this.sessionRepository = sessionRepository;
	}

	public Result<Optional<Session>> login(String email, String password) {

		return Result.of(() -> {

			Optional<User> userOpt = userDAO.findByEmail(email).orElseThrow();

			if (userOpt.isEmpty()) return Optional.empty();

			User user = userOpt.get();

			if (!PasswordHasher.verifyPassword(
				password, 
				user.salt(), 
				user.password()
			).expect()) return Optional.empty();

			Session session = new Session(
				UUID.randomUUID().toString(),
				user.id(),
				LocalDateTime.now().plusHours(2)
			);

			sessionRepository.saveSession(session);

			return Optional.of(
				session
			);
		});
	}

	public Result<Boolean> logout(String sessionId) {

		return sessionRepository.deleteSession(sessionId);
	}

	public Result<Optional<User>> validateSession(String sessionId) {

		return Result.of(() -> {
			Optional<Session> sessionOpt = sessionRepository.getSession(sessionId).orElseThrow();

			if (sessionOpt.isEmpty()) return Optional.empty();

			Session session = sessionOpt.get();

			if (session.expiresAt().isBefore(LocalDateTime.now())) {

				sessionRepository.deleteSession(sessionId);

				return Optional.empty();
			}

			return userDAO.get(session.userId()).orElseThrow();
		});
	}

	public Result<RegisterResult> register(String name, String email, String password) {

		return Result.of(() -> {

			if (name == null || name.isBlank()) {
				
				return RegisterResult.INVALID_NAME;
			}

			if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
				return RegisterResult.INVALID_EMAIL;
			}

			if (password == null || !password.matches("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).{8,}$")) {
				return RegisterResult.INVALID_PASSWORD;
			}

			String salt = PasswordHasher.generateSalt();

			User user = new User(
				1,
				name,
				email,
				PasswordHasher.hashPassword(password, salt).expect(),
				salt
			);

			if (userDAO.create(user) instanceof Result.Failure) {

				return RegisterResult.EMAIL_EXISTS;
			}

			return RegisterResult.SUCCESS;
		});
	}
}