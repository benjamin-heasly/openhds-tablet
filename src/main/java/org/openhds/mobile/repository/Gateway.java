package org.openhds.mobile.repository;

import android.net.Uri;

import java.util.List;

/**
 * Supertype for database table Gateways.  Expose entity-specific query and CRUD operations,
 */
public abstract class Gateway<T> {
    protected final Uri tableUri;
    protected final Converter<T> converter;

    public Gateway(Uri tableUri, Converter<T> converter) {
        this.tableUri = tableUri;
        this.converter = converter;
    }

    public abstract boolean insertOrUpdate();
    public abstract boolean deleteById(String id);
    public abstract boolean exists(String id);
    public abstract T findById(String id);
    public abstract List<T> findAll();
    public abstract List<T> findByCriteriaEqual(String[] columnNames, String[] columnValues);
    public abstract List<T> findByCriteriaLike(String[] columnNames, String[] columnValues);
}
