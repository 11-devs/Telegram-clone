package Shared.Database;

import Shared.Database.XMLManager.PersistenceXmlReader;
import Shared.Utils.Console;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLiteDatabase {
    private static final String PERSISTENCE_UNIT_NAME = "TelegramCloneSQLite";
    String databaseName;
    private final EntityManagerFactory emf;

    public SQLiteDatabase() {
        try {
            databaseName = PersistenceXmlReader.getDatabaseNameFromPersistenceXml(PERSISTENCE_UNIT_NAME , "persistence.xml");
            SQLiteDatabaseCreator.createDatabaseIfNotExists(databaseName);
        } catch (Exception e) {
            Console.error("Failed to create or open SQLite database: " + databaseName + ": " + e.getMessage());
        }
        emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
        Console.print("EntityManagerFactory created for persistence unit: " + PERSISTENCE_UNIT_NAME, Console.Color.GREEN);
    }

    public EntityManager getEntityManager() {
        Console.log("Creating new EntityManager");
        return emf.createEntityManager();
    }

    // Inner static class to handle SQLite DB creation
    private static class SQLiteDatabaseCreator {
        private static final String SQLITE_JDBC_PREFIX = "jdbc:sqlite:";

        public static void createDatabaseIfNotExists(String databaseFileName) {
            String dbUrl = SQLITE_JDBC_PREFIX + databaseFileName;
            try (Connection conn = DriverManager.getConnection(dbUrl)) {
                if (conn != null) {
                    Console.print("SQLite database file ready: " + databaseFileName, Console.Color.GREEN);
                }
            } catch (SQLException e) {
                Console.error("Failed to create or connect to SQLite database file: " + e.getMessage());
            }
        }
    }
}
