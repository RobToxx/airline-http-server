package repository;

import java.util.Optional;

import auth.User;
import data.DatabaseConnection;
import util.Result;

public class UserDAO {
	
	private DatabaseConnection database;

	public UserDAO(DatabaseConnection database) {

		this.database = database;
	}

	public Result<Optional<User>> get(int id) {

		String sql = """
			SELECT * 
			FROM users
			WHERE id = ?
		""";

		return this.database.query(
			sql, 
			statement -> statement.setInt(1, id), 
			resultSet -> {
				return new User(
					resultSet.getInt("id"),
					resultSet.getString("name"),
					resultSet.getString("email"),
					resultSet.getString("password"),
					resultSet.getString("salt")
				);
			}
		);
	}

	public Result<Boolean> create(User user) {

		String sql = """
			INSERT INTO users
			VALUES (DEFAULT, ?, ?, ?, ?)
		""";

		return this.database.modify(
			sql, 
			statement -> {
				statement.setString(1, user.name());
				statement.setString(2, user.email());
				statement.setString(3, user.password());
				statement.setString(4, user.salt());
			}
		);
	}

	public Result<Optional<User>> findByEmail(String email) {

		String sql = """
			SELECT * 
			FROM users
			WHERE email = ?
		""";

		return this.database.query(
			sql, 
			statement -> statement.setString(1, email), 
			resultSet -> {
				return new User(
					resultSet.getInt("id"),
					resultSet.getString("name"),
					resultSet.getString("email"),
					resultSet.getString("password"),
					resultSet.getString("salt")
				);
			}
		);
	}
}