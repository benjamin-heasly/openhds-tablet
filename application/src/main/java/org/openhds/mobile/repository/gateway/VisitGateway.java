package org.openhds.mobile.repository.gateway;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;

import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.model.update.Visit;
import org.openhds.mobile.repository.Converter;
import org.openhds.mobile.repository.DataWrapper;

import static org.openhds.mobile.OpenHDS.Visits.DATE;
import static org.openhds.mobile.OpenHDS.Visits.EXT_ID;
import static org.openhds.mobile.OpenHDS.Visits.FIELD_WORKER_UUID;
import static org.openhds.mobile.OpenHDS.Visits.LOCATION_UUID;
import static org.openhds.mobile.OpenHDS.Visits.UUID;
import static org.openhds.mobile.repository.RepositoryUtils.extractString;


/**
 * Convert Visit to and from database.  Visit-specific queries.
 */
public class VisitGateway extends Gateway<Visit> {

    public VisitGateway() {
        super(OpenHDS.Visits.CONTENT_ID_URI_BASE, OpenHDS.Visits.UUID, new VisitConverter());
    }

    private static class VisitConverter implements Converter<Visit> {

        @Override
        public Visit fromCursor(Cursor cursor) {
            Visit visit = new Visit();

            visit.setUuid(extractString(cursor, UUID));
            visit.setExtId(extractString(cursor, EXT_ID));
            visit.setVisitDate(extractString(cursor, DATE));
            visit.setLocationUuid(extractString(cursor, LOCATION_UUID));
            visit.setFieldWorkerUuid(extractString(cursor, FIELD_WORKER_UUID));

            return visit;
        }

        @Override
        public ContentValues toContentValues(Visit visit) {
            ContentValues contentValues = new ContentValues();

            contentValues.put(EXT_ID, visit.getExtId());
            contentValues.put(UUID, visit.getUuid());
            contentValues.put(DATE, visit.getVisitDate());
            contentValues.put(LOCATION_UUID, visit.getLocationUuid());
            contentValues.put(FIELD_WORKER_UUID, visit.getFieldWorkerUuid());

            return contentValues;
        }

        @Override
        public String getId(Visit visit) {
            return visit.getUuid();
        }

        @Override
        public DataWrapper toDataWrapper(ContentResolver contentResolver, Visit visit, String state) {
            DataWrapper dataWrapper = new DataWrapper();

            dataWrapper.setUuid(visit.getUuid());
            dataWrapper.setExtId(visit.getExtId());
            dataWrapper.setName(visit.getLocationUuid());
            dataWrapper.setCategory(state);

            return dataWrapper;
        }
    }
}
