package Shared.Database;
import Shared.Database.XMLManager.PersistenceXmlReader;
import Shared.Utils.Console;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

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
}
