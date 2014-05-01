package org.openhds.mobile.database;

import static org.openhds.mobile.OpenHDS.Locations.COLUMN_LOCATION_EXTID;
import static org.openhds.mobile.OpenHDS.Locations.COLUMN_LOCATION_HIERARCHY;
import static org.openhds.mobile.OpenHDS.Locations.COLUMN_LOCATION_LATITUDE;
import static org.openhds.mobile.OpenHDS.Locations.COLUMN_LOCATION_LONGITUDE;
import static org.openhds.mobile.OpenHDS.Locations.COLUMN_LOCATION_NAME;
import static org.openhds.mobile.OpenHDS.Locations.CONTENT_ID_URI_BASE;

import org.openhds.mobile.model.Location;

import android.content.ContentResolver;
import android.content.ContentValues;

public class LocationAdapter {

	public static int update(ContentResolver resolver, Location location) {
		ContentValues cv = new ContentValues();

		cv.put(COLUMN_LOCATION_EXTID, location.getExtId());
		cv.put(COLUMN_LOCATION_HIERARCHY, location.getHierarchyExtId());
		cv.put(COLUMN_LOCATION_LATITUDE, location.getLatitude());
		cv.put(COLUMN_LOCATION_LONGITUDE, location.getLongitude());
		cv.put(COLUMN_LOCATION_NAME, location.getName());

		return resolver.update(CONTENT_ID_URI_BASE, cv, COLUMN_LOCATION_EXTID + " = '" + location.getExtId()
				+ "'", null);
	}
}
