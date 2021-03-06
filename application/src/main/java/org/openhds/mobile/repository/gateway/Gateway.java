package org.openhds.mobile.repository.gateway;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.forms.FormContent;
import org.openhds.mobile.repository.Converter;
import org.openhds.mobile.repository.DataWrapper;
import org.openhds.mobile.repository.Query;
import org.openhds.mobile.repository.QueryResultsIterator;
import org.openhds.mobile.repository.ResultsIterator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.openhds.mobile.OpenHDS.Common.LAST_MODIFIED_CLIENT;
import static org.openhds.mobile.OpenHDS.Common.LAST_MODIFIED_SERVER;
import static org.openhds.mobile.OpenHDS.Common.UUID;
import static org.openhds.mobile.repository.RepositoryUtils.ASCENDING;
import static org.openhds.mobile.repository.RepositoryUtils.DESCENDING;
import static org.openhds.mobile.repository.RepositoryUtils.EQUALS;
import static org.openhds.mobile.repository.RepositoryUtils.GREATER_THAN_EQUALS;
import static org.openhds.mobile.repository.RepositoryUtils.LESS_THAN_EQUALS;
import static org.openhds.mobile.repository.RepositoryUtils.LIKE;
import static org.openhds.mobile.repository.RepositoryUtils.bulkInsert;
import static org.openhds.mobile.repository.RepositoryUtils.countRecords;
import static org.openhds.mobile.repository.RepositoryUtils.delete;
import static org.openhds.mobile.repository.RepositoryUtils.extractString;
import static org.openhds.mobile.repository.RepositoryUtils.insert;
import static org.openhds.mobile.repository.RepositoryUtils.update;
import static org.openhds.mobile.utilities.DateUtils.formatDateTimeIso;

/**
 * Supertype for database table Gateways.  Expose and implement query and CRUD operations,
 */
public abstract class Gateway<T> {
    protected final Uri tableUri;
    protected final String idColumnName;
    protected final Converter<T> converter;

    // subclass constructor must supply implementation details
    public Gateway(Uri tableUri, String idColumnName, Converter<T> converter) {
        this.tableUri = tableUri;
        this.idColumnName = idColumnName;
        this.converter = converter;
    }

    public Converter<T> getConverter() {
        return converter;
    }

    public abstract Set<String> getColumns();

    public String getIdColumnName() {
        return idColumnName;
    }

    // inserted/updated record, or null if failed
    public T insertOrUpdate(ContentResolver contentResolver, FormContent formContent) {
        ContentValues contentValues = toContentValues(formContent);
        return insertOrUpdate(contentResolver, contentValues);
    }

    // inserted/updated record, or null if failed
    public T insertOrUpdate(ContentResolver contentResolver, T entity) {
        ContentValues contentValues = toContentValues(entity);
        return insertOrUpdate(contentResolver, contentValues);
    }

    // inserted/updated record, or null if failed
    public T insertOrUpdate(ContentResolver contentResolver, ContentValues contentValues) {
        if (null == contentValues || !contentValues.containsKey(idColumnName)) {
            return null;
        }

        String id = contentValues.getAsString(idColumnName);
        if (null == id || id.trim().isEmpty()) {
            id = java.util.UUID.randomUUID().toString();
            contentValues.put(idColumnName, id);
        }

        if (exists(contentResolver, id)) {
            T entity = getFirst(contentResolver, findById(id));
            ContentValues existing = toContentValues(entity);
            addAllNotNull(existing, contentValues);
            update(contentResolver, tableUri, existing, idColumnName, id);
        } else {
            insert(contentResolver, tableUri, contentValues);
        }

        return getFirst(contentResolver, findById(id));
    }

    // put all non-null entries from second into the first
    private void addAllNotNull(ContentValues original, ContentValues other) {
        for (String key : other.keySet()) {
            String value = other.getAsString(key);
            if (null != value) {
                original.put(key, value);
            }
        }
    }

