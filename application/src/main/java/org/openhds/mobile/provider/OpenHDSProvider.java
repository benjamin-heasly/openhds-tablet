package org.openhds.mobile.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import net.sqlcipher.SQLException;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;
import net.sqlcipher.database.SQLiteQueryBuilder;

import org.openhds.mobile.OpenHDS;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * ContentProvider for OpenHDS
 */
public class OpenHDSProvider extends ContentProvider {
    public static final String DATABASE_NAME = "openhds.db";
    public static final int DATABASE_VERSION = 100;

    private static final String TAG = "OpenHDSProvider";
    private static final String DATABASE_PASSWORD_KEY = "database-password";
    private static final String DATABASE_SHARED_PREF = "openhds-provider";

    private static final int INDIVIDUALS = 10;
    private static final int LOCATIONS = 20;
    private static final int LOCATION_HIERARCHIES = 30;
    private static final int LOCATION_HIERARCHY_LEVELS = 40;
    private static final int VISITS = 50;
    private static final int RELATIONSHIPS = 60;
    private static final int USERS = 70;
    private static final int FIELD_WORKERS = 80;
    private static final int SOCIAL_GROUPS = 90;
    private static final int MEMBERSHIPS = 100;
    private static final int RESIDENCIES = 110;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final Map<Integer, String> codeToTableName = new HashMap<>();
    private static final Map<Integer, String> codeToContentType = new HashMap<>();
    private static final Map<Integer, Uri> codeToUriBase = new HashMap<>();

