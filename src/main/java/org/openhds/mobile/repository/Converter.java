package org.openhds.mobile.repository;

import android.content.ContentValues;
import android.database.Cursor;

/**
 * Convert an entity to database content values and convert a database cursor to an entity.
 */
public interface Converter<T> {

    public T fromCursor(Cursor cursor);

    public ContentValues toContentValues(T entity);

}
