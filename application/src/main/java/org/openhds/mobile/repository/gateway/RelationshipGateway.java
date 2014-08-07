package org.openhds.mobile.repository.gateway;

import android.content.ContentValues;
import android.database.Cursor;
import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.model.Relationship;
import org.openhds.mobile.repository.Converter;

import static org.openhds.mobile.OpenHDS.Relationships.COLUMN_RELATIONSHIP_INDIVIDUAL_A;
import static org.openhds.mobile.OpenHDS.Relationships.COLUMN_RELATIONSHIP_INDIVIDUAL_B;
import static org.openhds.mobile.OpenHDS.Relationships.COLUMN_RELATIONSHIP_STARTDATE;
import static org.openhds.mobile.OpenHDS.Relationships.COLUMN_RELATIONSHIP_TYPE;
import static org.openhds.mobile.repository.RepositoryUtils.extractString;


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
