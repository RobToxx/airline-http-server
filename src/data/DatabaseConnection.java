package data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import util.Result;

public interface DatabaseConnection {
    
    public interface StatementBuilder {

        void build(PreparedStatement statement) throws SQLException;
    }

    interface ResultMapper<T> {
        T map(ResultSet resultSet) throws SQLException;
    }

    <T> Result<Optional<T>> query(String query, StatementBuilder builder, ResultMapper<T> mapper);

    <T> Result<Boolean> modify(String command, StatementBuilder builder);
}
