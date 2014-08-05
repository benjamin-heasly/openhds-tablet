package org.openhds.mobile.database;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import org.openhds.mobile.database.queries.Queries;
import org.openhds.mobile.model.Location;
import org.openhds.mobile.projectdata.ProjectFormFields;

import java.util.Map;

import static org.openhds.mobile.OpenHDS.Locations.COLUMN_LOCATION_COMMUNITY_NAME;
import static org.openhds.mobile.OpenHDS.Locations.COLUMN_LOCATION_EXTID;
import static org.openhds.mobile.OpenHDS.Locations.COLUMN_LOCATION_HIERARCHY;
import static org.openhds.mobile.OpenHDS.Locations.COLUMN_LOCATION_LATITUDE;
import static org.openhds.mobile.OpenHDS.Locations.COLUMN_LOCATION_LOCALITY_NAME;
import static org.openhds.mobile.OpenHDS.Locations.COLUMN_LOCATION_LONGITUDE;
import static org.openhds.mobile.OpenHDS.Locations.COLUMN_LOCATION_MAP_AREA_NAME;
import static org.openhds.mobile.OpenHDS.Locations.COLUMN_LOCATION_NAME;
import static org.openhds.mobile.OpenHDS.Locations.COLUMN_LOCATION_SECTOR_NAME;
import static org.openhds.mobile.OpenHDS.Locations.CONTENT_ID_URI_BASE;

public class LocationAdapter {

    public static Location create(Map<String, String> formInstanceData) {
        Location location = new Location();

        location.setExtId(formInstanceData.get(ProjectFormFields.Locations
                .getFieldNameFromColumn(COLUMN_LOCATION_EXTID)));
        location.setName(formInstanceData.get(ProjectFormFields.Locations
                .getFieldNameFromColumn(COLUMN_LOCATION_NAME)));
        location.setHierarchyExtId(formInstanceData.get(ProjectFormFields.Locations
                .getFieldNameFromColumn(COLUMN_LOCATION_HIERARCHY)));
        location.setCommunityName(formInstanceData.get(ProjectFormFields.Locations
                .getFieldNameFromColumn(COLUMN_LOCATION_COMMUNITY_NAME)));
        location.setLocalityName(formInstanceData.get(ProjectFormFields.Locations
                .getFieldNameFromColumn(COLUMN_LOCATION_LOCALITY_NAME)));
        location.setSectorName(formInstanceData.get(ProjectFormFields.Locations
                .getFieldNameFromColumn(COLUMN_LOCATION_SECTOR_NAME)));
        location.setMapAreaName(formInstanceData.get(ProjectFormFields.Locations
                .getFieldNameFromColumn(COLUMN_LOCATION_MAP_AREA_NAME)));

        return location;
    }

    private static ContentValues buildContentValues(Location location) {
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_LOCATION_EXTID, location.getExtId());
        cv.put(COLUMN_LOCATION_HIERARCHY, location.getHierarchyExtId());
        cv.put(COLUMN_LOCATION_LATITUDE, location.getLatitude());
        cv.put(COLUMN_LOCATION_LONGITUDE, location.getLongitude());
        cv.put(COLUMN_LOCATION_NAME, location.getName());
        cv.put(COLUMN_LOCATION_SECTOR_NAME, location.getSectorName());
        cv.put(COLUMN_LOCATION_MAP_AREA_NAME, location.getMapAreaName());
        cv.put(COLUMN_LOCATION_LOCALITY_NAME, location.getLocalityName());
        cv.put(COLUMN_LOCATION_COMMUNITY_NAME, location.getCommunityName());

        return cv;
    }

    public static int update(ContentResolver resolver, Location location) {
        ContentValues cv = buildContentValues(location);
        return resolver.update(CONTENT_ID_URI_BASE, cv, COLUMN_LOCATION_EXTID + " = '" + location.getExtId()
                + "'", null);
    }

    public static Uri insert(ContentResolver resolver, Location location) {
        ContentValues cv = buildContentValues(location);
        return resolver.insert(CONTENT_ID_URI_BASE, cv);
    }

    // returns true if inserts or false if updates.
    public static boolean insertOrUpdate(ContentResolver resolver, Location location) {
        if (Queries.hasLocationByExtId(resolver, location.getExtId())) {
            update(resolver, location);
            return false;
        } else {
            Uri newRow = insert(resolver, location);
            return (null != newRow);
        }
    }
}
