package org.openhds.mobile.repository.gateway;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;

import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.model.core.Residency;
import org.openhds.mobile.repository.Converter;
import org.openhds.mobile.repository.DataWrapper;
import org.openhds.mobile.repository.Query;

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

    // true if residency was inserted, false if updated
    // take care to update at the correct location AND individual
    @Override
    public boolean insertOrUpdate(ContentResolver contentResolver, Residency residency) {
        ContentValues contentValues = converter.toContentValues(residency);

        String locationId = residency.getLocationUuid();
        String individualId = residency.getIndividualUuid();
        Residency existingResidency = getFirst(contentResolver, findByLocationAndIndividual(locationId, individualId));

        if (null == existingResidency) {
            return null != insert(contentResolver, tableUri, contentValues);
        } else {
            final String[] columnNames = {LOCATION_UUID, INDIVIDUAL_UUID};
            final String[] columnValues = {locationId, individualId};
            update(contentResolver, tableUri, contentValues, columnNames, columnValues);
            return false;
        }
    }

    public Query findByIndividual(String individualId) {
        return new Query(tableUri, INDIVIDUAL_UUID, individualId, INDIVIDUAL_UUID);
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

            return residency;
        }

        @Override
        public ContentValues toContentValues(Residency residency) {
            ContentValues contentValues = new ContentValues();

            contentValues.put(UUID, residency.getUuid());
            contentValues.put(INDIVIDUAL_UUID, residency.getIndividualUuid());
            contentValues.put(LOCATION_UUID, residency.getLocationUuid());
            contentValues.put(END_TYPE, residency.getEndType());

            return contentValues;
        }

        @Override
        public String getId(Residency residency) {
            return residency.getUuid();
        }

        @Override
        public DataWrapper toDataWrapper(ContentResolver contentResolver, Residency residency, String state) {
            DataWrapper dataWrapper = new DataWrapper();
            dataWrapper.setUuid(residency.getUuid());
            dataWrapper.setCategory(state);
            return dataWrapper;
        }
    }
}
