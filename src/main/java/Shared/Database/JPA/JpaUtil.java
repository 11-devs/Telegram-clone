package Shared.Database.JPA;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class JpaUtil {
    private static String PERSISTENCE_UNIT_NAME = "restaurantPU";
    private static EntityManagerFactory emf;

    public void setPersistenceUnitName(String persistenceUnitName) {
        PERSISTENCE_UNIT_NAME = persistenceUnitName;
    }

    public static EntityManagerFactory getEntityManagerFactory() {
        if (emf == null) {
            emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
        }
        return emf;
    }

    public static EntityManager getEntityManager() {
        return getEntityManagerFactory().createEntityManager();
    }

    public static void close() {
        if (emf != null) {
            emf.close();
        }
    }
}
