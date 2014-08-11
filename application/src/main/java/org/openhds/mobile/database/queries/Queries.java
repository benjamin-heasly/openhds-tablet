package org.openhds.mobile.database.queries;

import org.openhds.mobile.OpenHDS;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import org.openhds.mobile.projectdata.ProjectResources;

/**
 * Centralized location to put queries on data to reduce duplication
 */
public class Queries {

    private Queries() {
    }

    public static boolean hasIndividualByExtId(ContentResolver resolver,
                                               String extId) {
        return isFound(getCursor(resolver,
                OpenHDS.Individuals.CONTENT_ID_URI_BASE,
                OpenHDS.Individuals.COLUMN_INDIVIDUAL_EXTID, extId, null));
    }

    private static Cursor getCursor(ContentResolver resolver, Uri uri,
                                    String column, String value, String orderBy) {
        return resolver.query(uri, null, column + " = ?",
                new String[]{value}, orderBy);
    }

    private static Cursor getCursor(ContentResolver resolver, Uri uri, String column1,
                                    String value1, String column2, String value2, String orderBy) {

        final String whereClause = column1 + " = ? AND " + column2 + " = ?";
        return resolver.query(uri, null, whereClause, new String[]{value1, value2}, orderBy);
    }

    public static Cursor getAllIndividuals(ContentResolver resolver) {
        return getCursorForAll(resolver,
                OpenHDS.Individuals.CONTENT_ID_URI_BASE);
    }

    public static Cursor getAllLocations(ContentResolver resolver) {
        return getCursorForAll(resolver, OpenHDS.Locations.CONTENT_ID_URI_BASE);
    }

    public static Cursor getAllHierarchys(ContentResolver resolver) {
        return getCursorForAll(resolver,
                OpenHDS.HierarchyItems.CONTENT_ID_URI_BASE);
    }

    private static Cursor getCursorForAll(ContentResolver resolver, Uri uri) {
        return resolver.query(uri, null, null, null, null);
    }

    public static Cursor getIndividualByExtId(ContentResolver resolver,
                                              String extId) {
        return getCursor(resolver, OpenHDS.Individuals.CONTENT_ID_URI_BASE,
                OpenHDS.Individuals.COLUMN_INDIVIDUAL_EXTID, extId, null);
    }

    public static Cursor getIndividualsByResidency(ContentResolver resolver,
                                                   String extId) {
        return getCursor(resolver, OpenHDS.Individuals.CONTENT_ID_URI_BASE,
                OpenHDS.Individuals.COLUMN_INDIVIDUAL_RESIDENCE_LOCATION_EXTID,
                extId, OpenHDS.Individuals.COLUMN_INDIVIDUAL_RESIDENCE_END_TYPE, ProjectResources.Individual.RESIDENCY_END_TYPE_NA, null);
    }

    private static boolean isFound(Cursor cursor) {
        boolean found = false;
        if (cursor != null) {
            found = cursor.moveToFirst();
            cursor.close();
        }

        return found;
    }

    public static boolean hasLocationByExtId(ContentResolver resolver,
                                             String temp) {
        return isFound(getCursor(resolver,
                OpenHDS.Locations.CONTENT_ID_URI_BASE,
                OpenHDS.Locations.COLUMN_LOCATION_EXTID, temp, null));
    }

    public static Cursor getLocationByExtId(ContentResolver resolver,
                                            String extId) {
        return getCursor(resolver, OpenHDS.Locations.CONTENT_ID_URI_BASE,
                OpenHDS.Locations.COLUMN_LOCATION_EXTID, extId, null);
    }

    public static Cursor getLocationsByHierarchy(ContentResolver resolver,
                                                 String extId) {
        return getCursor(resolver, OpenHDS.Locations.CONTENT_ID_URI_BASE,
                OpenHDS.Locations.COLUMN_LOCATION_HIERARCHY, extId, null);
    }

    public static Cursor getLocationsOrderedByBuildingNumber(ContentResolver resolver, String hierarchyExtId) {

        return resolver.query(OpenHDS.Locations.CONTENT_ID_URI_BASE, new String[]{OpenHDS.Locations.COLUMN_LOCATION_BUILDING_NUMBER}, OpenHDS.Locations.COLUMN_LOCATION_HIERARCHY + " = ?",
                new String[]{hierarchyExtId}, "CAST ("+OpenHDS.Locations.COLUMN_LOCATION_BUILDING_NUMBER+" AS INT)"+" DESC");

    }

