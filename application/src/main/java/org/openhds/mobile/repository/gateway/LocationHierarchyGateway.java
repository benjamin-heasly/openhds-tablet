package org.openhds.mobile.repository.gateway;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.model.LocationHierarchy;
import org.openhds.mobile.repository.Converter;
import org.openhds.mobile.repository.DataWrapper;
import org.openhds.mobile.repository.Query;

import static org.openhds.mobile.OpenHDS.HierarchyItems.COLUMN_HIERARCHY_EXTID;
import static org.openhds.mobile.OpenHDS.HierarchyItems.COLUMN_HIERARCHY_LEVEL;
import static org.openhds.mobile.OpenHDS.HierarchyItems.COLUMN_HIERARCHY_NAME;
import static org.openhds.mobile.OpenHDS.HierarchyItems.COLUMN_HIERARCHY_PARENT;
import static org.openhds.mobile.repository.RepositoryUtils.extractString;


/**
 * Convert LocationHierarchy items to and from database.  LocationHierarchy-specific queries.
 */
public class LocationHierarchyGateway extends Gateway<LocationHierarchy> {

    public LocationHierarchyGateway() {
        super(OpenHDS.HierarchyItems.CONTENT_ID_URI_BASE, COLUMN_HIERARCHY_EXTID, new LocationHierarchyConverter());
    }

    public Query findByLevel(String level) {
        return new Query(tableUri, COLUMN_HIERARCHY_LEVEL, level, COLUMN_HIERARCHY_EXTID);
    }

    public Query findByParent(String parentId) {
        return new Query(tableUri, COLUMN_HIERARCHY_PARENT, parentId, COLUMN_HIERARCHY_EXTID);
    }

    private static class LocationHierarchyConverter implements Converter<LocationHierarchy> {

        @Override
        public LocationHierarchy fromCursor(Cursor cursor) {
            LocationHierarchy locationHierarchy = new LocationHierarchy();

            locationHierarchy.setExtId(extractString(cursor, COLUMN_HIERARCHY_EXTID));
            locationHierarchy.setName(extractString(cursor, COLUMN_HIERARCHY_NAME));
            locationHierarchy.setLevel(extractString(cursor, COLUMN_HIERARCHY_LEVEL));
            locationHierarchy.setParent(extractString(cursor, COLUMN_HIERARCHY_PARENT));

            return locationHierarchy;
        }

        @Override
        public ContentValues toContentValues(LocationHierarchy locationHierarchy) {
            ContentValues contentValues = new ContentValues();

            contentValues.put(COLUMN_HIERARCHY_EXTID, locationHierarchy.getExtId());
            contentValues.put(COLUMN_HIERARCHY_NAME, locationHierarchy.getName());
            contentValues.put(COLUMN_HIERARCHY_LEVEL, locationHierarchy.getLevel());
            contentValues.put(COLUMN_HIERARCHY_PARENT, locationHierarchy.getParent());

            return contentValues;
        }

        @Override
        public String getId(LocationHierarchy locationHierarchy) {
            return locationHierarchy.getExtId();
        }

        @Override
        public DataWrapper toQueryResult(ContentResolver contentResolver, LocationHierarchy locationHierarchy, String state) {
            DataWrapper dataWrapper = new DataWrapper();
            dataWrapper.setExtId(locationHierarchy.getExtId());
            dataWrapper.setName(locationHierarchy.getName());
            dataWrapper.setState(state);
            return dataWrapper;
        }
    }
}