    // insert many entities, return number of new rows
    public int insertMany(ContentResolver contentResolver, List<T> entities) {
        ContentValues[] allContentValues = fromList(entities);
        return bulkInsert(contentResolver, tableUri, allContentValues);
    }

    // true if entity was deleted
    public boolean deleteById(ContentResolver contentResolver, String id){
        return delete(contentResolver, tableUri, idColumnName, id) > 0;
    }

    // clear the whole table and return rows deleted--be careful
    public int deleteAll(ContentResolver resolver) {
        return resolver.delete(tableUri, null, null);
    }

    // true if entity was found with given id
    public boolean exists(ContentResolver contentResolver, String id) {
        Query query = findById(id);
        return null != getFirst(contentResolver, query);
    }

    // count all the records in the table
    public int countAll(ContentResolver resolver) {
        return countRecords(resolver, tableUri);
    }

    // get the first result from a query as an entity or null
    public T getFirst(ContentResolver contentResolver, Query query) {
        Cursor cursor = query.select(contentResolver);
        return toEntity(cursor);
    }

    // get all results from a query as a list
    public List<T> getList(ContentResolver contentResolver, Query query) {
        Cursor cursor = query.select(contentResolver);
        return toList(cursor);
    }

    // get an iterator over all results from a query
    public Iterator<T> getIterator(ContentResolver contentResolver, Query query) {
        return new ResultsIterator<T>(contentResolver, query, converter);
    }

    // get an iterator over all results from a query, with given iterator window size
    public Iterator<T> getIterator(ContentResolver contentResolver, Query query, int windowSize) {
        return new ResultsIterator<T>(contentResolver, query, converter, windowSize);
    }

    // get the first result from a query as a QueryResult or null
    public DataWrapper getFirstDataWrapper(ContentResolver contentResolver, Query query, String state) {
        T entity = getFirst(contentResolver, query);
        if (null == entity) {
            return null;
        }
        return converter.toDataWrapper(contentResolver, entity, state);
    }

    // get all results from a query as a list of QueryResults
    public List<DataWrapper> getDataWrapperList(ContentResolver contentResolver, Query query, String level) {
        List<DataWrapper> dataWrappers = new ArrayList<DataWrapper>();
        Cursor cursor = query.select(contentResolver);
        List<T> entities = toList(cursor);
        for (T entity : entities) {
            dataWrappers.add(converter.toDataWrapper(contentResolver, entity, level));
        }
        return dataWrappers;
    }

    // get an iterator over all results from a query as DataWrapper
    public Iterator<DataWrapper> getDataWrapperIterator(ContentResolver contentResolver, Query query, String state) {
        return new QueryResultsIterator<T>(contentResolver, query, converter, state);
    }

    // get an iterator over all results from a query as DataWrapper, with given iterator window size
    public Iterator<DataWrapper> getDataWrapperIterator(ContentResolver contentResolver, Query query, String state, int windowSize) {
        return new QueryResultsIterator<T>(contentResolver, query, converter, state, windowSize);
    }

    // find entities with given id
    public Query findById(String id) {
        return new Query(tableUri, idColumnName, id, idColumnName);
    }

    // find entities ordered by id, might be huge
    public Query findAll() {
        return findAll(idColumnName);
    }

    // find entities ordered by given columnOrderBy
    public Query findAll(String columnOrderBy) {
        return new Query(tableUri, null, null, columnOrderBy);
    }

    // find entities where given columns equal corresponding values
    public Query findByCriteriaEqual(String[] columnNames, String[] columnValues, String columnOrderBy) {
        return new Query(tableUri, columnNames, columnValues, columnOrderBy, EQUALS);
    }

    // find entities where given columns are SQL "LIKE" corresponding value
    public Query findByCriteriaLike(String[] columnNames, String[] columnValues, String columnOrderBy) {
        return new Query(tableUri, columnNames, columnValues, columnOrderBy, LIKE);
    }

