package org.openhds.mobile.repository.gateway;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;

import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.forms.FormContent;
import org.openhds.mobile.model.core.FieldWorker;
import org.openhds.mobile.repository.Converter;
import org.openhds.mobile.repository.DataWrapper;
import org.openhds.mobile.repository.Query;

import java.util.Set;

import static org.openhds.mobile.OpenHDS.Common.LAST_MODIFIED_CLIENT;
import static org.openhds.mobile.OpenHDS.Common.LAST_MODIFIED_SERVER;
import static org.openhds.mobile.OpenHDS.FieldWorkers.FIELD_WORKER_ID;
import static org.openhds.mobile.OpenHDS.FieldWorkers.FIRST_NAME;
import static org.openhds.mobile.OpenHDS.FieldWorkers.LAST_NAME;
import static org.openhds.mobile.OpenHDS.FieldWorkers.PASSWORD_HASH;
import static org.openhds.mobile.OpenHDS.FieldWorkers.UUID;
import static org.openhds.mobile.repository.RepositoryUtils.extractString;


/**
 * Convert FieldWorker to and from database.  FieldWorker-specific queries.
 */
public class FieldWorkerGateway extends Gateway<FieldWorker> {

    public FieldWorkerGateway() {
        super(OpenHDS.FieldWorkers.CONTENT_ID_URI_BASE, UUID, new FieldWorkerConverter());
    }

    @Override
    public Set<String> getColumns() {
        return converter.toContentValues(new FieldWorker()).keySet();
    }

    public Query findByExtId(String extId) {
        return new Query(tableUri, FIELD_WORKER_ID, extId, UUID);
    }

    private static class FieldWorkerConverter implements Converter<FieldWorker> {

        @Override
        public FieldWorker fromCursor(Cursor cursor) {
            FieldWorker fieldWorker = new FieldWorker();

            fieldWorker.setUuid(extractString(cursor, UUID));
            fieldWorker.setFieldWorkerId(extractString(cursor, FIELD_WORKER_ID));
            fieldWorker.setFirstName(extractString(cursor, FIRST_NAME));
            fieldWorker.setLastName(extractString(cursor, LAST_NAME));
            fieldWorker.setPasswordHash(extractString(cursor, PASSWORD_HASH));
            fieldWorker.setLastModifiedClient(extractString(cursor, LAST_MODIFIED_CLIENT));
            fieldWorker.setLastModifiedServer(extractString(cursor, LAST_MODIFIED_SERVER));

            return fieldWorker;
        }

        @Override
        public ContentValues toContentValues(FieldWorker fieldWorker) {
            ContentValues contentValues = new ContentValues();

            contentValues.put(FIELD_WORKER_ID, fieldWorker.getFieldWorkerId());
            contentValues.put(FIRST_NAME, fieldWorker.getFirstName());
            contentValues.put(LAST_NAME, fieldWorker.getLastName());
            contentValues.put(PASSWORD_HASH, fieldWorker.getPasswordHash());
            contentValues.put(UUID, fieldWorker.getUuid());
            contentValues.put(LAST_MODIFIED_CLIENT, fieldWorker.getLastModifiedClient());
            contentValues.put(LAST_MODIFIED_SERVER, fieldWorker.getLastModifiedServer());

            return contentValues;
        }

        @Override
        public ContentValues toContentValues(FormContent formContent, String entityAlias) {
            ContentValues contentValues = new ContentValues();

            contentValues.put(FIELD_WORKER_ID, formContent.getContentString(entityAlias, FIELD_WORKER_ID));
            contentValues.put(FIRST_NAME, formContent.getContentString(entityAlias, FIRST_NAME));
            contentValues.put(LAST_NAME, formContent.getContentString(entityAlias, LAST_NAME));
            contentValues.put(PASSWORD_HASH, formContent.getContentString(entityAlias, PASSWORD_HASH));
            contentValues.put(UUID, formContent.getContentString(entityAlias, UUID));

            return contentValues;
        }

        @Override
        public String getDefaultAlias() {
            return "fieldWorker";
        }

        @Override
        public String getId(FieldWorker fieldWorker) {
            return fieldWorker.getUuid();
        }

        @Override
        public DataWrapper toDataWrapper(ContentResolver contentResolver, FieldWorker entity, String level) {
            return new DataWrapper(entity.getUuid(),
                    entity.getFieldWorkerId(),
                    entity.getFirstName(),
                    level,
                    FieldWorker.class.getSimpleName(),
                    toContentValues(entity));
        }

        @Override
        public String getClientModificationTime(FieldWorker entity) {
            return entity.getLastModifiedClient();
        }
    }
}