    static {
        sUriMatcher.addURI(OpenHDS.AUTHORITY, OpenHDS.Individuals.TABLE_NAME, INDIVIDUALS);
        sUriMatcher.addURI(OpenHDS.AUTHORITY, OpenHDS.Locations.TABLE_NAME, LOCATIONS);
        sUriMatcher.addURI(OpenHDS.AUTHORITY, OpenHDS.LocationHierarchies.TABLE_NAME, LOCATION_HIERARCHIES);
        sUriMatcher.addURI(OpenHDS.AUTHORITY, OpenHDS.LocationHierarchyLevels.TABLE_NAME, LOCATION_HIERARCHY_LEVELS);
        sUriMatcher.addURI(OpenHDS.AUTHORITY, OpenHDS.Visits.TABLE_NAME, VISITS);
        sUriMatcher.addURI(OpenHDS.AUTHORITY, OpenHDS.Relationships.TABLE_NAME, RELATIONSHIPS);
        sUriMatcher.addURI(OpenHDS.AUTHORITY, OpenHDS.Users.TABLE_NAME, USERS);
        sUriMatcher.addURI(OpenHDS.AUTHORITY, OpenHDS.FieldWorkers.TABLE_NAME, FIELD_WORKERS);
        sUriMatcher.addURI(OpenHDS.AUTHORITY, OpenHDS.SocialGroups.TABLE_NAME, SOCIAL_GROUPS);
        sUriMatcher.addURI(OpenHDS.AUTHORITY, OpenHDS.Memberships.TABLE_NAME, MEMBERSHIPS);
        sUriMatcher.addURI(OpenHDS.AUTHORITY, OpenHDS.Residencies.TABLE_NAME, RESIDENCIES);

        codeToTableName.put(INDIVIDUALS, OpenHDS.Individuals.TABLE_NAME);
        codeToTableName.put(LOCATIONS, OpenHDS.Locations.TABLE_NAME);
        codeToTableName.put(LOCATION_HIERARCHIES, OpenHDS.LocationHierarchies.TABLE_NAME);
        codeToTableName.put(LOCATION_HIERARCHY_LEVELS, OpenHDS.LocationHierarchyLevels.TABLE_NAME);
        codeToTableName.put(VISITS, OpenHDS.Visits.TABLE_NAME);
        codeToTableName.put(RELATIONSHIPS, OpenHDS.Relationships.TABLE_NAME);
        codeToTableName.put(USERS, OpenHDS.Users.TABLE_NAME);
        codeToTableName.put(FIELD_WORKERS, OpenHDS.FieldWorkers.TABLE_NAME);
        codeToTableName.put(SOCIAL_GROUPS, OpenHDS.SocialGroups.TABLE_NAME);
        codeToTableName.put(MEMBERSHIPS, OpenHDS.Memberships.TABLE_NAME);
        codeToTableName.put(RESIDENCIES, OpenHDS.Residencies.TABLE_NAME);

        codeToContentType.put(INDIVIDUALS, OpenHDS.Individuals.CONTENT_TYPE);
        codeToContentType.put(LOCATIONS, OpenHDS.Locations.CONTENT_TYPE);
        codeToContentType.put(LOCATION_HIERARCHIES, OpenHDS.LocationHierarchies.CONTENT_TYPE);
        codeToContentType.put(LOCATION_HIERARCHY_LEVELS, OpenHDS.LocationHierarchyLevels.CONTENT_TYPE);
        codeToContentType.put(VISITS, OpenHDS.Visits.CONTENT_TYPE);
        codeToContentType.put(RELATIONSHIPS, OpenHDS.Relationships.CONTENT_TYPE);
        codeToContentType.put(USERS, OpenHDS.Users.CONTENT_TYPE);
        codeToContentType.put(FIELD_WORKERS, OpenHDS.FieldWorkers.CONTENT_TYPE);
        codeToContentType.put(SOCIAL_GROUPS, OpenHDS.SocialGroups.CONTENT_TYPE);
        codeToContentType.put(RESIDENCIES, OpenHDS.Residencies.CONTENT_TYPE);

        codeToUriBase.put(INDIVIDUALS, OpenHDS.Individuals.CONTENT_ID_URI_BASE);
        codeToUriBase.put(LOCATIONS, OpenHDS.Locations.CONTENT_ID_URI_BASE);
        codeToUriBase.put(LOCATION_HIERARCHIES, OpenHDS.LocationHierarchies.CONTENT_ID_URI_BASE);
        codeToUriBase.put(LOCATION_HIERARCHY_LEVELS, OpenHDS.LocationHierarchyLevels.CONTENT_ID_URI_BASE);
        codeToUriBase.put(VISITS, OpenHDS.Visits.CONTENT_ID_URI_BASE);
        codeToUriBase.put(RELATIONSHIPS, OpenHDS.Relationships.CONTENT_ID_URI_BASE);
        codeToUriBase.put(USERS, OpenHDS.Users.CONTENT_ID_URI_BASE);
        codeToUriBase.put(FIELD_WORKERS, OpenHDS.FieldWorkers.CONTENT_ID_URI_BASE);
        codeToUriBase.put(SOCIAL_GROUPS, OpenHDS.SocialGroups.CONTENT_ID_URI_BASE);
        codeToUriBase.put(MEMBERSHIPS, OpenHDS.Memberships.CONTENT_ID_URI_BASE);
        codeToUriBase.put(RESIDENCIES, OpenHDS.Residencies.CONTENT_ID_URI_BASE);
    }

    private DatabaseHelper databaseHelper;
    private PasswordHelper passwordHelper;

    public DatabaseHelper getDatabaseHelper() {
        return databaseHelper;
    }

    public void setPasswordHelper(PasswordHelper passwordHelper) {
        this.passwordHelper = passwordHelper;
    }

    /**
     * Initializes the provider by creating a new DatabaseHelper. onCreate() is
     * called automatically when Android creates the provider in response to a
     * resolver request from a client.
     */
    @Override
    public boolean onCreate() {

        // password helper that uses Android shared preferences
        passwordHelper = new SharedPreferencesPasswordHelper();

        // Creates a new database helper object.
        // Note: database itself isn't opened until something tries to access it,
        // and it's only created if it doesn't already exist.
        databaseHelper = new DatabaseHelper(getContext());

        try {
            SQLiteDatabase.loadLibs(getContext());
        } catch (Exception e) {
            Log.e(DATABASE_NAME, e.getMessage(), e);
        }
        return true;
    }


    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        int inserted = -1;