    // find records modified on the server between the given timestamps
    public Query findByServerModificationTimeBetween(String afterDate, String beforeDate) {
        return findByColumnValueBetween(LAST_MODIFIED_SERVER, afterDate, beforeDate);
    }

    // find records modified on the client between the given timestamps
    public Query findByClientModificationTimeBetween(String afterDate, String beforeDate) {
        return findByColumnValueBetween(LAST_MODIFIED_CLIENT, afterDate, beforeDate);
    }

    // find records where the given column has values in [min, max], inclusive.
    public Query findByColumnValueBetween(String columnName, String min, String max) {
        final String[] columnNames = {columnName, columnName};
        final String[] columnValues = {min, max};
        final String[] operators = {GREATER_THAN_EQUALS, LESS_THAN_EQUALS};
        return new Query(tableUri, columnNames, columnValues, columnName, operators);
    }

    // find the maximum server-side modification timestamp
    public String findLastServerModificationTime(ContentResolver contentResolver) {
        Query query = new Query(tableUri, null, null, LAST_MODIFIED_SERVER + " " + DESCENDING);
        Cursor cursor = query.selectRange(contentResolver, 0, 1);
        return toString(cursor, LAST_MODIFIED_SERVER);
    }

    // find the minimum server-side modification timestamp
    public String findFirstServerModificationTime(ContentResolver contentResolver) {
        Query query = new Query(tableUri, null, null, LAST_MODIFIED_SERVER + " " + ASCENDING);
        Cursor cursor = query.selectRange(contentResolver, 0, 1);
        return toString(cursor, LAST_MODIFIED_SERVER);
    }

    // find the maximum client-side modification timestamp
    public String findLastClientModificationTime(ContentResolver contentResolver) {
        Query query = new Query(tableUri, null, null, LAST_MODIFIED_CLIENT + " " + DESCENDING);
        Cursor cursor = query.selectRange(contentResolver, 0, 1);
        return toString(cursor, LAST_MODIFIED_CLIENT);
    }

    // find the minimum client-side modification timestamp
    public String findFirstClientModificationTime(ContentResolver contentResolver) {
        Query query = new Query(tableUri, null, null, LAST_MODIFIED_CLIENT + " " + ASCENDING);
        Cursor cursor = query.selectRange(contentResolver, 0, 1);
        return toString(cursor, OpenHDS.Common.LAST_MODIFIED_CLIENT);
    }

    // get a string from the first result
    protected String toString(Cursor cursor, String columnName) {
        if(!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }
        String result = extractString(cursor, columnName);
        cursor.close();
        return result;
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

    // read all entities into content values
    protected ContentValues[] fromList(List<T> entities) {
        List<ContentValues> allContentValues = new ArrayList<ContentValues>();
        for (T entity : entities) {
            allContentValues.add(toContentValues(entity));
        }
        return allContentValues.toArray(new ContentValues[allContentValues.size()]);
    }

    // convert entity to content values, and assign client-side modification time
    protected ContentValues toContentValues(T entity) {
        ContentValues contentValues = converter.toContentValues(entity);
        contentValues.put(LAST_MODIFIED_CLIENT, formatDateTimeIso(Calendar.getInstance()));
        return contentValues;
    }

    // convert form content to content values, assign client-side modification time, uuid if needed
    protected ContentValues toContentValues(FormContent formContent) {
        String alias = converter.getDefaultAlias();
        ContentValues contentValues = converter.toContentValues(formContent, alias);

        String uuid = formContent.getContentString(alias, UUID);
        if (null == uuid || uuid.isEmpty()) {
            uuid = java.util.UUID.randomUUID().toString();
        }
        contentValues.put(UUID, uuid);

        contentValues.put(LAST_MODIFIED_CLIENT, formatDateTimeIso(Calendar.getInstance()));

        return contentValues;
    }
}
