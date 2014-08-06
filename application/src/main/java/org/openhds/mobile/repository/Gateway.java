package org.openhds.mobile.repository;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

import static org.openhds.mobile.repository.RepositoryUtils.EQUALS;
import static org.openhds.mobile.repository.RepositoryUtils.LIKE;
import static org.openhds.mobile.repository.RepositoryUtils.insert;
import static org.openhds.mobile.repository.RepositoryUtils.update;
import static org.openhds.mobile.repository.RepositoryUtils.delete;


/**
 * Supertype for database table Gateways.  Expose and implement query and CRUD operations,
 */
public abstract class Gateway<T> {
    protected final Uri tableUri;
    protected final String idColumnName;
    protected Converter<T> converter;

    // subclass must supply implementation details
    public Gateway(Uri tableUri, String idColumnName, Converter<T> converter) {
        this.tableUri = tableUri;
        this.idColumnName = idColumnName;
        this.converter = converter;
    }

    public boolean insertOrUpdate(ContentResolver contentResolver, T entity) {
        ContentValues contentValues = converter.toContentValues(entity);
        String id = converter.getId(entity);
        if (exists(contentResolver, id)) {
            update(contentResolver, tableUri, contentValues, idColumnName, id);
            return false;
        } else {
            return null != insert(contentResolver, tableUri, contentValues);
        }
    }

    public boolean deleteById(ContentResolver contentResolver, String id){
        return delete(contentResolver, tableUri, idColumnName, id) > 0;
    }

    public boolean exists(ContentResolver contentResolver, String id) {
        return null != findById(contentResolver, id);
    }

    public T findById(ContentResolver contentResolver, String id) {
        Query query = new Query(tableUri, idColumnName, id);
        Cursor cursor = query.select(contentResolver);
        return toEntity(cursor);
    }

    public List<T> findAll(ContentResolver contentResolver) {
        Query query = new Query(tableUri, null, null);
        Cursor cursor = query.select(contentResolver);
        return toList(cursor);
    }

    public List<T> findByCriteriaEqual(ContentResolver contentResolver, String[] columnNames, String[] columnValues, String columnOrderBy) {
        Query query = new Query(tableUri, columnNames, columnValues, columnOrderBy, EQUALS);
        Cursor cursor = query.select(contentResolver);
        return toList(cursor);
    }

    public List<T> findByCriteriaLike(ContentResolver contentResolver, String[] columnNames, String[] columnValues, String columnOrderBy) {
        Query query = new Query(tableUri, columnNames, columnValues, columnOrderBy, LIKE);
        Cursor cursor = query.select(contentResolver);
        return toList(cursor);
    }

    // convert first result and close cursor
    protected T toEntity(Cursor cursor) {
        if(!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }
        T entity = converter.fromCursor(cursor);
        cursor.close();
        return entity;
    }

    // convert all results and close cursor
    protected List<T> toList(Cursor cursor) {
        List<T> list = new ArrayList<T>();
        while(cursor.moveToNext()) {
            list.add(converter.fromCursor(cursor));
        }
        cursor.close();
        return list;
    }
}
