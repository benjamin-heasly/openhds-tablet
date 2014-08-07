package org.openhds.mobile.repository;

import android.content.ContentValues;
import android.database.Cursor;
import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.model.Location;

import static org.openhds.mobile.OpenHDS.Locations.COLUMN_LOCATION_COMMUNITY_NAME;
import static org.openhds.mobile.OpenHDS.Locations.COLUMN_LOCATION_EXTID;
import static org.openhds.mobile.OpenHDS.Locations.COLUMN_LOCATION_HIERARCHY;
import static org.openhds.mobile.OpenHDS.Locations.COLUMN_LOCATION_LATITUDE;
import static org.openhds.mobile.OpenHDS.Locations.COLUMN_LOCATION_LOCALITY_NAME;
import static org.openhds.mobile.OpenHDS.Locations.COLUMN_LOCATION_LONGITUDE;
import static org.openhds.mobile.OpenHDS.Locations.COLUMN_LOCATION_MAP_AREA_NAME;
import static org.openhds.mobile.OpenHDS.Locations.COLUMN_LOCATION_NAME;
import static org.openhds.mobile.OpenHDS.Locations.COLUMN_LOCATION_SECTOR_NAME;
import static org.openhds.mobile.repository.RepositoryUtils.extractString;


/**
 * Convert Locations to and from database.  Location-specific queries.
 */
public class LocationGateway extends Gateway<Location> {

    public LocationGateway() {
        super(OpenHDS.Locations.CONTENT_ID_URI_BASE, COLUMN_LOCATION_EXTID, new LocationConverter());
    }

    private static class LocationConverter implements Converter<Location> {

        @Override
        public Location fromCursor(Cursor cursor) {
            Location location = new Location();

            location.setExtId(extractString(cursor, COLUMN_LOCATION_EXTID));
            location.setHierarchyExtId(extractString(cursor, COLUMN_LOCATION_HIERARCHY));
            location.setLatitude(extractString(cursor, COLUMN_LOCATION_LATITUDE));
            location.setLongitude(extractString(cursor, COLUMN_LOCATION_LONGITUDE));
            location.setName(extractString(cursor, COLUMN_LOCATION_NAME));
            location.setSectorName(extractString(cursor, COLUMN_LOCATION_SECTOR_NAME));
            location.setMapAreaName(extractString(cursor, COLUMN_LOCATION_MAP_AREA_NAME));
            location.setLocalityName(extractString(cursor, COLUMN_LOCATION_LOCALITY_NAME));
            location.setCommunityName(extractString(cursor, COLUMN_LOCATION_COMMUNITY_NAME));

            return location;
        }

        @Override
        public ContentValues toContentValues(Location location) {
            ContentValues contentValues = new ContentValues();

            contentValues.put(COLUMN_LOCATION_EXTID, location.getExtId());
            contentValues.put(COLUMN_LOCATION_HIERARCHY, location.getHierarchyExtId());
            contentValues.put(COLUMN_LOCATION_LATITUDE, location.getLatitude());
            contentValues.put(COLUMN_LOCATION_LONGITUDE, location.getLongitude());
            contentValues.put(COLUMN_LOCATION_NAME, location.getName());
            contentValues.put(COLUMN_LOCATION_SECTOR_NAME, location.getSectorName());
            contentValues.put(COLUMN_LOCATION_MAP_AREA_NAME, location.getMapAreaName());
            contentValues.put(COLUMN_LOCATION_LOCALITY_NAME, location.getLocalityName());
            contentValues.put(COLUMN_LOCATION_COMMUNITY_NAME, location.getCommunityName());

            return contentValues;
        }

        @Override
        public String getId(Location location) {
            return location.getExtId();
        }
    }
}
