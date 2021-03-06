package org.openhds.mobile.repository.gateway;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;

import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.forms.FormContent;
import org.openhds.mobile.model.core.Relationship;
import org.openhds.mobile.repository.Converter;
import org.openhds.mobile.repository.DataWrapper;
import org.openhds.mobile.repository.Query;

import java.util.Set;

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

    @Override
    public Set<String> getColumns() {
        return converter.toContentValues(new Relationship()).keySet();
    }

    // take care to update at BOTH correct individuals
    @Override
    public Relationship insertOrUpdate(ContentResolver contentResolver, ContentValues contentValues) {
        if (null == contentValues || !contentValues.containsKey(INDIVIDUAL_A_UUID) || !contentValues.containsKey(INDIVIDUAL_B_UUID)) {
            return null;
        }

        String locationId = contentValues.getAsString(INDIVIDUAL_A_UUID);
        String individualId = contentValues.getAsString(INDIVIDUAL_B_UUID);
        Relationship existingResidency = getFirst(contentResolver, findByBothIndividuals(locationId, individualId));

        if (null == existingResidency) {
            insert(contentResolver, tableUri, contentValues);
        } else {
            final String[] columnNames = {INDIVIDUAL_A_UUID, INDIVIDUAL_B_UUID};
            final String[] columnValues = {locationId, individualId};
            update(contentResolver, tableUri, contentValues, columnNames, columnValues);
        }

        return getFirst(contentResolver, findByBothIndividuals(locationId, individualId));
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
        public ContentValues toContentValues(FormContent formContent, String entityAlias) {
            ContentValues contentValues = new ContentValues();

            contentValues.put(INDIVIDUAL_A_UUID, formContent.getContentString("individualA", UUID));
            contentValues.put(INDIVIDUAL_B_UUID, formContent.getContentString("individualB", UUID));
            contentValues.put(START_DATE, formContent.getContentString(entityAlias, START_DATE));
            contentValues.put(TYPE, formContent.getContentString(entityAlias, TYPE));
            contentValues.put(UUID, formContent.getContentString(entityAlias, UUID));

            return contentValues;
        }

        @Override
        public String getDefaultAlias() {
            return "relationship";
        }


        @Override
        public String getId(Relationship relationship) {
            return relationship.getUuid();
        }

        @Override
        public DataWrapper toDataWrapper(ContentResolver contentResolver, Relationship entity, String level) {
            return new DataWrapper(entity.getUuid(),
                    entity.getIndividualA(),
                    entity.getIndividualB(),
                    level,
                    Relationship.class.getSimpleName(),
                    toContentValues(entity));
        }

        @Override
        public String getClientModificationTime(Relationship entity) {
            return entity.getLastModifiedClient();
        }

    }
}
