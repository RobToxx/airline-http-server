package data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import org.postgresql.Driver;

import util.Result;

public class PostgreSQLConnection implements DatabaseConnection {

    private final Connection connection;

    private PostgreSQLConnection(String url, String user, String password) throws SQLException, ClassNotFoundException {

        this.connection = DriverManager.getConnection(url, user, password);
    }

    public static Result<DatabaseConnection> create(String url, String user, String password) {

        return Result.of(
            ()-> new PostgreSQLConnection(url, user, password)
        );
    }

    public <T> Result<Optional<T>> query(String query, StatementBuilder builder, ResultMapper<T> mapper) {

        return Result.of(() -> {

            PreparedStatement statement = connection.prepareStatement(query);

            builder.build(statement);

            ResultSet result = statement.executeQuery();

            if (!result.next()) {

                result.close();
                statement.close();

                return Optional.empty();
            }

            return Optional.of(mapper.map(result));
        });
    }

    public <T> Result<Boolean> modify(String command, StatementBuilder builder) {

        return Result.of(() -> {

            PreparedStatement statement = connection.prepareStatement(command);

            builder.build(statement);

            return statement.executeUpdate() >= 1;
        });
    }
}
