package org.openhds.mobile.repository;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;

import org.openhds.mobile.forms.FormContent;

/**
 * Convert an entity to database content values and convert a database cursor to an entity.
 */
public interface Converter<T> {

    T fromCursor(Cursor cursor);

    ContentValues toContentValues(T entity);

    ContentValues toContentValues(FormContent formContent, String entityAlias);

    String getDefaultAlias();

    String getId(T entity);

    String getClientModificationTime(T entity);

    DataWrapper toDataWrapper(ContentResolver contentResolver, T entity, String level);

}
