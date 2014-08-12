package org.openhds.mobile.repository.gateway;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.model.Membership;
import org.openhds.mobile.model.Relationship;
import org.openhds.mobile.repository.Converter;
import org.openhds.mobile.repository.Query;

import static org.openhds.mobile.OpenHDS.Memberships.COLUMN_INDIVIDUAL_EXTID;
import static org.openhds.mobile.OpenHDS.Memberships.COLUMN_SOCIAL_GROUP_EXTID;
import static org.openhds.mobile.OpenHDS.Relationships.COLUMN_RELATIONSHIP_INDIVIDUAL_A;
import static org.openhds.mobile.OpenHDS.Relationships.COLUMN_RELATIONSHIP_INDIVIDUAL_B;
import static org.openhds.mobile.OpenHDS.Relationships.COLUMN_RELATIONSHIP_STARTDATE;
import static org.openhds.mobile.OpenHDS.Relationships.COLUMN_RELATIONSHIP_TYPE;
import static org.openhds.mobile.repository.RepositoryUtils.EQUALS;
import static org.openhds.mobile.repository.RepositoryUtils.extractString;
import static org.openhds.mobile.repository.RepositoryUtils.insert;
import static org.openhds.mobile.repository.RepositoryUtils.update;


/**
 * Convert Relationship to and from database.  Relationship-specific queries.
 *
 * Uses the extId of individualA as the id for the Relationship.
 *
 */
public class RelationshipGateway extends Gateway<Relationship> {

    public RelationshipGateway() {
        super(OpenHDS.Relationships.CONTENT_ID_URI_BASE, COLUMN_RELATIONSHIP_INDIVIDUAL_A, new RelationshipConverter());
    }

    // true if relationship was inserted, false if updated
    // take care to update at BOTH correct individuals
    @Override
    public boolean insertOrUpdate(ContentResolver contentResolver, Relationship relationship) {
        ContentValues contentValues = converter.toContentValues(relationship);

        String individualAId = relationship.getIndividualA();
        String individualBId = relationship.getIndividualB();
        Relationship existingRelationship = getFirst(contentResolver,
                findByBothIndividuals(individualAId, individualBId));

        if (null == existingRelationship) {
            return null != insert(contentResolver, tableUri, contentValues);
        } else {
            final String[] columnNames = {COLUMN_RELATIONSHIP_INDIVIDUAL_A, COLUMN_RELATIONSHIP_INDIVIDUAL_B};
            final String[] columnValues = {individualAId, individualBId};
            update(contentResolver, tableUri, contentValues, columnNames, columnValues);
            return false;
        }
    }

    public Query findByBothIndividuals(String individualAId, String individualBId) {
        final String[] columnNames = {COLUMN_RELATIONSHIP_INDIVIDUAL_A, COLUMN_RELATIONSHIP_INDIVIDUAL_B};
        final String[] columnValues = {individualAId, individualBId};
        return new Query(tableUri, columnNames, columnValues, COLUMN_RELATIONSHIP_INDIVIDUAL_A, EQUALS);
    }


    private static class RelationshipConverter implements Converter<Relationship> {

        @Override
        public Relationship fromCursor(Cursor cursor) {
            Relationship relationship = new Relationship();

            relationship.setIndividualA(extractString(cursor, COLUMN_RELATIONSHIP_INDIVIDUAL_A));
            relationship.setIndividualB(extractString(cursor, COLUMN_RELATIONSHIP_INDIVIDUAL_B));
            relationship.setStartDate(extractString(cursor, COLUMN_RELATIONSHIP_STARTDATE));
            relationship.setType(extractString(cursor, COLUMN_RELATIONSHIP_TYPE));

            return relationship;
        }

        @Override
        public ContentValues toContentValues(Relationship relationship) {
            ContentValues contentValues = new ContentValues();

            contentValues.put(COLUMN_RELATIONSHIP_INDIVIDUAL_A, relationship.getIndividualA());
            contentValues.put(COLUMN_RELATIONSHIP_INDIVIDUAL_B, relationship.getIndividualB());
            contentValues.put(COLUMN_RELATIONSHIP_STARTDATE, relationship.getStartDate());
            contentValues.put(COLUMN_RELATIONSHIP_TYPE, relationship.getType());

            return contentValues;
        }

        @Override
        public String getId(Relationship relationship) {
            return relationship.getIndividualA();
        }
    }
}
