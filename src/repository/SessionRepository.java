package repository;

import java.sql.Timestamp;
import java.util.Optional;

import auth.Session;
import data.DatabaseConnection;
import util.Result;

public class SessionRepository {
	
	private final DatabaseConnection database;

	public SessionRepository(DatabaseConnection database) {

		this.database = database;
	}

	public Result<Optional<Session>> getSession(String id) {

		String sql =  """
            SELECT *
            FROM sessions
            WHERE id = ?
        """;

        return this.database.query(
        	sql, 
        	statement -> statement.setString(1, id),
        	resultSet -> {
        		return new Session(
        			resultSet.getString("id"),
        			resultSet.getInt("user_id"),
        			resultSet.getTimestamp("expiration_date").toLocalDateTime()
        		);
        	} 
        );
	}

	public Result<Void> saveSession(Session session) {

		String sql = """
			INSERT INTO sessions
			VALUES (?, ?, ?)
		""";

		return this.database.modify(
			sql, 
			statement -> {
				statement.setString(1, session.id());
				statement.setInt(2, session.userId());
				statement.setTimestamp(3, Timestamp.valueOf(session.expiresAt()));
			}
		).andThen((value) -> null);
	}

	public Result<Boolean> deleteSession(String id) {

		String sql = """
			DELETE FROM sessions
			WHERE id = ?
		""";

		return this.database.modify(
			sql, 
			statement -> {
				statement.setString(1, id);
			}
		);
	}
}