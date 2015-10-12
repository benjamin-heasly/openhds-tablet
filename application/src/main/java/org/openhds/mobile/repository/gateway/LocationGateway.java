package org.openhds.mobile.repository.gateway;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;

import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.model.core.Location;
import org.openhds.mobile.repository.Converter;
import org.openhds.mobile.repository.DataWrapper;
import org.openhds.mobile.repository.Query;

import static org.openhds.mobile.OpenHDS.Common.LAST_MODIFIED_CLIENT;
import static org.openhds.mobile.OpenHDS.Common.LAST_MODIFIED_SERVER;
import static org.openhds.mobile.OpenHDS.Locations.EXT_ID;
import static org.openhds.mobile.OpenHDS.Locations.LATITUDE;
import static org.openhds.mobile.OpenHDS.Locations.LOCATION_HIERARCHY_UUID;
import static org.openhds.mobile.OpenHDS.Locations.LONGITUDE;
import static org.openhds.mobile.OpenHDS.Locations.NAME;
import static org.openhds.mobile.OpenHDS.Locations.UUID;
import static org.openhds.mobile.repository.RepositoryUtils.extractString;

/**
 * Convert Locations to and from database.  Location-specific queries.
 */
public class LocationGateway extends Gateway<Location> {

    public LocationGateway() {
        super(OpenHDS.Locations.CONTENT_ID_URI_BASE, UUID, new LocationConverter());
    }

    public Query findByHierarchy(String hierarchyId) {
        return new Query(tableUri, LOCATION_HIERARCHY_UUID, hierarchyId, UUID);
    }

    private static class LocationConverter implements Converter<Location> {

        @Override
        public Location fromCursor(Cursor cursor) {
            Location location = new Location();

            location.setUuid(extractString(cursor, UUID));
            location.setExtId(extractString(cursor, EXT_ID));
            location.setHierarchyUuid(extractString(cursor, LOCATION_HIERARCHY_UUID));
            location.setLatitude(extractString(cursor, LATITUDE));
            location.setLongitude(extractString(cursor, LONGITUDE));
            location.setName(extractString(cursor, NAME));
            location.setLastModifiedClient(extractString(cursor, LAST_MODIFIED_CLIENT));
            location.setLastModifiedServer(extractString(cursor, LAST_MODIFIED_SERVER));

            return location;
        }

        @Override
        public ContentValues toContentValues(Location location) {
            ContentValues contentValues = new ContentValues();

            contentValues.put(UUID, location.getUuid());
            contentValues.put(EXT_ID, location.getExtId());
            contentValues.put(LOCATION_HIERARCHY_UUID, location.getHierarchyUuid());
            contentValues.put(LATITUDE, location.getLatitude());
            contentValues.put(LONGITUDE, location.getLongitude());
            contentValues.put(NAME, location.getName());
            contentValues.put(LAST_MODIFIED_CLIENT, location.getLastModifiedClient());
            contentValues.put(LAST_MODIFIED_SERVER, location.getLastModifiedServer());

            return contentValues;
        }

        @Override
        public String getId(Location location) {
            return location.getUuid();
        }

        @Override
        public DataWrapper toDataWrapper(ContentResolver contentResolver, Location location, String state) {

            DataWrapper dataWrapper = new DataWrapper();
            dataWrapper.setUuid(location.getUuid());
            dataWrapper.setExtId(location.getExtId());
            dataWrapper.setName(location.getName());
            dataWrapper.setLevel(state);

            return dataWrapper;
        }

        @Override
        public String getClientModificationTime(Location entity) {
            return entity.getLastModifiedClient();
        }

    }
}
