package org.openhds.mobile.repository.gateway;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;

import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.forms.FormContent;
import org.openhds.mobile.model.core.Residency;
import org.openhds.mobile.repository.Converter;
import org.openhds.mobile.repository.DataWrapper;
import org.openhds.mobile.repository.Query;

import java.util.Set;

import static org.openhds.mobile.OpenHDS.Common.LAST_MODIFIED_CLIENT;
import static org.openhds.mobile.OpenHDS.Common.LAST_MODIFIED_SERVER;
import static org.openhds.mobile.OpenHDS.Residencies.END_TYPE;
import static org.openhds.mobile.OpenHDS.Residencies.INDIVIDUAL_UUID;
import static org.openhds.mobile.OpenHDS.Residencies.LOCATION_UUID;
import static org.openhds.mobile.OpenHDS.Residencies.UUID;
import static org.openhds.mobile.repository.RepositoryUtils.EQUALS;
import static org.openhds.mobile.repository.RepositoryUtils.extractString;
import static org.openhds.mobile.repository.RepositoryUtils.insert;
import static org.openhds.mobile.repository.RepositoryUtils.update;

/**
 * Convert Residency to and from database.  Residency-specific queries.
 *
 */
public class ResidencyGateway extends Gateway<Residency> {

    public ResidencyGateway() {
        super(OpenHDS.Residencies.CONTENT_ID_URI_BASE, UUID, new ResidencyConverter());
    }

    @Override
    public Set<String> getColumns() {
        return converter.toContentValues(new Residency()).keySet();
    }

    // take care to update at the correct location AND individual
    @Override
    public Residency insertOrUpdate(ContentResolver contentResolver, ContentValues contentValues) {
        if (null == contentValues || !contentValues.containsKey(LOCATION_UUID) || !contentValues.containsKey(INDIVIDUAL_UUID)) {
            return null;
        }

        String locationId = contentValues.getAsString(LOCATION_UUID);
        String individualId = contentValues.getAsString(INDIVIDUAL_UUID);
        Residency existingResidency = getFirst(contentResolver, findByLocationAndIndividual(locationId, individualId));

        if (null == existingResidency) {
            insert(contentResolver, tableUri, contentValues);
        } else {
            final String[] columnNames = {LOCATION_UUID, INDIVIDUAL_UUID};
            final String[] columnValues = {locationId, individualId};
            update(contentResolver, tableUri, contentValues, columnNames, columnValues);
        }

        return getFirst(contentResolver, findByLocationAndIndividual(locationId, individualId));
    }

    public Query findByIndividual(String individualId) {
        return new Query(tableUri, INDIVIDUAL_UUID, individualId, INDIVIDUAL_UUID);
    }

    public Query findByLocation(String locationId) {
        return new Query(tableUri, LOCATION_UUID, locationId, LOCATION_UUID);
    }

    public Query findByLocationAndIndividual(String locationId, String individualId) {
        final String[] columnNames = {LOCATION_UUID, INDIVIDUAL_UUID};
        final String[] columnValues = {locationId, individualId};
        return new Query(tableUri, columnNames, columnValues, INDIVIDUAL_UUID, EQUALS);
    }

    private static class ResidencyConverter implements Converter<Residency> {

        @Override
        public Residency fromCursor(Cursor cursor) {
            Residency residency = new Residency();

            residency.setUuid(extractString(cursor, UUID));
            residency.setIndividualUuid(extractString(cursor, INDIVIDUAL_UUID));
            residency.setLocationUuid(extractString(cursor, LOCATION_UUID));
            residency.setEndType(extractString(cursor, END_TYPE));
            residency.setLastModifiedClient(extractString(cursor, LAST_MODIFIED_CLIENT));
            residency.setLastModifiedServer(extractString(cursor, LAST_MODIFIED_SERVER));

            return residency;
        }

        @Override
        public ContentValues toContentValues(Residency residency) {
            ContentValues contentValues = new ContentValues();

            contentValues.put(UUID, residency.getUuid());
            contentValues.put(INDIVIDUAL_UUID, residency.getIndividualUuid());
            contentValues.put(LOCATION_UUID, residency.getLocationUuid());
            contentValues.put(END_TYPE, residency.getEndType());
            contentValues.put(LAST_MODIFIED_CLIENT, residency.getLastModifiedClient());
            contentValues.put(LAST_MODIFIED_SERVER, residency.getLastModifiedServer());

            return contentValues;
        }

        @Override
        public ContentValues toContentValues(FormContent formContent, String entityAlias) {
            ContentValues contentValues = new ContentValues();

            contentValues.put(INDIVIDUAL_UUID, formContent.getContentString("individual", UUID));
            contentValues.put(LOCATION_UUID, formContent.getContentString("location", UUID));
            contentValues.put(END_TYPE, formContent.getContentString(entityAlias, END_TYPE));
            contentValues.put(UUID, formContent.getContentString(entityAlias, UUID));

            return contentValues;
        }

        @Override
        public String getDefaultAlias() {
            return "residency";
        }


        @Override
        public String getId(Residency residency) {
            return residency.getUuid();
        }

        @Override
        public DataWrapper toDataWrapper(ContentResolver contentResolver, Residency entity, String level) {
            return new DataWrapper(entity.getUuid(),
                    entity.getIndividualUuid(),
                    entity.getLocationUuid(),
                    level,
                    Residency.class.getSimpleName(),
                    toContentValues(entity));
        }

        @Override
        public String getClientModificationTime(Residency entity) {
            return entity.getLastModifiedClient();
        }

    }
}