    public static Cursor getHierarchyByExtId(ContentResolver contentResolver,
                                             String hierarchy) {
        return getCursor(contentResolver,
                OpenHDS.HierarchyItems.CONTENT_ID_URI_BASE,
                OpenHDS.HierarchyItems.COLUMN_HIERARCHY_EXTID, hierarchy, null);
    }

    public static Cursor getLocationsBySectorNameAndMapAreaName(
            ContentResolver contentResolver, String sectorName, String mapAreaName) {

        return getCursor(contentResolver, OpenHDS.Locations.CONTENT_ID_URI_BASE,
                OpenHDS.Locations.COLUMN_LOCATION_SECTOR_NAME, sectorName,
                OpenHDS.Locations.COLUMN_LOCATION_MAP_AREA_NAME, mapAreaName,
                null);
    }

    public static Cursor getHierarchysByLevel(ContentResolver contentResolver,
                                              String hierarchyTopLevel) {
        return getCursor(contentResolver,
                OpenHDS.HierarchyItems.CONTENT_ID_URI_BASE,
                OpenHDS.HierarchyItems.COLUMN_HIERARCHY_LEVEL,
                hierarchyTopLevel, null);
    }

    public static Cursor getHierarchysByParent(ContentResolver contentResolver,
                                               String extId) {
        return getCursor(contentResolver,
                OpenHDS.HierarchyItems.CONTENT_ID_URI_BASE,
                OpenHDS.HierarchyItems.COLUMN_HIERARCHY_PARENT, extId, OpenHDS.HierarchyItems.COLUMN_HIERARCHY_EXTID);
    }

    public static boolean hasFieldWorker(ContentResolver resolver,
                                         String extId, String password) {
        Cursor cursor = resolver
                .query(OpenHDS.FieldWorkers.CONTENT_ID_URI_BASE,
                        new String[]{OpenHDS.FieldWorkers.COLUMN_FIELD_WORKER_EXTID},
                        OpenHDS.FieldWorkers.COLUMN_FIELD_WORKER_EXTID
                                + " = ? AND "
                                + OpenHDS.FieldWorkers.COLUMN_FIELD_WORKER_PASSWORD
                                + " = ?", new String[]{extId, password},
                        null);
        return isFound(cursor);
    }

    public static Cursor getFieldWorkByExtId(ContentResolver resolver,
                                             String extId) {
        return getCursor(resolver, OpenHDS.FieldWorkers.CONTENT_ID_URI_BASE,
                OpenHDS.FieldWorkers.COLUMN_FIELD_WORKER_EXTID, extId, null);
    }

    public static boolean hasSocialGroupByExtId(ContentResolver resolver,
                                                String temp) {
        return isFound(getCursor(resolver,
                OpenHDS.SocialGroups.CONTENT_ID_URI_BASE,
                OpenHDS.SocialGroups.COLUMN_SOCIAL_GROUP_EXTID, temp, null));
    }

    public static Cursor getSocialGroupByExtId(ContentResolver resolver,
                                               String extId) {
        return getCursor(resolver, OpenHDS.SocialGroups.CONTENT_ID_URI_BASE,
                OpenHDS.SocialGroups.COLUMN_SOCIAL_GROUP_EXTID, extId, null);
    }

    public static Cursor getSocialGroupByName(ContentResolver resolver,
                                              String groupName) {
        return getCursor(resolver, OpenHDS.SocialGroups.CONTENT_ID_URI_BASE,
                OpenHDS.SocialGroups.COLUMN_SOCIAL_GROUP_NAME, groupName, null);
    }

    public static Cursor getSocialGroupsByIndividualExtId(
            ContentResolver resolver, String extId) {
        Uri uri = OpenHDS.Individuals.CONTENT_SG_URI_BASE.buildUpon()
                .appendPath(extId).build();
        return resolver.query(uri, null, "x."
                        + OpenHDS.Memberships.COLUMN_INDIVIDUAL_EXTID + " = ?",
                new String[]{extId}, "s." + OpenHDS.SocialGroups._ID);
    }

    public static Cursor getIndividualsExtIdsByPrefix(ContentResolver resolver,
                                                      String prefix) {
        String likeArg = "'" + prefix + "%'";
        return resolver.query(OpenHDS.Individuals.CONTENT_ID_URI_BASE,
                new String[]{OpenHDS.Individuals.COLUMN_INDIVIDUAL_EXTID},
                OpenHDS.Individuals.COLUMN_INDIVIDUAL_EXTID + " LIKE "
                        + likeArg, null,
                OpenHDS.Individuals.COLUMN_INDIVIDUAL_EXTID);
    }

