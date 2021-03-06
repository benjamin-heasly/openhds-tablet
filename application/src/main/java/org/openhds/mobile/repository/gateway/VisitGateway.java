package org.openhds.mobile.repository.gateway;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;

import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.forms.FormContent;
import org.openhds.mobile.model.update.Visit;
import org.openhds.mobile.repository.Converter;
import org.openhds.mobile.repository.DataWrapper;

import java.util.Set;

import static org.openhds.mobile.OpenHDS.Common.LAST_MODIFIED_CLIENT;
import static org.openhds.mobile.OpenHDS.Common.LAST_MODIFIED_SERVER;
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

    @Override
    public Set<String> getColumns() {
        return converter.toContentValues(new Visit()).keySet();
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
            visit.setLastModifiedClient(extractString(cursor, LAST_MODIFIED_CLIENT));
            visit.setLastModifiedServer(extractString(cursor, LAST_MODIFIED_SERVER));

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
            contentValues.put(LAST_MODIFIED_CLIENT, visit.getLastModifiedClient());
            contentValues.put(LAST_MODIFIED_SERVER, visit.getLastModifiedServer());

            return contentValues;
        }

        @Override
        public ContentValues toContentValues(FormContent formContent, String entityAlias) {
            ContentValues contentValues = new ContentValues();

            contentValues.put(LOCATION_UUID, formContent.getContentString(entityAlias, "locationUuid"));
            contentValues.put(FIELD_WORKER_UUID, formContent.getContentString(entityAlias, "collectedByUuid"));
            contentValues.put(EXT_ID, formContent.getContentString(entityAlias, EXT_ID));
            contentValues.put(DATE, formContent.getContentString(entityAlias, DATE));
            contentValues.put(UUID, formContent.getContentString(entityAlias, UUID));

            return contentValues;
        }

        @Override
        public String getDefaultAlias() {
            return "visit";
        }

        @Override
        public String getId(Visit visit) {
            return visit.getUuid();
        }

        @Override
        public DataWrapper toDataWrapper(ContentResolver contentResolver, Visit entity, String level) {
            return new DataWrapper(entity.getUuid(),
                    entity.getExtId(),
                    entity.getExtId(),
                    level,
                    Visit.class.getSimpleName(),
                    toContentValues(entity));
        }

        @Override
        public String getClientModificationTime(Visit entity) {
            return entity.getLastModifiedClient();
        }
    }
}
