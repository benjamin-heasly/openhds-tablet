package org.openhds.mobile.database;

import org.openhds.mobile.model.Location;

import android.content.ContentResolver;
import android.content.ContentValues;

import static org.openhds.mobile.OpenHDS.Locations.*;

public class LocationAdapter {

	public static int update(ContentResolver resolver, Location location) {
		ContentValues cv = new ContentValues();

		cv.put(COLUMN_LOCATION_EXTID, location.getExtId());
		cv.put(COLUMN_LOCATION_HIERARCHY, location.getHierarchyExtId());
		cv.put(COLUMN_LOCATION_LATITUDE, location.getLatitude());
		cv.put(COLUMN_LOCATION_LONGITUDE, location.getLongitude());
		cv.put(COLUMN_LOCATION_NAME, location.getName());
        cv.put(COLUMN_LOCATION_SECTOR_NAME, location.getSectorName());
        cv.put(COLUMN_LOCATION_LOCALITY_NAME, location.getLocalityName());
        cv.put(COLUMN_LOCATION_COMMUNITY_NAME, location.getCommunityName());

		return resolver.update(CONTENT_ID_URI_BASE, cv, COLUMN_LOCATION_EXTID + " = '" + location.getExtId()
				+ "'", null);
	}
}
