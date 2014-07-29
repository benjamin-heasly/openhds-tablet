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
    public static final String OPERATOR_EQUALS = "=";
    public static final String OPERATOR_LIKE = "LIKE";
    public static final String WHERE_AND = "AND";
    public static final String WHERE_PLACEHOLDER = "?";
    public static final String WHERE_TRUE = "1";


    public static Uri insert(ContentResolver contentResolver, Uri tableUri, ContentValues contentValues) {
        return contentResolver.insert(tableUri, contentValues);
    }

    public static Cursor query(ContentResolver contentResolver, Uri tableUri,
                               String whereClause, String[] columnValues, String columnSortBy) {

        return contentResolver.query(tableUri, null, whereClause, columnValues, columnSortBy);
    }

    public static int update(ContentResolver contentResolver, Uri tableUri, ContentValues contentValues,
                             String columnName, String columnValue) {

        final String[] columnNames = {columnName};
        final String[] columnValues = {columnValue};
        final String whereStatement = buildWhereStatement(columnNames, OPERATOR_EQUALS);

        return contentResolver.update(tableUri, contentValues, whereStatement, columnValues);
    }

    public static int delete(ContentResolver contentResolver, Uri tableUri, String columnName, String columnValue) {

        final String[] columnNames = {columnName};
        final String[] columnValues = {columnValue};
        final String whereStatement = buildWhereStatement(columnNames, OPERATOR_EQUALS);

        return contentResolver.delete(tableUri, whereStatement, columnValues);
    }

    public static String buildWhereStatement(String[] columnNames, String operator) {
        if (null == columnNames || 0 == columnNames.length) {
            return WHERE_TRUE;
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(buildWhereClause(columnNames[0], operator));

        if (1 == columnNames.length) {
            return stringBuilder.toString();
        }

        for (int i = 2; i < columnNames.length; i++) {
            stringBuilder.append(" " + WHERE_AND + " ");
            stringBuilder.append(buildWhereClause(columnNames[i], operator));
        }

        return stringBuilder.toString();
    }

    private static String buildWhereClause(String columnName, String operator) {
        return columnName + " " + operator + " ' " + WHERE_PLACEHOLDER + " '";
    }
}
