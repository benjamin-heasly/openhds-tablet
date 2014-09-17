package org.openhds.mobile.repository.gateway;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.model.Visit;
import org.openhds.mobile.repository.Converter;
import org.openhds.mobile.repository.DataWrapper;

import static org.openhds.mobile.OpenHDS.Visits.COLUMN_VISIT_DATE;
import static org.openhds.mobile.OpenHDS.Visits.COLUMN_VISIT_EXTID;
import static org.openhds.mobile.OpenHDS.Visits.COLUMN_VISIT_FIELDWORKER_EXTID;
import static org.openhds.mobile.OpenHDS.Visits.COLUMN_VISIT_LOCATION_EXTID;
import static org.openhds.mobile.repository.RepositoryUtils.extractString;


/**
 * Convert Visit to and from database.  Visit-specific queries.
 */
public class VisitGateway extends Gateway<Visit> {

    public VisitGateway() {
        super(OpenHDS.Visits.CONTENT_ID_URI_BASE, COLUMN_VISIT_EXTID, new VisitConverter());
    }

    private static class VisitConverter implements Converter<Visit> {

        @Override
        public Visit fromCursor(Cursor cursor) {
            Visit visit = new Visit();

            visit.setVisitExtId(extractString(cursor, COLUMN_VISIT_EXTID));
            visit.setVisitDate(extractString(cursor, COLUMN_VISIT_DATE));
            visit.setLocationExtId(extractString(cursor, COLUMN_VISIT_LOCATION_EXTID));
            visit.setFieldWorkerExtId(extractString(cursor, COLUMN_VISIT_FIELDWORKER_EXTID));

            return visit;
        }

        @Override
        public ContentValues toContentValues(Visit visit) {
            ContentValues contentValues = new ContentValues();

            contentValues.put(COLUMN_VISIT_EXTID, visit.getVisitExtId());
            contentValues.put(COLUMN_VISIT_DATE, visit.getVisitDate());
            contentValues.put(COLUMN_VISIT_LOCATION_EXTID, visit.getLocationExtId());
            contentValues.put(COLUMN_VISIT_FIELDWORKER_EXTID, visit.getFieldWorkerExtId());

            return contentValues;
        }

        @Override
        public String getId(Visit visit) {
            return visit.getVisitExtId();
        }

        @Override
        public DataWrapper toQueryResult(ContentResolver contentResolver, Visit visit, String state) {
            DataWrapper dataWrapper = new DataWrapper();
            dataWrapper.setExtId(visit.getVisitExtId());
            dataWrapper.setName(visit.getLocationExtId());
            dataWrapper.setCategory(state);
            return dataWrapper;
        }
    }
}
