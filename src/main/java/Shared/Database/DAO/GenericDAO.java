package Shared.Database.DAO;

import Shared.Models.BaseEntity;
import Shared.Utils.Console;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.function.Consumer;

public class GenericDAO<T> {
    private final Class<T> type;
    final EntityManager entityManager;

    public GenericDAO(Class<T> type, EntityManager entityManager) {
        this.type = type;
        this.entityManager = entityManager;
        Console.log("GenericDAO for entity: " + type.getName());
    }

    private void executeTransaction(Consumer<EntityManager> action) {
        try {
            entityManager.getTransaction().begin();
            action.accept(entityManager);
            entityManager.getTransaction().commit();
        } catch (RuntimeException e) {
            entityManager.getTransaction().rollback();
            Console.error(e.getMessage());
        }
    }

    public void insert(T obj) {
        // todo : handle dublicates
        executeTransaction(em -> em.persist(obj));
    }

    public T findById(Object id) {
        return entityManager.find(type, id);
    }

    public List<T> findAll() {
        String jpql = "SELECT e FROM " + type.getSimpleName() + " e";
        TypedQuery<T> query = entityManager.createQuery(jpql, type);
        return query.getResultList();
    }


    /**
     * Executes a JPQL query to find multiple entities.
     * @param jpql The JPQL query string.
     * @param queryConsumer A consumer to set parameters on the query.
     * @return A list of entities matching the query.
     */
    public List<T> findByJpql(String jpql, Consumer<TypedQuery<T>> queryConsumer) {
        TypedQuery<T> query = entityManager.createQuery(jpql, type);
        if (queryConsumer != null) {
            queryConsumer.accept(query);
        }
        return query.getResultList();
    }

    /**
     * Executes a JPQL query to find a single entity safely.
     * This is safer than getSingleResult() as it returns null instead of throwing an exception if not found.
     * @param jpql The JPQL query string.
     * @param queryConsumer A consumer to set parameters on the query.
     * @return The single entity, or null if not found.
     */
    public T findOneByJpql(String jpql, Consumer<TypedQuery<T>> queryConsumer) {
        TypedQuery<T> query = entityManager.createQuery(jpql, type);
        if (queryConsumer != null) {
            queryConsumer.accept(query);
        }
        query.setMaxResults(1); // Ensure we only fetch one result
        List<T> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * Overload for findByJpql without parameters.
     */
    public List<T> findByJpql(String jpql) {
        return findByJpql(jpql, null);
    }

    /**
     * Overload for findOneByJpql without parameters.
     */
    public T findOneByJpql(String jpql) {
        return findOneByJpql(jpql, null);
    }

    /**
     * Executes a JPQL COUNT query for high-performance counting.
     * @param jpql The JPQL query string (e.g., "SELECT COUNT(e) FROM Entity e WHERE ...").
     * @param queryConsumer A consumer to set parameters on the query.
     * @return The number of matching records.
     */
    public long countByJpql(String jpql, Consumer<TypedQuery<Long>> queryConsumer) {
        TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class);
        if (queryConsumer != null) {
            queryConsumer.accept(query);
        }
        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            return 0L;
        }
    }

    public void update(T obj) {
        executeTransaction(em -> em.merge(obj));
    }

    public void delete(T obj) {
        if (obj instanceof BaseEntity entity) {
            executeTransaction(em -> {
                entity.setIsDeleted(true);
                em.merge(entity);
            });
        } else {
            // Fallback to hard delete for any non-BaseEntity types
            executeTransaction(em -> {
                T entityToRemove = obj;
                if (!em.contains(entityToRemove)) {
                    entityToRemove = em.merge(entityToRemove);
                }
                em.remove(entityToRemove);
            });
        }
    }

    public void deleteById(Object id) {
        T entity = findById(id);
        if (entity != null) {
            delete(entity);
        }
    }
    public void save(T obj) {
        executeTransaction(em -> em.merge(obj));
    }
    public T findByField(String fieldName, Object value) {
        String jpql = "SELECT e FROM " + type.getSimpleName() + " e WHERE e." + fieldName + " = :value";
        TypedQuery<T> query = entityManager.createQuery(jpql, type);
        query.setParameter("value", value);
        List<T> results = query.setMaxResults(1).getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    public List<T> findAllByField(String fieldName, Object value) {
        String jpql = "SELECT e FROM " + type.getSimpleName() + " e WHERE e." + fieldName + " = :value";
        TypedQuery<T> query = entityManager.createQuery(jpql, type);
        query.setParameter("value", value);
        return query.getResultList();
    }

}