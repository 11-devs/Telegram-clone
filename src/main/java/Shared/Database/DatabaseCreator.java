package Shared.Database;

import Shared.Utils.Console;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseCreator {
    private static final String DEFAULT_URL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String DEFAULT_USER = "postgres";
    private static final String DEFAULT_PASSWORD = "12345678";
    private static final String APP_USER = "postgres";
    private static final String APP_USER_PASSWORD = "12345678";

    public static void createDatabaseIfNotExists(String databaseName) throws SQLException {
        validateDatabaseName(databaseName);

        try (Connection conn = DriverManager.getConnection(DEFAULT_URL, DEFAULT_USER, DEFAULT_PASSWORD);
             Statement stmt = conn.createStatement()) {
            if (!databaseExists(stmt, databaseName)) {
                createDatabase(stmt, databaseName);
                Console.print("Database '" + databaseName + "' created successfully.", Console.Color.GREEN);
            } else {
                Console.print("Database '" + databaseName + "' already exists.", Console.Color.YELLOW);
            }
        } catch (SQLException e) {
            Console.error("Failed to create database: " + e.getMessage());
            throw new SQLException("Database creation failed", e);
        }
    }

    private static void validateDatabaseName(String dbName) {
        if (dbName == null || dbName.trim().isEmpty()) {
            throw new IllegalArgumentException("Database name cannot be null or empty");
        }
        if (!dbName.matches("^[a-zA-Z_][a-zA-Z0-9_]*$")) {
            throw new IllegalArgumentException("Invalid database name format");
        }
    }

    private static boolean databaseExists(Statement stmt, String dbName) throws SQLException {
        String checkDbSql = "SELECT 1 FROM pg_database WHERE datname = ?";
        try (var ps = stmt.getConnection().prepareStatement(checkDbSql)) {
            ps.setString(1, dbName);
            return ps.executeQuery().next();
        }
    }

    private static void createDatabase(Statement stmt, String dbName) throws SQLException {
        String createDbSql = String.format(
                "CREATE DATABASE %s WITH OWNER = %s ENCODING = 'UTF8' LC_COLLATE = 'en_US.UTF-8' " +
                        "LC_CTYPE = 'en_US.UTF-8' TEMPLATE = template0",
                dbName, APP_USER);

        stmt.executeUpdate(createDbSql);
    }
}