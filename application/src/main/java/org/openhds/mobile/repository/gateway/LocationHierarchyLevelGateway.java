package org.openhds.mobile.repository.gateway;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;

import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.model.core.LocationHierarchyLevel;
import org.openhds.mobile.repository.Converter;
import org.openhds.mobile.repository.DataWrapper;
import org.openhds.mobile.repository.Query;

import static org.openhds.mobile.OpenHDS.Common.LAST_MODIFIED_CLIENT;
import static org.openhds.mobile.OpenHDS.Common.LAST_MODIFIED_SERVER;
import static org.openhds.mobile.repository.RepositoryUtils.EQUALS;
import static org.openhds.mobile.repository.RepositoryUtils.extractInt;
import static org.openhds.mobile.repository.RepositoryUtils.extractString;


/**
 * Convert LocationHierarchyLevel to and from database.  LocationHierarchyLevel-specific queries.
 */
public class LocationHierarchyLevelGateway extends Gateway<LocationHierarchyLevel> {

    public LocationHierarchyLevelGateway() {
        super(OpenHDS.LocationHierarchyLevels.CONTENT_ID_URI_BASE, OpenHDS.LocationHierarchyLevels.UUID, new LocationHierarchyLevelConverter());
    }

    public Query findByKeyIdentifier(String keyIdentifier) {
        final String[] columnNames = {OpenHDS.LocationHierarchyLevels.KEY_IDENTIFIER};
        final String[] columnValues = {keyIdentifier};
        return new Query(tableUri, columnNames, columnValues, OpenHDS.LocationHierarchyLevels.KEY_IDENTIFIER, EQUALS);
    }

    private static class LocationHierarchyLevelConverter implements Converter<LocationHierarchyLevel> {

        @Override
        public LocationHierarchyLevel fromCursor(Cursor cursor) {
            LocationHierarchyLevel locationHierarchyLevel = new LocationHierarchyLevel();

            locationHierarchyLevel.setUuid(extractString(cursor, OpenHDS.LocationHierarchyLevels.UUID));
            locationHierarchyLevel.setName(extractString(cursor, OpenHDS.LocationHierarchyLevels.NAME));
            locationHierarchyLevel.setKeyIdentifier(extractInt(cursor, OpenHDS.LocationHierarchyLevels.KEY_IDENTIFIER));
            locationHierarchyLevel.setLastModifiedClient(extractString(cursor, LAST_MODIFIED_CLIENT));
            locationHierarchyLevel.setLastModifiedServer(extractString(cursor, LAST_MODIFIED_SERVER));

            return locationHierarchyLevel;
        }

        @Override
        public ContentValues toContentValues(LocationHierarchyLevel locationHierarchyLevel) {
            ContentValues contentValues = new ContentValues();

            contentValues.put(OpenHDS.LocationHierarchyLevels.UUID, locationHierarchyLevel.getUuid());
            contentValues.put(OpenHDS.LocationHierarchyLevels.NAME, locationHierarchyLevel.getName());
            contentValues.put(OpenHDS.LocationHierarchyLevels.KEY_IDENTIFIER, locationHierarchyLevel.getKeyIdentifier());
            contentValues.put(LAST_MODIFIED_CLIENT, locationHierarchyLevel.getLastModifiedClient());
            contentValues.put(LAST_MODIFIED_SERVER, locationHierarchyLevel.getLastModifiedServer());

            return contentValues;
        }

        @Override
        public String getId(LocationHierarchyLevel LocationHierarchyLevel) {
            return LocationHierarchyLevel.getUuid();
        }

        @Override
        public DataWrapper toDataWrapper(ContentResolver contentResolver, LocationHierarchyLevel entity, String level) {
            return new DataWrapper(entity.getUuid(),
                    Integer.toString(entity.getKeyIdentifier()),
                    entity.getName(),
                    level,
                    LocationHierarchyLevel.class.getSimpleName(),
                    toContentValues(entity));
        }

        @Override
        public String getClientModificationTime(LocationHierarchyLevel entity) {
            return entity.getLastModifiedClient();
        }

    }
}
