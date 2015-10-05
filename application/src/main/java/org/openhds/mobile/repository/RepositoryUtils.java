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
    public static final String LESS_THAN_EQUALS = "<=";
    public static final String GREATER_THAN_EQUALS = ">=";
    public static final String LIKE = "LIKE";
    public static final String LIKE_WILD_CARD = "%";
    public static final String ASCENDING = "ASC";
    public static final String DESCENDING = "DESC";

    private static final String AND = "AND";
    private static final String LIMIT = "LIMIT";
    private static final String OFFSET = "OFFSET";
    private static final String WHERE_PLACEHOLDER = "?";
    private static final String WHERE_ALL = "1";
    private static final String[] COUNT_COLUMN = new String[] {"COUNT(*) AS count"};

    public static Uri insert(ContentResolver contentResolver, Uri tableUri, ContentValues contentValues) {
        return contentResolver.insert(tableUri, contentValues);
    }

    public static int bulkInsert(ContentResolver contentResolver, Uri tableUri, ContentValues[] allContentValues) {
        return contentResolver.bulkInsert(tableUri, allContentValues);
    }

    public static int update(ContentResolver contentResolver, Uri tableUri, ContentValues contentValues,
                             String columnName, String columnValue) {

        final String[] columnNames = {columnName};
        final String[] columnValues = {columnValue};
        return update(contentResolver, tableUri, contentValues, columnNames, columnValues);
    }

    public static int update(ContentResolver contentResolver, Uri tableUri, ContentValues contentValues,
                             String[] columnNames, String[] columnValues) {

        final String[] ops = new String[columnNames.length];
        for (int i=0; i< ops.length; i++) {
            ops[i] = EQUALS;
        }
        final String whereStatement = buildWhereStatement(columnNames, ops);
        return contentResolver.update(tableUri, contentValues, whereStatement, columnValues);
    }

    public static Cursor query(ContentResolver contentResolver, Uri tableUri, String whereStatement,
                               String[] columnValues, String columnOrderBy) {

        return contentResolver.query(tableUri, null, whereStatement, columnValues, columnOrderBy);
    }

    public static Cursor queryRange(ContentResolver contentResolver, Uri tableUri, String whereStatement,
                                    String[] columnValues, String columnOrderBy, int start, int maxResults) {

        final String rangeStatement = buildRangeStatement(start, maxResults);
        final String orderByPlusRange = columnOrderBy + " " + rangeStatement;
        return contentResolver.query(tableUri, null, whereStatement, columnValues, orderByPlusRange);
    }

    public static int countRecords(ContentResolver contentResolver, Uri tableUri) {
        int count = 0;
        Cursor cursor = contentResolver.query(tableUri, COUNT_COLUMN, null, null, null);
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public static int delete(ContentResolver contentResolver, Uri tableUri, String columnName, String columnValue) {
        final String[] columnNames = {columnName};
        final String[] columnValues = {columnValue};
        final String whereStatement = buildWhereStatement(columnNames, new String[]{EQUALS});
        return contentResolver.delete(tableUri, whereStatement, columnValues);
    }

    public static String buildWhereStatement(String[] columnNames, String[] operators) {
        if (null == columnNames || 0 == columnNames.length) {
            return WHERE_ALL;
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(buildWhereClause(columnNames[0], operators[0]));

        if (1 == columnNames.length) {
            return stringBuilder.toString();
        }

        for (int i = 1; i < columnNames.length; i++) {
            stringBuilder.append(" " + AND + " ");
            stringBuilder.append(buildWhereClause(columnNames[i], operators[i]));
        }

        return stringBuilder.toString();
    }

    private static String buildWhereClause(String columnName, String operator) {
        return columnName + " " + operator + " " + WHERE_PLACEHOLDER;
    }

    public static String buildRangeStatement(int start, int maxResults) {
        return LIMIT + " " + maxResults + " " + OFFSET + " " + start;
    }

    public static String extractString(Cursor cursor, String columnName) {
        int columnIndex = cursor.getColumnIndex(columnName);
        if (columnIndex < 0) {
            return null;
        }
        return cursor.getString(columnIndex);
    }

    public static int extractInt(Cursor cursor, String columnName) {
        int columnIndex = cursor.getColumnIndex(columnName);
        if (columnIndex < 0) {
            return 0;
        }
        return cursor.getInt(columnIndex);
    }
}

