package Shared.Database;

import Shared.Database.XMLManager.PersistenceXmlReader;
import Shared.Utils.Console;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.Query; // Import the Query class
import java.util.List; // Import the List class

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
     * Retrieves and prints all table names from the 'public' schema of the connected database.
     * This method handles its own EntityManager and transaction to ensure it is self-contained.
     */
    public void printAllTableNames() {
        EntityManager em = null;
        try {
            // Get a new EntityManager instance for this operation.
            em = getEntityManager();

            // Start a transaction. While not always required for read-only queries,
            // it's good practice and some JPA providers or JDBC drivers might need it.
            em.getTransaction().begin();

            // Create a native SQL query to select table names from the database's metadata schema.
            // This query is standard for PostgreSQL and many other SQL databases.
            Query query = em.createNativeQuery(
                    "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public' ORDER BY table_name"
            );

            // Execute the query and cast the result to a List of Strings.
            @SuppressWarnings("unchecked")
            List<String> tableNames = query.getResultList();

            // Commit the transaction.
            em.getTransaction().commit();

            // Print the results to the console.
            Console.print("\nTables in database '" + databaseName + "':", Console.Color.BLUE);
            if (tableNames.isEmpty()) {
                Console.log("No tables found in the public schema.");
            } else {
                for (String tableName : tableNames) {
                    Console.log("- " + tableName);
                }
            }
            System.out.println(); // Add a blank line for better formatting.

        } catch (Exception e) {
            Console.error("Failed to retrieve table names: " + e.getMessage());
            // If an error occurs, roll back the transaction if it is active.
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
        } finally {
            // Crucially, always close the EntityManager to release database connections and resources.
            if (em != null) {
                em.close();
            }
        }
    }
}