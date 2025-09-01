package Shared.Database;

import Shared.Database.XMLManager.PersistenceXmlReader;
import Shared.Utils.Console;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.Query;
import java.util.List;

public class Database {
    private static final String PERSISTENCE_UNIT_NAME = "telegramclonePU";
    String databaseName;

    private final EntityManagerFactory emf;

    public Database() {
        try {
            databaseName = PersistenceXmlReader.getDatabaseNameFromPersistenceXml(PERSISTENCE_UNIT_NAME , "persistence.xml");
            DatabaseCreator.createDatabaseIfNotExists(databaseName);
        } catch (Exception e) {
            Console.error("Failed to create database: " + databaseName + ": " + e.getMessage());
        }
        emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
        Console.print("emf created: " , Console.Color.GREEN);
    }

    public EntityManager getEntityManager() {
        Console.log("getEntityManager");
        return emf.createEntityManager();
    }

    /**
     * Deletes all data from all tables in the 'public' schema without dropping the tables.
     * It uses the TRUNCATE command for efficiency and cascades to handle foreign keys,
     * while also resetting auto-incrementing primary key sequences.
     */
    public void clearAllTables() {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();

            // Get all table names from the helper method.
            List<String> tableNames = getPublicTableNames(em);

            if (tableNames.isEmpty()) {
                Console.log("No tables found to clear.");
                em.getTransaction().commit(); // Commit the empty transaction and return.
                return;
            }

            // Create a single, powerful TRUNCATE command for PostgreSQL.
            // - TRUNCATE is faster than DELETE.
            // - RESTART IDENTITY resets auto-incrementing keys (e.g., for 'id' columns).
            // - CASCADE automatically truncates tables with foreign key dependencies.
            String truncateQuery = "TRUNCATE TABLE " + String.join(", ", tableNames) + " RESTART IDENTITY CASCADE";
            em.createNativeQuery(truncateQuery).executeUpdate();

            em.getTransaction().commit();
            Console.print("\nAll table data has been successfully cleared.", Console.Color.YELLOW);

        } catch (Exception e) {
            Console.error("Failed to clear tables: " + e.getMessage());
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    /**
     * Retrieves and prints all table names from the 'public' schema of the connected database.
     * This method handles its own EntityManager to ensure it is self-contained.
     */
    public void printAllTableNames() {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin(); // Transaction is good practice even for reads

            // Call the helper method to get the list of tables
            List<String> tableNames = getPublicTableNames(em);

            em.getTransaction().commit();

            Console.print("\nTables in database '" + databaseName + "':", Console.Color.BLUE);
            if (tableNames.isEmpty()) {
                Console.log("No tables found in the public schema.");
            } else {
                tableNames.forEach(tableName -> Console.log("- " + tableName));
            }
            System.out.println();

        } catch (Exception e) {
            Console.error("Failed to retrieve table names: " + e.getMessage());
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    /**
     * Private helper method to get a list of all table names in the 'public' schema.
     * @param em The EntityManager to use for the query.
     * @return A List of table name strings.
     */
    private List<String> getPublicTableNames(EntityManager em) {
        Query query = em.createNativeQuery(
                "SELECT table_name FROM information_schema.tables " +
                        "WHERE table_schema = 'public' AND table_type = 'BASE TABLE' ORDER BY table_name"
        );
        @SuppressWarnings("unchecked")
        List<String> tableNames = query.getResultList();
        return tableNames;
    }
}