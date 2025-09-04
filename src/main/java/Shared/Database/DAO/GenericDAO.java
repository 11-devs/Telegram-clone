package Shared.Database.DAO;

import Shared.Utils.Console;
import jakarta.persistence.EntityManager;
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

    public void update(T obj) {
        executeTransaction(em -> em.merge(obj));
    }

    public void delete(T obj) {
        // todo : implemet soft delete
        executeTransaction(em -> {
            T entityToRemove = obj;
            if (!em.contains(entityToRemove)) {
                entityToRemove = em.merge(entityToRemove);
            }
            em.remove(entityToRemove);
        });
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
