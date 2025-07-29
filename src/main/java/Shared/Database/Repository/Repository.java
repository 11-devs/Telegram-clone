package Shared.Database.Repository;

import Shared.Database.DAO.GenericDAO;

import java.util.List;

public abstract class Repository<T, ID> {
    protected final GenericDAO<T> primaryDAO;      // e.g., PostgreSQL
    protected final GenericDAO<T> cacheDAO;        // e.g., SQLite (optional cache)

    protected final Class<T> type;

    public Repository(Class<T> type, GenericDAO<T> primaryDAO, GenericDAO<T> cacheDAO) {
        this.type = type;
        this.primaryDAO = primaryDAO;
        this.cacheDAO = cacheDAO;
    }

    public void insert(T obj, ID id) {
        if (primaryDAO.findById(id) == null) {
            primaryDAO.insert(obj);
        }
        if (cacheDAO != null && cacheDAO.findById(id) == null) {
            cacheDAO.insert(obj);
        }
    }

    public T findById(ID id) {
        T obj = null;
        if (cacheDAO != null) {
            obj = cacheDAO.findById(id);
        }
        if (obj == null) {
            obj = primaryDAO.findById(id);
            if (obj != null && cacheDAO != null) {
                cacheDAO.insert(obj); // optional cache update
            }
        }
        return obj;
    }

    public List<T> findAll() {
        return primaryDAO.findAll();
    }

    public void update(T obj) {
        primaryDAO.update(obj);
        if (cacheDAO != null) {
            cacheDAO.update(obj);
        }
    }

    public void delete(T obj) {
        primaryDAO.delete(obj);
        if (cacheDAO != null) {
            cacheDAO.delete(obj);
        }
    }

    public void deleteById(ID id) {
        T obj = findById(id);
        if (obj != null) {
            delete(obj);
        }
    }

    public T findByField(String fieldName, Object value) {
        T obj = cacheDAO != null ? cacheDAO.findByField(fieldName, value) : null;
        if (obj == null) {
            obj = primaryDAO.findByField(fieldName, value);
            if (obj != null && cacheDAO != null) {
                cacheDAO.insert(obj);
            }
        }
        return obj;
    }

    public List<T> findAllByField(String fieldName, Object value) {
        return primaryDAO.findAllByField(fieldName, value); // skip cache for multi-results
    }
}
