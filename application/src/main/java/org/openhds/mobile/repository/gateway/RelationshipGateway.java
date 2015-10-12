package org.openhds.mobile.repository.gateway;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;

import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.model.core.Relationship;
import org.openhds.mobile.repository.Converter;
import org.openhds.mobile.repository.DataWrapper;
import org.openhds.mobile.repository.Query;

import static org.openhds.mobile.OpenHDS.Common.LAST_MODIFIED_CLIENT;
import static org.openhds.mobile.OpenHDS.Common.LAST_MODIFIED_SERVER;
import static org.openhds.mobile.OpenHDS.Relationships.INDIVIDUAL_A_UUID;
import static org.openhds.mobile.OpenHDS.Relationships.INDIVIDUAL_B_UUID;
import static org.openhds.mobile.OpenHDS.Relationships.START_DATE;
import static org.openhds.mobile.OpenHDS.Relationships.TYPE;
import static org.openhds.mobile.OpenHDS.Relationships.UUID;
import static org.openhds.mobile.repository.RepositoryUtils.EQUALS;
import static org.openhds.mobile.repository.RepositoryUtils.extractString;
import static org.openhds.mobile.repository.RepositoryUtils.insert;
import static org.openhds.mobile.repository.RepositoryUtils.update;


/**
 * Convert Relationship to and from database.  Relationship-specific queries.
 *
 *
 */
public class RelationshipGateway extends Gateway<Relationship> {

    public RelationshipGateway() {
        super(OpenHDS.Relationships.CONTENT_ID_URI_BASE, UUID, new RelationshipConverter());
    }

    // true if relationship was inserted, false if updated
    // take care to update at BOTH correct individuals
    @Override
    public boolean insertOrUpdate(ContentResolver contentResolver, Relationship relationship) {
        ContentValues contentValues = toContentValues(relationship);

        String individualAId = relationship.getIndividualA();
        String individualBId = relationship.getIndividualB();
        Relationship existingRelationship = getFirst(contentResolver, findByBothIndividuals(individualAId, individualBId));

        if (null == existingRelationship) {
            return null != insert(contentResolver, tableUri, contentValues);
        } else {
            final String[] columnNames = {INDIVIDUAL_A_UUID, INDIVIDUAL_B_UUID};
            final String[] columnValues = {individualAId, individualBId};
            update(contentResolver, tableUri, contentValues, columnNames, columnValues);
            return false;
        }
    }

    public Query findByBothIndividuals(String individualAId, String individualBId) {
        final String[] columnNames = {INDIVIDUAL_A_UUID, INDIVIDUAL_B_UUID};
        final String[] columnValues = {individualAId, individualBId};
        return new Query(tableUri, columnNames, columnValues, INDIVIDUAL_A_UUID, EQUALS);
    }

    private static class RelationshipConverter implements Converter<Relationship> {

        @Override
        public Relationship fromCursor(Cursor cursor) {
            Relationship relationship = new Relationship();

            relationship.setUuid(extractString(cursor, UUID));
            relationship.setIndividualA(extractString(cursor, INDIVIDUAL_A_UUID));
            relationship.setIndividualB(extractString(cursor, INDIVIDUAL_B_UUID));
            relationship.setStartDate(extractString(cursor, START_DATE));
            relationship.setType(extractString(cursor, TYPE));
            relationship.setLastModifiedClient(extractString(cursor, LAST_MODIFIED_CLIENT));
            relationship.setLastModifiedServer(extractString(cursor, LAST_MODIFIED_SERVER));

            return relationship;
        }

        @Override
        public ContentValues toContentValues(Relationship relationship) {
            ContentValues contentValues = new ContentValues();

            contentValues.put(UUID, relationship.getUuid());
            contentValues.put(INDIVIDUAL_A_UUID, relationship.getIndividualA());
            contentValues.put(INDIVIDUAL_B_UUID, relationship.getIndividualB());
            contentValues.put(START_DATE, relationship.getStartDate());
            contentValues.put(TYPE, relationship.getType());
            contentValues.put(LAST_MODIFIED_CLIENT, relationship.getLastModifiedClient());
            contentValues.put(LAST_MODIFIED_SERVER, relationship.getLastModifiedServer());

            return contentValues;
        }

        @Override
        public String getId(Relationship relationship) {
            return relationship.getUuid();
        }

        @Override
        public DataWrapper toDataWrapper(ContentResolver contentResolver, Relationship relationship, String state) {
            DataWrapper dataWrapper = new DataWrapper();

            dataWrapper.setUuid(relationship.getUuid());
            dataWrapper.setName(relationship.getType());
            dataWrapper.setLevel(state);

            return dataWrapper;
        }

        @Override
        public String getClientModificationTime(Relationship entity) {
            return entity.getLastModifiedClient();
        }

    }
}