    public static Cursor allSocialGroups(ContentResolver resolver) {
        return resolver.query(OpenHDS.SocialGroups.CONTENT_ID_URI_BASE, null,
                null, null, null);
    }

    public static Cursor getHeadOfHouseholdByHouseholdExtId(
            ContentResolver resolver, String householdExtId) {
        Cursor cursor = getSocialGroupByExtId(resolver, householdExtId);

        cursor.moveToFirst();
        int columnIndex = cursor
                .getColumnIndex(OpenHDS.SocialGroups.COLUMN_SOCIAL_GROUP_HEAD_INDIVIDUAL_EXTID);
        String headExtId = cursor.getString(columnIndex);

        cursor.close();
        return getCursor(resolver, OpenHDS.Individuals.CONTENT_ID_URI_BASE,
                OpenHDS.Individuals.COLUMN_INDIVIDUAL_EXTID, headExtId, null);

    }

    public static Cursor allLocations(ContentResolver contentResolver) {
        return contentResolver.query(OpenHDS.Locations.CONTENT_ID_URI_BASE,
                null, null, null, null);
    }

    public static Cursor getRelationshipByIndividualA(ContentResolver resolver,
                                                      String extId) {
        return getCursor(resolver, OpenHDS.Relationships.CONTENT_ID_URI_BASE,
                OpenHDS.Relationships.COLUMN_RELATIONSHIP_INDIVIDUAL_A, extId, null);
    }

    public static Cursor getRelationshipByIndividualB(ContentResolver resolver,
                                                      String extId) {
        return getCursor(resolver, OpenHDS.Relationships.CONTENT_ID_URI_BASE,
                OpenHDS.Relationships.COLUMN_RELATIONSHIP_INDIVIDUAL_B, extId, null);
    }

    public static Cursor getRelationshipByBothIndividuals(
            ContentResolver resolver, String extIdA, String extIdB) {
        return resolver
                .query(OpenHDS.Relationships.CONTENT_ID_URI_BASE,
                        new String[]{
                                OpenHDS.Relationships.COLUMN_RELATIONSHIP_INDIVIDUAL_A,
                                OpenHDS.Relationships.COLUMN_RELATIONSHIP_INDIVIDUAL_B,
                                OpenHDS.Relationships.COLUMN_RELATIONSHIP_STARTDATE,
                                OpenHDS.Relationships.COLUMN_RELATIONSHIP_TYPE},
                        OpenHDS.Relationships.COLUMN_RELATIONSHIP_INDIVIDUAL_A
                                + " = '"
                                + extIdA
                                + "' AND "
                                + OpenHDS.Relationships.COLUMN_RELATIONSHIP_INDIVIDUAL_B
                                + " = '" + extIdB + "'", null, null);
    }

    public static boolean hasRelationshipByBothIndividuals(
            ContentResolver resolver, String extIdA, String extIdB) {

        Cursor cursor = getRelationshipByBothIndividuals(resolver, extIdA,
                extIdB);

        if (cursor.getCount() > 0) {
            cursor.close();
            return true;
        } else {
            cursor.close();
            return false;
        }
    }

    public static Cursor getMembershipByHouseholdAndIndividualExtId(
            ContentResolver resolver, String extIdH, String extIdI) {
        return resolver
                .query(OpenHDS.Memberships.CONTENT_ID_URI_BASE,
                        new String[]{
                                OpenHDS.Memberships.COLUMN_SOCIAL_GROUP_EXTID,
                                OpenHDS.Memberships.COLUMN_INDIVIDUAL_EXTID,
                                OpenHDS.Memberships.COLUMN_MEMBERSHIP_RELATIONSHIP_TO_HEAD,},
                        OpenHDS.Memberships.COLUMN_SOCIAL_GROUP_EXTID + " = '"
                                + extIdH + "' AND "
                                + OpenHDS.Memberships.COLUMN_INDIVIDUAL_EXTID
                                + " = '" + extIdI + "'", null, null);
    }

    public static Cursor getMembershipsByIndividualExtId(
            ContentResolver resolver, String extId) {
        return getCursor(resolver, OpenHDS.Memberships.CONTENT_ID_URI_BASE,
                OpenHDS.Memberships.COLUMN_INDIVIDUAL_EXTID, extId, null);
    }

    public static boolean hasMembershipByHouseholdAndIndividualExtId(
            ContentResolver resolver, String extIdH, String extIdI) {
        Cursor cursor = getMembershipByHouseholdAndIndividualExtId(resolver,
                extIdH, extIdI);
        if (cursor.getCount() > 0) {
            cursor.close();
            return true;
        } else {
            cursor.close();
            return false;
        }

    }

}