        SQLiteDatabase db = databaseHelper.getWritableDatabase(passwordHelper.getPassword());
        db.beginTransaction();
        try {
            inserted = super.bulkInsert(uri, values);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        return inserted;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        int code = sUriMatcher.match(uri);
        if (!codeToTableName.containsKey(code)) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        qb.setTables(codeToTableName.get(code));

        final String orderBy;
        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = OpenHDS.DEFAULT_SORT_ORDER;
        } else {
            orderBy = sortOrder;
        }

        SQLiteDatabase db = databaseHelper.getReadableDatabase(passwordHelper.getPassword());
        Cursor cursor = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        int code = sUriMatcher.match(uri);
        if (!codeToContentType.containsKey(code)) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        return (codeToContentType.get(code));
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        int code = sUriMatcher.match(uri);
        if (!codeToTableName.containsKey(code)) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        String table = codeToTableName.get(code);

        if (!codeToUriBase.containsKey(code)) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        Uri contentUriBase = codeToUriBase.get(code);

        ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }

        SQLiteDatabase db = databaseHelper.getWritableDatabase(passwordHelper.getPassword());

        long rowId = db.insertWithOnConflict(table, null, values, SQLiteDatabase.CONFLICT_REPLACE);

        if (rowId > 0) {
            getContext().getContentResolver().notifyChange(contentUriBase, null);
            return contentUriBase;
        }

        throw new SQLException("Failed to insert row into " + uri + " for content " + values);
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        int code = sUriMatcher.match(uri);
        if (!codeToTableName.containsKey(code)) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        String table = codeToTableName.get(code);

        SQLiteDatabase db = databaseHelper.getWritableDatabase(passwordHelper.getPassword());
        int count = db.delete(table, where, whereArgs);
        getContext().getContentResolver().notifyChange(uri, null);

        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
        int code = sUriMatcher.match(uri);
        if (!codeToTableName.containsKey(code)) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        String table = codeToTableName.get(code);

        SQLiteDatabase db = databaseHelper.getWritableDatabase(passwordHelper.getPassword());
        int count = db.update(table, values, where, whereArgs);
        getContext().getContentResolver().notifyChange(uri, null);

        return count;
    }

    public static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + OpenHDS.Individuals.TABLE_NAME + " ("
                    + OpenHDS.Individuals._ID + " INTEGER,"
                    + OpenHDS.Individuals.UUID + " TEXT PRIMARY KEY NOT NULL,"
                    + OpenHDS.Individuals.LAST_MODIFIED_SERVER + " TEXT,"
                    + OpenHDS.Individuals.LAST_MODIFIED_CLIENT + " TEXT,"
                    + OpenHDS.Individuals.EXT_ID + " TEXT,"
                    + OpenHDS.Individuals.FIRST_NAME + " TEXT,"
                    + OpenHDS.Individuals.MIDDLE_NAME + " TEXT,"
                    + OpenHDS.Individuals.LAST_NAME + " TEXT,"
                    + OpenHDS.Individuals.DOB + " TEXT,"
                    + OpenHDS.Individuals.GENDER + " TEXT,"
                    + OpenHDS.Individuals.FATHER + " TEXT,"
                    + OpenHDS.Individuals.MOTHER + " TEXT);");
            db.execSQL(indexStatement(OpenHDS.Individuals.TABLE_NAME, OpenHDS.Individuals.LAST_MODIFIED_SERVER));
            db.execSQL(indexStatement(OpenHDS.Individuals.TABLE_NAME, OpenHDS.Individuals.LAST_MODIFIED_CLIENT));
            db.execSQL(indexStatement(OpenHDS.Individuals.TABLE_NAME, OpenHDS.Individuals.EXT_ID));
            db.execSQL(indexStatement(OpenHDS.Individuals.TABLE_NAME, OpenHDS.Individuals.FIRST_NAME));
            db.execSQL(indexStatement(OpenHDS.Individuals.TABLE_NAME, OpenHDS.Individuals.LAST_NAME));

            db.execSQL("CREATE TABLE " + OpenHDS.Locations.TABLE_NAME + " ("
                    + OpenHDS.Locations._ID + " INTEGER,"
                    + OpenHDS.Locations.UUID + " TEXT PRIMARY KEY NOT NULL,"
                    + OpenHDS.Locations.LAST_MODIFIED_SERVER + " TEXT,"
                    + OpenHDS.Locations.LAST_MODIFIED_CLIENT + " TEXT,"
                    + OpenHDS.Locations.EXT_ID + " TEXT NOT NULL,"
                    + OpenHDS.Locations.NAME + " TEXT NOT NULL,"
                    + OpenHDS.Locations.DESCRIPTION + " TEXT,"
                    + OpenHDS.Locations.TYPE + " TEXT,"
                    + OpenHDS.Locations.LATITUDE + " TEXT,"
                    + OpenHDS.Locations.LONGITUDE + " TEXT,"
                    + OpenHDS.Locations.LOCATION_HIERARCHY_UUID + " TEXT NOT NULL);");
            db.execSQL(indexStatement(OpenHDS.Locations.TABLE_NAME, OpenHDS.Locations.LAST_MODIFIED_SERVER));
            db.execSQL(indexStatement(OpenHDS.Locations.TABLE_NAME, OpenHDS.Locations.LAST_MODIFIED_CLIENT));
            db.execSQL(indexStatement(OpenHDS.Locations.TABLE_NAME, OpenHDS.Locations.LOCATION_HIERARCHY_UUID));
            db.execSQL(indexStatement(OpenHDS.Locations.TABLE_NAME, OpenHDS.Locations.EXT_ID));
            db.execSQL(indexStatement(OpenHDS.Locations.TABLE_NAME, OpenHDS.Locations.NAME));

            db.execSQL("CREATE TABLE " + OpenHDS.LocationHierarchies.TABLE_NAME + " ("
                    + OpenHDS.LocationHierarchies._ID + " INTEGER,"
                    + OpenHDS.LocationHierarchies.UUID + " TEXT PRIMARY KEY NOT NULL,"
                    + OpenHDS.LocationHierarchies.LAST_MODIFIED_SERVER + " TEXT,"
                    + OpenHDS.LocationHierarchies.LAST_MODIFIED_CLIENT + " TEXT,"
                    + OpenHDS.LocationHierarchies.EXT_ID + " TEXT NOT NULL,"
                    + OpenHDS.LocationHierarchies.LOCATION_HIERARCHY_LEVEL_UUID + " TEXT NOT NULL,"
                    + OpenHDS.LocationHierarchies.NAME + " TEXT NOT NULL,"
                    + OpenHDS.LocationHierarchies.PARENT_UUID + " TEXT);");
            db.execSQL(indexStatement(OpenHDS.LocationHierarchies.TABLE_NAME, OpenHDS.LocationHierarchies.LAST_MODIFIED_SERVER));
            db.execSQL(indexStatement(OpenHDS.LocationHierarchies.TABLE_NAME, OpenHDS.LocationHierarchies.LAST_MODIFIED_CLIENT));
            db.execSQL(indexStatement(OpenHDS.LocationHierarchies.TABLE_NAME, OpenHDS.LocationHierarchies.PARENT_UUID));
            db.execSQL(indexStatement(OpenHDS.LocationHierarchies.TABLE_NAME, OpenHDS.LocationHierarchies.LOCATION_HIERARCHY_LEVEL_UUID));

            db.execSQL("CREATE TABLE " + OpenHDS.LocationHierarchyLevels.TABLE_NAME + " ("
                    + OpenHDS.LocationHierarchyLevels._ID + " INTEGER,"
                    + OpenHDS.LocationHierarchyLevels.UUID + " TEXT PRIMARY KEY NOT NULL,"
                    + OpenHDS.LocationHierarchyLevels.LAST_MODIFIED_SERVER + " TEXT,"
                    + OpenHDS.LocationHierarchyLevels.LAST_MODIFIED_CLIENT + " TEXT,"
                    + OpenHDS.LocationHierarchyLevels.NAME + " TEXT NOT NULL,"
                    + OpenHDS.LocationHierarchyLevels.KEY_IDENTIFIER + " INTEGER);");

            db.execSQL("CREATE TABLE " + OpenHDS.Visits.TABLE_NAME + " ("
                    + OpenHDS.Visits._ID + " INTEGER,"
                    + OpenHDS.Visits.UUID + " TEXT PRIMARY KEY NOT NULL,"
                    + OpenHDS.Visits.LAST_MODIFIED_SERVER + " TEXT,"
                    + OpenHDS.Visits.LAST_MODIFIED_CLIENT + " TEXT,"
                    + OpenHDS.Visits.EXT_ID + " TEXT NOT NULL,"
                    + OpenHDS.Visits.DATE + " TEXT NOT NULL,"
                    + OpenHDS.Visits.FIELD_WORKER_UUID + " TEXT NOT NULL,"
                    + OpenHDS.Visits.LOCATION_UUID + " TEXT);");
            db.execSQL(indexStatement(OpenHDS.Visits.TABLE_NAME, OpenHDS.Visits.LAST_MODIFIED_SERVER));
            db.execSQL(indexStatement(OpenHDS.Visits.TABLE_NAME, OpenHDS.Visits.LAST_MODIFIED_CLIENT));
            db.execSQL(indexStatement(OpenHDS.Visits.TABLE_NAME, OpenHDS.Visits.LOCATION_UUID));

            db.execSQL("CREATE TABLE " + OpenHDS.Relationships.TABLE_NAME + " ("
                    + OpenHDS.Relationships._ID + " INTEGER,"
                    + OpenHDS.Relationships.UUID + " TEXT PRIMARY KEY NOT NULL,"
                    + OpenHDS.Relationships.LAST_MODIFIED_SERVER + " TEXT,"
                    + OpenHDS.Relationships.LAST_MODIFIED_CLIENT + " TEXT,"
                    + OpenHDS.Relationships.INDIVIDUAL_A_UUID + " TEXT NOT NULL,"
                    + OpenHDS.Relationships.INDIVIDUAL_B_UUID + " TEXT NOT NULL,"
                    + OpenHDS.Relationships.TYPE + " TEXT NOT NULL,"
                    + OpenHDS.Relationships.START_DATE + " TEXT NOT NULL);");
            db.execSQL(indexStatement(OpenHDS.Relationships.TABLE_NAME, OpenHDS.Relationships.LAST_MODIFIED_SERVER));
            db.execSQL(indexStatement(OpenHDS.Relationships.TABLE_NAME, OpenHDS.Relationships.LAST_MODIFIED_CLIENT));
            db.execSQL(indexStatement(OpenHDS.Relationships.TABLE_NAME, OpenHDS.Relationships.INDIVIDUAL_A_UUID));
            db.execSQL(indexStatement(OpenHDS.Relationships.TABLE_NAME, OpenHDS.Relationships.INDIVIDUAL_B_UUID));

            db.execSQL("CREATE TABLE " + OpenHDS.Users.TABLE_NAME + " ("
                    + OpenHDS.Users._ID + " INTEGER,"
                    + OpenHDS.Users.UUID + " TEXT PRIMARY KEY NOT NULL,"
                    + OpenHDS.Users.LAST_MODIFIED_SERVER + " TEXT,"
                    + OpenHDS.Users.LAST_MODIFIED_CLIENT + " TEXT,"
                    + OpenHDS.Users.USERNAME + " TEXT NOT NULL,"
                    + OpenHDS.Users.FIRST_NAME + " TEXT,"
                    + OpenHDS.Users.LAST_NAME + " TEXT,"
                    + OpenHDS.Users.PASSWORD_HASH + " TEXT NOT NULL);");
            db.execSQL(indexStatement(OpenHDS.Users.TABLE_NAME, OpenHDS.Users.LAST_MODIFIED_SERVER));
            db.execSQL(indexStatement(OpenHDS.Users.TABLE_NAME, OpenHDS.Users.LAST_MODIFIED_CLIENT));
            db.execSQL(indexStatement(OpenHDS.Users.TABLE_NAME, OpenHDS.Users.USERNAME));

            db.execSQL("CREATE TABLE " + OpenHDS.FieldWorkers.TABLE_NAME + " ("
                    + OpenHDS.FieldWorkers._ID + " INTEGER,"
                    + OpenHDS.FieldWorkers.UUID + " TEXT PRIMARY KEY NOT NULL,"
                    + OpenHDS.FieldWorkers.LAST_MODIFIED_SERVER + " TEXT,"
                    + OpenHDS.FieldWorkers.LAST_MODIFIED_CLIENT + " TEXT,"
                    + OpenHDS.FieldWorkers.FIELD_WORKER_ID + " TEXT NOT NULL,"
                    + OpenHDS.FieldWorkers.FIRST_NAME + " TEXT,"
                    + OpenHDS.FieldWorkers.LAST_NAME + " TEXT,"
                    + OpenHDS.FieldWorkers.PASSWORD_HASH + " TEXT NOT NULL);");
            db.execSQL(indexStatement(OpenHDS.FieldWorkers.TABLE_NAME, OpenHDS.FieldWorkers.LAST_MODIFIED_SERVER));
            db.execSQL(indexStatement(OpenHDS.FieldWorkers.TABLE_NAME, OpenHDS.FieldWorkers.LAST_MODIFIED_CLIENT));
            db.execSQL(indexStatement(OpenHDS.FieldWorkers.TABLE_NAME, OpenHDS.FieldWorkers.FIRST_NAME));
            db.execSQL(indexStatement(OpenHDS.FieldWorkers.TABLE_NAME, OpenHDS.FieldWorkers.LAST_NAME));
            db.execSQL(indexStatement(OpenHDS.FieldWorkers.TABLE_NAME, OpenHDS.FieldWorkers.FIELD_WORKER_ID));

            db.execSQL("CREATE TABLE " + OpenHDS.SocialGroups.TABLE_NAME + " ("
                    + OpenHDS.SocialGroups._ID + " INTEGER,"
                    + OpenHDS.SocialGroups.UUID + " TEXT PRIMARY KEY NOT NULL,"
                    + OpenHDS.SocialGroups.LAST_MODIFIED_SERVER + " TEXT,"
                    + OpenHDS.SocialGroups.LAST_MODIFIED_CLIENT + " TEXT,"
                    + OpenHDS.SocialGroups.EXT_ID + " TEXT NOT NULL,"
                    + OpenHDS.SocialGroups.GROUP_NAME + " TEXT NOT NULL);");
            db.execSQL(indexStatement(OpenHDS.SocialGroups.TABLE_NAME, OpenHDS.SocialGroups.LAST_MODIFIED_SERVER));
            db.execSQL(indexStatement(OpenHDS.SocialGroups.TABLE_NAME, OpenHDS.SocialGroups.LAST_MODIFIED_CLIENT));

            db.execSQL("CREATE TABLE " + OpenHDS.Memberships.TABLE_NAME + " ("
                    + OpenHDS.Memberships._ID + " INTEGER,"
                    + OpenHDS.Memberships.UUID + " TEXT PRIMARY KEY NOT NULL,"
                    + OpenHDS.Memberships.LAST_MODIFIED_SERVER + " TEXT,"
                    + OpenHDS.Memberships.LAST_MODIFIED_CLIENT + " TEXT,"
                    + OpenHDS.Memberships.INDIVIDUAL_UUID + " TEXT NOT NULL,"
                    + OpenHDS.Memberships.SOCIAL_GROUP_UUID + " TEXT NOT NULL);");
            db.execSQL(indexStatement(OpenHDS.Memberships.TABLE_NAME, OpenHDS.Memberships.LAST_MODIFIED_SERVER));
            db.execSQL(indexStatement(OpenHDS.Memberships.TABLE_NAME, OpenHDS.Memberships.LAST_MODIFIED_CLIENT));
            db.execSQL(indexStatement(OpenHDS.Memberships.TABLE_NAME, OpenHDS.Memberships.INDIVIDUAL_UUID));
            db.execSQL(indexStatement(OpenHDS.Memberships.TABLE_NAME, OpenHDS.Memberships.SOCIAL_GROUP_UUID));

            db.execSQL("CREATE TABLE " + OpenHDS.Residencies.TABLE_NAME + " ("
                    + OpenHDS.Residencies._ID + " INTEGER,"
                    + OpenHDS.Residencies.UUID + " TEXT PRIMARY KEY NOT NULL,"
                    + OpenHDS.Residencies.LAST_MODIFIED_SERVER + " TEXT,"
                    + OpenHDS.Residencies.LAST_MODIFIED_CLIENT + " TEXT,"
                    + OpenHDS.Residencies.INDIVIDUAL_UUID + " TEXT NOT NULL,"
                    + OpenHDS.Residencies.END_TYPE + " TEXT,"
                    + OpenHDS.Residencies.LOCATION_UUID + " TEXT NOT NULL);");
            db.execSQL(indexStatement(OpenHDS.Residencies.TABLE_NAME, OpenHDS.Residencies.LAST_MODIFIED_SERVER));
            db.execSQL(indexStatement(OpenHDS.Residencies.TABLE_NAME, OpenHDS.Residencies.LAST_MODIFIED_CLIENT));
            db.execSQL(indexStatement(OpenHDS.Residencies.TABLE_NAME, OpenHDS.Residencies.INDIVIDUAL_UUID));
            db.execSQL(indexStatement(OpenHDS.Residencies.TABLE_NAME, OpenHDS.Residencies.LOCATION_UUID));
        }

        private static String indexStatement(String tableName, String columnName) {
            return "CREATE INDEX " + tableName + "_" + columnName
                    + " ON " + tableName + "(" + columnName + ") ; ";

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from " + oldVersion + " to " + newVersion
                    + ", which will destroy all old data.");
            db.execSQL("DROP TABLE IF EXISTS " + OpenHDS.Individuals.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + OpenHDS.Locations.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + OpenHDS.LocationHierarchies.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + OpenHDS.LocationHierarchyLevels.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + OpenHDS.Visits.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + OpenHDS.Relationships.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + OpenHDS.Users.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + OpenHDS.FieldWorkers.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + OpenHDS.SocialGroups.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + OpenHDS.Memberships.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + OpenHDS.Residencies.TABLE_NAME);
            onCreate(db);
        }
    }

    private class SharedPreferencesPasswordHelper implements PasswordHelper {

        private String password;

        @Override
        public String getPassword() {

            // already have cached password?
            if (null != password) {
                return password;
            }

            // find a saved password in shared preferences?
            SharedPreferences sp = getContext().getSharedPreferences(DATABASE_SHARED_PREF, Context.MODE_PRIVATE);
            password = sp.getString(DATABASE_PASSWORD_KEY, "");

            // make a new password and save in shared preferences
            if (password.isEmpty()) {
                password = UUID.randomUUID().toString();
                Editor editor = sp.edit();
                editor.putString(DATABASE_PASSWORD_KEY, password);
                editor.commit();
            }

            return password;
        }
    }
}
