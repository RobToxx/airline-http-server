package data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import util.Result;

public class OracleConnection implements DatabaseConnection {

    private final Connection connection;

    private OracleConnection(String url, String user, String password) throws SQLException, ClassNotFoundException {

        Class.forName("org.oracle.Driver");

        this.connection = DriverManager.getConnection(url, user, password);
    }

    public static Result<DatabaseConnection> create(String url, String user, String password) {

        return Result.of(
            ()-> new OracleConnection(url, user, password)
        );
    }

    public <T> Result<Optional<T>> query(String query, StatementBuilder builder, ResultMapper<T> mapper) {

        return Result.of(()->{

            PreparedStatement statement = connection.prepareStatement(query);

            builder.build(statement);

            ResultSet result = statement.executeQuery();

            if (!result.first()) {

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
