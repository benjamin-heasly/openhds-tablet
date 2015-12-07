package org.openhds.mobile.repository.gateway;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;

import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.forms.FormContent;
import org.openhds.mobile.model.core.Location;
import org.openhds.mobile.repository.Converter;
import org.openhds.mobile.repository.DataWrapper;
import org.openhds.mobile.repository.Query;

import static org.openhds.mobile.OpenHDS.Common.LAST_MODIFIED_CLIENT;
import static org.openhds.mobile.OpenHDS.Common.LAST_MODIFIED_SERVER;
import static org.openhds.mobile.OpenHDS.Locations.DESCRIPTION;
import static org.openhds.mobile.OpenHDS.Locations.EXT_ID;
import static org.openhds.mobile.OpenHDS.Locations.LATITUDE;
import static org.openhds.mobile.OpenHDS.Locations.LOCATION_HIERARCHY_UUID;
import static org.openhds.mobile.OpenHDS.Locations.LONGITUDE;
import static org.openhds.mobile.OpenHDS.Locations.NAME;
import static org.openhds.mobile.OpenHDS.Locations.TYPE;
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
            location.setDescription(extractString(cursor, DESCRIPTION));
            location.setType(extractString(cursor, TYPE));
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
            contentValues.put(DESCRIPTION, location.getDescription());
            contentValues.put(TYPE, location.getType());
            contentValues.put(LAST_MODIFIED_CLIENT, location.getLastModifiedClient());
            contentValues.put(LAST_MODIFIED_SERVER, location.getLastModifiedServer());

            return contentValues;
        }

        @Override
        public ContentValues toContentValues(FormContent formContent, String entityAlias) {
            ContentValues contentValues = new ContentValues();

            contentValues.put(LOCATION_HIERARCHY_UUID, formContent.getContentString(FormContent.TOP_LEVEL_ALIAS, "locationHierarchyUuid"));
            contentValues.put(EXT_ID, formContent.getContentString(entityAlias, EXT_ID));
            contentValues.put(LATITUDE, formContent.getContentString(entityAlias, LATITUDE));
            contentValues.put(LONGITUDE, formContent.getContentString(entityAlias, LONGITUDE));
            contentValues.put(NAME, formContent.getContentString(entityAlias, NAME));
            contentValues.put(DESCRIPTION, formContent.getContentString(entityAlias, DESCRIPTION));
            contentValues.put(TYPE, formContent.getContentString(entityAlias, TYPE));
            contentValues.put(UUID, formContent.getContentString(entityAlias, UUID));

            return contentValues;
        }

        @Override
        public String getDefaultAlias() {
            return "location";
        }

        @Override
        public String getId(Location location) {
            return location.getUuid();
        }

        @Override
        public DataWrapper toDataWrapper(ContentResolver contentResolver, Location entity, String level) {
            return new DataWrapper(entity.getUuid(),
                    entity.getExtId(),
                    entity.getName(),
                    level,
                    Location.class.getSimpleName(),
                    toContentValues(entity));
        }

        @Override
        public String getClientModificationTime(Location entity) {
            return entity.getLastModifiedClient();
        }

    }
}
