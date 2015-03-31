package com.ipeirotis.ofy;

import static com.ipeirotis.ofy.OfyService.ofy;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.cmd.Query;

public class OfyBaseDao<T> {

    protected Class<T> clazz;

    protected OfyBaseDao(Class<T> clazz) {
        this.clazz = clazz;
    }

    public Key<T> save(T entity) {
        return ofy().save().entity(entity).now();
    }

    public Map<Key<T>, T> save(List<T> entities) {
        return ofy().save().entities(entities).now();
    }

    public T saveAndGet(T entity) {
        Key<T> key = ofy().save().entity(entity).now();
        return ofy().load().key(key).now();
    }

    public void saveAll(Collection<T> entities) {
        ofy().save().entities(entities).now();
    }

    public void delete(T entity) {
        ofy().delete().entity(entity).now();
    }

    public void delete(List<T> entities) {
        ofy().delete().entities(entities).now();
    }

    public void delete(Long id) {
        ofy().delete().type(clazz).id(id).now();
    }

    public void delete(String id) {
        ofy().delete().type(clazz).id(id).now();
    }

    public void deleteAll(List<Key<T>> keys) {
        ofy().delete().keys(keys).now();
    }

    public T get(Long id) {
        return ofy().load().type(clazz).id(id).now();
    }

    public T safeGet(Long id) {
        return ofy().load().type(clazz).id(id).safe();
    }

    public T get(String id) {
        return ofy().load().type(clazz).id(id).now();
    }

    public T safeGet(String id) {
        return ofy().load().type(clazz).id(id).safe();
    }

    public T get(Key<T> key) {
        return ofy().load().key(key).now();
    }

    public T getByProperty(String propName, Object propValue) {
        return ofy().load().type(clazz).filter(propName, propValue).first().now();
    }

    public List<T> listByProperty(String propName, Object propValue) {
        return ofy().load().type(clazz).filter(propName, propValue).list();
    }

    public Query<T> query(int limit, String sortOrder) {
        return ofy().load().type(clazz).order(sortOrder).limit(limit);
    }

    public Query<T> query(Map<String, Object> params) {
        Query<T> q = ofy().load().type(clazz);
        if (params != null) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                q = q.filter(entry.getKey(), entry.getValue());
            }
        }
        return q;
    }

}
