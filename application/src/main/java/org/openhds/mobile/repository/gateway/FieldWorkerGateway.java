package org.openhds.mobile.repository.gateway;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.model.FieldWorker;
import org.openhds.mobile.repository.Converter;
import org.openhds.mobile.repository.DataWrapper;

import static org.openhds.mobile.OpenHDS.FieldWorkers.COLUMN_FIELD_WORKER_EXTID;
import static org.openhds.mobile.OpenHDS.FieldWorkers.COLUMN_FIELD_WORKER_FIRST_NAME;
import static org.openhds.mobile.OpenHDS.FieldWorkers.COLUMN_FIELD_WORKER_LAST_NAME;
import static org.openhds.mobile.OpenHDS.FieldWorkers.COLUMN_FIELD_WORKER_PASSWORD;
import static org.openhds.mobile.repository.RepositoryUtils.extractString;
import static org.openhds.mobile.repository.RepositoryUtils.LIKE_WILD_CARD;


/**
 * Convert FieldWorker to and from database.  FieldWorker-specific queries.
 */
public class FieldWorkerGateway extends Gateway<FieldWorker> {

    public FieldWorkerGateway() {
        super(OpenHDS.FieldWorkers.CONTENT_ID_URI_BASE, COLUMN_FIELD_WORKER_EXTID, new FieldWorkerConverter());
    }

    private static class FieldWorkerConverter implements Converter<FieldWorker> {

        @Override
        public FieldWorker fromCursor(Cursor cursor) {
            FieldWorker fieldWorker = new FieldWorker();

            fieldWorker.setExtId(extractString(cursor, COLUMN_FIELD_WORKER_EXTID));
            fieldWorker.setFirstName(extractString(cursor, COLUMN_FIELD_WORKER_FIRST_NAME));
            fieldWorker.setLastName(extractString(cursor, COLUMN_FIELD_WORKER_LAST_NAME));
            fieldWorker.setPassword(extractString(cursor, COLUMN_FIELD_WORKER_PASSWORD));

            // for Bioko
            int id = cursor.getInt(cursor.getColumnIndex(OpenHDS.FieldWorkers._ID));
            String idString = String.format(LIKE_WILD_CARD + "02d", id);
            fieldWorker.setCollectedIdPrefix(idString);

            return fieldWorker;
        }

        @Override
        public ContentValues toContentValues(FieldWorker fieldWorker) {
            ContentValues contentValues = new ContentValues();

            contentValues.put(COLUMN_FIELD_WORKER_EXTID, fieldWorker.getExtId());
            contentValues.put(COLUMN_FIELD_WORKER_FIRST_NAME, fieldWorker.getFirstName());
            contentValues.put(COLUMN_FIELD_WORKER_LAST_NAME, fieldWorker.getLastName());
            contentValues.put(COLUMN_FIELD_WORKER_PASSWORD, fieldWorker.getPassword());

            return contentValues;
        }

        @Override
        public String getId(FieldWorker fieldWorker) {
            return fieldWorker.getExtId();
        }

        @Override
        public DataWrapper toQueryResult(ContentResolver contentResolver, FieldWorker fieldWorker, String state) {
            DataWrapper dataWrapper = new DataWrapper();
            dataWrapper.setExtId(fieldWorker.getExtId());
            dataWrapper.setName(fieldWorker.getFirstName());
            dataWrapper.setCategory(state);
            return dataWrapper;
        }
    }
}
