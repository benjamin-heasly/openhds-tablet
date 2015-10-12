package org.openhds.mobile.repository.gateway;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;

import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.model.core.LocationHierarchy;
import org.openhds.mobile.repository.Converter;
import org.openhds.mobile.repository.DataWrapper;
import org.openhds.mobile.repository.Query;

import static org.openhds.mobile.OpenHDS.Common.LAST_MODIFIED_CLIENT;
import static org.openhds.mobile.OpenHDS.Common.LAST_MODIFIED_SERVER;
import static org.openhds.mobile.OpenHDS.LocationHierarchies.EXT_ID;
import static org.openhds.mobile.OpenHDS.LocationHierarchies.LOCATION_HIERARCHY_LEVEL_UUID;
import static org.openhds.mobile.OpenHDS.LocationHierarchies.NAME;
import static org.openhds.mobile.OpenHDS.LocationHierarchies.PARENT_UUID;
import static org.openhds.mobile.OpenHDS.LocationHierarchies.UUID;
import static org.openhds.mobile.repository.RepositoryUtils.extractString;


/**
 * Convert LocationHierarchy items to and from database.  LocationHierarchy-specific queries.
 */
public class LocationHierarchyGateway extends Gateway<LocationHierarchy> {

    public LocationHierarchyGateway() {
        super(OpenHDS.LocationHierarchies.CONTENT_ID_URI_BASE, UUID, new LocationHierarchyConverter());
    }

    public Query findByLevel(String level) {
        return new Query(tableUri, LOCATION_HIERARCHY_LEVEL_UUID, level, UUID);
    }

    public Query findByExtId(String extId) {
        return new Query(tableUri, EXT_ID, extId, UUID);
    }

    public Query findByParent(String parentId) {
        return new Query(tableUri, PARENT_UUID, parentId, UUID);
    }

    private static class LocationHierarchyConverter implements Converter<LocationHierarchy> {

        @Override
        public LocationHierarchy fromCursor(Cursor cursor) {
            LocationHierarchy locationHierarchy = new LocationHierarchy();

            locationHierarchy.setUuid(extractString(cursor, UUID));
            locationHierarchy.setExtId(extractString(cursor, EXT_ID));
            locationHierarchy.setName(extractString(cursor, NAME));
            locationHierarchy.setLevelUuid(extractString(cursor, LOCATION_HIERARCHY_LEVEL_UUID));
            locationHierarchy.setParentUuid(extractString(cursor, PARENT_UUID));
            locationHierarchy.setLastModifiedClient(extractString(cursor, LAST_MODIFIED_CLIENT));
            locationHierarchy.setLastModifiedServer(extractString(cursor, LAST_MODIFIED_SERVER));

            return locationHierarchy;
        }

        @Override
        public ContentValues toContentValues(LocationHierarchy locationHierarchy) {
            ContentValues contentValues = new ContentValues();

            contentValues.put(UUID, locationHierarchy.getUuid());
            contentValues.put(EXT_ID, locationHierarchy.getExtId());
            contentValues.put(NAME, locationHierarchy.getName());
            contentValues.put(LOCATION_HIERARCHY_LEVEL_UUID, locationHierarchy.getLevelUuid());
            contentValues.put(PARENT_UUID, locationHierarchy.getParentUuid());
            contentValues.put(LAST_MODIFIED_CLIENT, locationHierarchy.getLastModifiedClient());
            contentValues.put(LAST_MODIFIED_SERVER, locationHierarchy.getLastModifiedServer());

            return contentValues;
        }

        @Override
        public String getId(LocationHierarchy locationHierarchy) {
            return locationHierarchy.getUuid();
        }

        @Override
        public DataWrapper toDataWrapper(ContentResolver contentResolver, LocationHierarchy locationHierarchy, String state) {
            DataWrapper dataWrapper = new DataWrapper();
            dataWrapper.setExtId(locationHierarchy.getExtId());
            dataWrapper.setUuid(locationHierarchy.getUuid());
            dataWrapper.setName(locationHierarchy.getName());
            dataWrapper.setLevel(state);
            return dataWrapper;
        }

        @Override
        public String getClientModificationTime(LocationHierarchy entity) {
            return entity.getLastModifiedClient();
        }

    }
}
