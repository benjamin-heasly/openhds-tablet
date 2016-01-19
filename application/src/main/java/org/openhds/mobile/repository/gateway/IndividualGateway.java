package org.openhds.mobile.repository.gateway;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;

import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.forms.FormContent;
import org.openhds.mobile.model.core.Individual;
import org.openhds.mobile.repository.Converter;
import org.openhds.mobile.repository.DataWrapper;
import org.openhds.mobile.repository.Query;

import java.util.Set;

import static org.openhds.mobile.OpenHDS.Common.LAST_MODIFIED_CLIENT;
import static org.openhds.mobile.OpenHDS.Common.LAST_MODIFIED_SERVER;
import static org.openhds.mobile.OpenHDS.Individuals.DOB;
import static org.openhds.mobile.OpenHDS.Individuals.EXT_ID;
import static org.openhds.mobile.OpenHDS.Individuals.FATHER;
import static org.openhds.mobile.OpenHDS.Individuals.FIRST_NAME;
import static org.openhds.mobile.OpenHDS.Individuals.GENDER;
import static org.openhds.mobile.OpenHDS.Individuals.LAST_NAME;
import static org.openhds.mobile.OpenHDS.Individuals.MIDDLE_NAME;
import static org.openhds.mobile.OpenHDS.Individuals.MOTHER;
import static org.openhds.mobile.OpenHDS.Individuals.UUID;
import static org.openhds.mobile.repository.RepositoryUtils.LIKE;
import static org.openhds.mobile.repository.RepositoryUtils.LIKE_WILD_CARD;
import static org.openhds.mobile.repository.RepositoryUtils.extractString;



/**
 * Convert Individuals to and from database.  Individual-specific queries.
 */
public class IndividualGateway extends Gateway<Individual> {

    public IndividualGateway() {
        super(OpenHDS.Individuals.CONTENT_ID_URI_BASE, UUID, new IndividualConverter());
    }

    @Override
    public Set<String> getColumns() {
        return converter.toContentValues(new Individual()).keySet();
    }

    public Query findByExtIdPrefixDescending(String extIdPrefix) {
        final String[] columnNames = {EXT_ID};
        final String[] columnValues = {extIdPrefix + LIKE_WILD_CARD};
        return new Query(tableUri, columnNames, columnValues, EXT_ID + " DESC", LIKE);
    }

    private static class IndividualConverter implements Converter<Individual> {

        @Override
        public Individual fromCursor(Cursor cursor) {
            Individual individual = new Individual();

            individual.setUuid(extractString(cursor, UUID));
            individual.setExtId(extractString(cursor, EXT_ID));
            individual.setFirstName(extractString(cursor, FIRST_NAME));
            individual.setMiddleName(extractString(cursor, MIDDLE_NAME));
            individual.setLastName(extractString(cursor, LAST_NAME));
            individual.setDob(extractString(cursor, DOB));
            individual.setGender(extractString(cursor, GENDER));
            individual.setMother(extractString(cursor, MOTHER));
            individual.setFather(extractString(cursor, FATHER));
            individual.setLastModifiedClient(extractString(cursor, LAST_MODIFIED_CLIENT));
            individual.setLastModifiedServer(extractString(cursor, LAST_MODIFIED_SERVER));

            return individual;
        }

        @Override
        public ContentValues toContentValues(Individual individual) {
            ContentValues contentValues = new ContentValues();

            contentValues.put(UUID, individual.getUuid());
            contentValues.put(EXT_ID, individual.getExtId());
            contentValues.put(FIRST_NAME, individual.getFirstName());
            contentValues.put(MIDDLE_NAME, individual.getMiddleName());
            contentValues.put(LAST_NAME, individual.getLastName());
            contentValues.put(DOB, individual.getDob());
            contentValues.put(GENDER, individual.getGender());
            contentValues.put(MOTHER, individual.getMother());
            contentValues.put(FATHER, individual.getFather());
            contentValues.put(LAST_MODIFIED_CLIENT, individual.getLastModifiedClient());
            contentValues.put(LAST_MODIFIED_SERVER, individual.getLastModifiedServer());

            return contentValues;
        }

        @Override
        public ContentValues toContentValues(FormContent formContent, String entityAlias) {
            ContentValues contentValues = new ContentValues();

            contentValues.put(EXT_ID, formContent.getContentString(entityAlias, EXT_ID));
            contentValues.put(FIRST_NAME, formContent.getContentString(entityAlias, FIRST_NAME));
            contentValues.put(MIDDLE_NAME, formContent.getContentString(entityAlias, MIDDLE_NAME));
            contentValues.put(LAST_NAME, formContent.getContentString(entityAlias, LAST_NAME));
            contentValues.put(DOB, formContent.getContentString(entityAlias, DOB));
            contentValues.put(GENDER, formContent.getContentString(entityAlias, GENDER));
            contentValues.put(MOTHER, formContent.getContentString("mother", UUID));
            contentValues.put(FATHER, formContent.getContentString("father", UUID));
            contentValues.put(UUID, formContent.getContentString(entityAlias, UUID));

            return contentValues;
        }

        @Override
        public String getDefaultAlias() {
            return "individual";
        }

        @Override
        public String getId(Individual individual) {
            return individual.getUuid();
        }

        @Override
        public DataWrapper toDataWrapper(ContentResolver contentResolver, Individual entity, String level) {
            return new DataWrapper(entity.getUuid(),
                    entity.getExtId(),
                    entity.getFirstName(),
                    level,
                    Individual.class.getSimpleName(),
                    toContentValues(entity));
        }

        @Override
        public String getClientModificationTime(Individual entity) {
            return entity.getLastModifiedClient();
        }

    }
}
