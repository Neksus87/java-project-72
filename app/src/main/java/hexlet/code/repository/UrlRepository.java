package hexlet.code.repository;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import hexlet.code.model.Url;

import java.sql.Timestamp;

public class UrlRepository extends BaseRepository {
    public static void save(Url url) throws SQLException {
        var sql = "INSERT INTO urls (name, created_at) VALUES (?, ?)";

        try (
                var conn = dataSource.getConnection();
                var preparedStatement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
        ) {
            // Устанавливаем текущую дату и время
            Timestamp now = new Timestamp(System.currentTimeMillis());
            url.setCreatedAt(now);

            preparedStatement.setString(1, url.getName());
            preparedStatement.setTimestamp(2, now);
            preparedStatement.executeUpdate();

            var generatedKeys = preparedStatement.getGeneratedKeys();

            if (generatedKeys.next()) {
                url.setId(generatedKeys.getLong(1));
            } else {
                throw new SQLException("DB have not returned an id after saving an entity");
            }
        }
    }

    public static Map<Long, Url> getEntities() throws SQLException {
        var sql = "SELECT * FROM urls ORDER BY id ASC"; // Явная сортировка по ID

        try (
                var conn = dataSource.getConnection();
                var stmt = conn.prepareStatement(sql)
        ) {
            var resultSet = stmt.executeQuery();
            var result = new HashMap<Long, Url>();

            while (resultSet.next()) {
                var id = resultSet.getLong("id");
                var name = resultSet.getString("name");
                var createdAt = resultSet.getTimestamp("created_at");
                var url = new Url(name);
                url.setId(id);
                url.setCreatedAt(createdAt);
                result.put(id, url); // Добавляем в мапу с ключом id
            }

            return result;
        }
    }

    public static Optional<Url> find(Long id) throws SQLException {
        var sql = "SELECT * FROM urls WHERE id = ?";

        try (
                var conn = dataSource.getConnection();
                var stmt = conn.prepareStatement(sql)
        ) {
            stmt.setLong(1, id);
            var resultSet = stmt.executeQuery();

            if (resultSet.next()) {
                var name = resultSet.getString("name");
                var createdAt = resultSet.getTimestamp("created_at");
                var url = new Url(name);
                url.setId(id);
                url.setCreatedAt(createdAt);
                return Optional.of(url);
            }

            return Optional.empty();
        }
    }

    public static boolean existsByName(String name) throws SQLException {
        var sql = "SELECT * FROM urls WHERE name = ?";

        try (
                var conn = dataSource.getConnection();
                var stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, name);
            var resultSet = stmt.executeQuery();

            return resultSet.next();
        }
    }
}
