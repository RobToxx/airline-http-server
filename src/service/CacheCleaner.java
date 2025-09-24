package service;

import data.DatabaseConnection;

public class CacheCleaner {

	private DatabaseConnection database;

	public CacheCleaner(DatabaseConnection database) {

		this.database = database;
	}

	public void clean() {

		clearSessions();
		clearReservations();
	}

	private void clearSessions() {

		String sql = """
			DELETE FROM sessions 
			WHERE expiration_date < now()
		""";

		this.database.modify(
			sql, 
			s -> {}
		);
	}

	private void clearReservations() {

		String sql = """
			DELETE FROM reservations 
			WHERE expiration_date < now()
		""";

		this.database.modify(
			sql, 
			s -> {}
		);
	}
}