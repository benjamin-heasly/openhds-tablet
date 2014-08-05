package org.openhds.mobile.repository;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

/**
 * Utilities to build queries and facilitate CRUD operations.
 *
 * BSH
 */
public class RepositoryUtils {
    public static final String EQUALS = "=";
    public static final String LIKE = "LIKE";

    private static final String AND = "AND";
    private static final String LIMIT = "LIMIT";
    private static final String OFFSET = "OFFSET";
    private static final String WHERE_PLACEHOLDER = "?";

    public static Uri insert(ContentResolver contentResolver, Uri tableUri, ContentValues contentValues) {
        return contentResolver.insert(tableUri, contentValues);
    }

    public static int update(ContentResolver contentResolver, Uri tableUri, ContentValues contentValues,
                             String columnName, String columnValue) {

        final String[] columnNames = {columnName};
        final String[] columnValues = {columnValue};
        final String whereStatement = buildWhereStatement(columnNames, EQUALS);

        return contentResolver.update(tableUri, contentValues, whereStatement, columnValues);
    }

    public static Cursor query(ContentResolver contentResolver, Uri tableUri,
                               String whereStatement, String[] columnValues, String columnSortBy) {

        return contentResolver.query(tableUri, null, whereStatement, columnValues, columnSortBy);
    }

    public static int delete(ContentResolver contentResolver, Uri tableUri, String columnName, String columnValue) {

        final String[] columnNames = {columnName};
        final String[] columnValues = {columnValue};
        final String whereStatement = buildWhereStatement(columnNames, EQUALS);

        return contentResolver.delete(tableUri, whereStatement, columnValues);
    }

    public static String buildWhereStatement(String[] columnNames, String operator) {
        if (null == columnNames || 0 == columnNames.length) {
            return null;
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(buildWhereClause(columnNames[0], operator));

        if (1 == columnNames.length) {
            return stringBuilder.toString();
        }

        for (int i = 2; i < columnNames.length; i++) {
            stringBuilder.append(" " + AND + " ");
            stringBuilder.append(buildWhereClause(columnNames[i], operator));
        }

        return stringBuilder.toString();
    }

    private static String buildWhereClause(String columnName, String operator) {
        return columnName + " " + operator + " " + WHERE_PLACEHOLDER;
    }

    public static String buildWhereRangedStatement(String[] columnNames, String operator, int start, int maxResults) {
        final String whereStatement = buildWhereStatement(columnNames, operator);
        return whereStatement + " " + LIMIT + " " + start + " " + OFFSET + " " + maxResults;
    }

    public static String extractString(Cursor cursor, String columnName) {
        int columnIndex = cursor.getColumnIndex(columnName);
        if (columnIndex < 0) {
            return null;
        }
        return cursor.getString(columnIndex);
    }
}

