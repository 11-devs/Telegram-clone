package Shared.Database;

import Shared.Utils.Console;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * Manages the lifecycle of the JPA EntityManagerFactory for the client-side SQLite database.
 * This class should be instantiated once when the client application starts.
 */
public class SQLiteDatabase {
    private static final String PERSISTENCE_UNIT_NAME = "TelegramCloneSQLite";
    private final EntityManagerFactory emf;

    /**
     * Initializes the connection to the SQLite database by creating the EntityManagerFactory.
     * This single step reads persistence.xml, creates the database file if it doesn't exist,
     * and prepares the connection pool.
     */
    public SQLiteDatabase() {
        try {
            // This single line does everything your original code was trying to do manually.
            // 1. It finds and parses persistence.xml.
            // 2. It reads the "jakarta.persistence.jdbc.url" property.
            // 3. It connects to the database, automatically creating the .db file and directories if they don't exist.
            this.emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
            Console.print("EntityManagerFactory created successfully for persistence unit: " + PERSISTENCE_UNIT_NAME);
        } catch (Exception e) {
            // If this fails, the application cannot continue.
            Console.error("FATAL: Failed to create EntityManagerFactory for " + PERSISTENCE_UNIT_NAME);
            e.printStackTrace();
            throw new RuntimeException("Could not initialize the database.", e);
        }
    }

    /**
     * Creates a new, short-lived EntityManager for a single transaction or unit of work.
     * @return A new EntityManager instance.
     */
    public EntityManager getEntityManager() {
        if (emf == null || !emf.isOpen()) {
            throw new IllegalStateException("EntityManagerFactory is closed or was not initialized.");
        }
        return emf.createEntityManager();
    }

    /**
     * Closes the EntityManagerFactory and releases all database resources.
     * This method MUST be called when the application is shutting down.
     */
    public void close() {
        if (emf != null && emf.isOpen()) {
            emf.close();
            Console.print("EntityManagerFactory has been closed.");
        }
    }
}