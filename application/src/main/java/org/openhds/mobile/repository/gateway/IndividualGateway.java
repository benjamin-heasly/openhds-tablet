package org.openhds.mobile.repository.gateway;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.model.Individual;
import org.openhds.mobile.model.Location;
import org.openhds.mobile.repository.Converter;
import org.openhds.mobile.repository.Query;

import java.util.List;

import static org.openhds.mobile.OpenHDS.Individuals.COLUMN_INDIVIDUAL_AGE;
import static org.openhds.mobile.OpenHDS.Individuals.COLUMN_INDIVIDUAL_AGE_UNITS;
import static org.openhds.mobile.OpenHDS.Individuals.COLUMN_INDIVIDUAL_DOB;
import static org.openhds.mobile.OpenHDS.Individuals.COLUMN_INDIVIDUAL_EXTID;
import static org.openhds.mobile.OpenHDS.Individuals.COLUMN_INDIVIDUAL_FATHER;
import static org.openhds.mobile.OpenHDS.Individuals.COLUMN_INDIVIDUAL_FIRST_NAME;
import static org.openhds.mobile.OpenHDS.Individuals.COLUMN_INDIVIDUAL_GENDER;
import static org.openhds.mobile.OpenHDS.Individuals.COLUMN_INDIVIDUAL_LANGUAGE_PREFERENCE;
import static org.openhds.mobile.OpenHDS.Individuals.COLUMN_INDIVIDUAL_LAST_NAME;
import static org.openhds.mobile.OpenHDS.Individuals.COLUMN_INDIVIDUAL_MOTHER;
import static org.openhds.mobile.OpenHDS.Individuals.COLUMN_INDIVIDUAL_OTHER_ID;
import static org.openhds.mobile.OpenHDS.Individuals.COLUMN_INDIVIDUAL_OTHER_NAMES;
import static org.openhds.mobile.OpenHDS.Individuals.COLUMN_INDIVIDUAL_OTHER_PHONE_NUMBER;
import static org.openhds.mobile.OpenHDS.Individuals.COLUMN_INDIVIDUAL_PHONE_NUMBER;
import static org.openhds.mobile.OpenHDS.Individuals.COLUMN_INDIVIDUAL_POINT_OF_CONTACT_NAME;
import static org.openhds.mobile.OpenHDS.Individuals.COLUMN_INDIVIDUAL_POINT_OF_CONTACT_PHONE_NUMBER;
import static org.openhds.mobile.OpenHDS.Individuals.COLUMN_INDIVIDUAL_RESIDENCE_END_TYPE;
import static org.openhds.mobile.OpenHDS.Individuals.COLUMN_INDIVIDUAL_RESIDENCE_LOCATION_EXTID;
import static org.openhds.mobile.OpenHDS.Individuals.COLUMN_INDIVIDUAL_STATUS;
import static org.openhds.mobile.repository.RepositoryUtils.LIKE;
import static org.openhds.mobile.repository.RepositoryUtils.extractString;


/**
 * Convert Individuals to and from database.  Individual-specific queries.
 */
public class IndividualGateway extends Gateway<Individual> {

    public IndividualGateway() {
        super(OpenHDS.Individuals.CONTENT_ID_URI_BASE, COLUMN_INDIVIDUAL_EXTID, new IndividualConverter());
    }

    public List<Individual> findByResidency(ContentResolver contentResolver, Location residency) {
        Query query = new Query(
                tableUri, COLUMN_INDIVIDUAL_RESIDENCE_LOCATION_EXTID, residency.getExtId(), COLUMN_INDIVIDUAL_EXTID);
        Cursor cursor = query.select(contentResolver);
        return toList(cursor);
    }

    public List<Individual> findByExtIdPrefix(ContentResolver contentResolver, String extIdPrefix) {
        final String[] columnNames = {COLUMN_INDIVIDUAL_EXTID};
        final String[] columnValues = {extIdPrefix + "%"};
        Query query = new Query(tableUri, columnNames, columnValues, COLUMN_INDIVIDUAL_EXTID, LIKE);
        Cursor cursor = query.select(contentResolver);
        return toList(cursor);
    }

    private static class IndividualConverter implements Converter<Individual> {

        @Override
        public Individual fromCursor(Cursor cursor) {
            Individual individual = new Individual();

            individual.setExtId(extractString(cursor, COLUMN_INDIVIDUAL_EXTID));
            individual.setFirstName(extractString(cursor, COLUMN_INDIVIDUAL_FIRST_NAME));
            individual.setLastName(extractString(cursor, COLUMN_INDIVIDUAL_LAST_NAME));
            individual.setDob(extractString(cursor, COLUMN_INDIVIDUAL_DOB));
            individual.setGender(extractString(cursor, COLUMN_INDIVIDUAL_GENDER));
            individual.setMother(extractString(cursor, COLUMN_INDIVIDUAL_MOTHER));
            individual.setFather(extractString(cursor, COLUMN_INDIVIDUAL_FATHER));
            individual.setCurrentResidence(extractString(cursor, COLUMN_INDIVIDUAL_RESIDENCE_LOCATION_EXTID));
            individual.setEndType(extractString(cursor, COLUMN_INDIVIDUAL_RESIDENCE_END_TYPE));
            individual.setOtherId(extractString(cursor, COLUMN_INDIVIDUAL_OTHER_ID));
            individual.setOtherNames(extractString(cursor, COLUMN_INDIVIDUAL_OTHER_NAMES));
            individual.setAge(extractString(cursor, COLUMN_INDIVIDUAL_AGE));
            individual.setAgeUnits(extractString(cursor, COLUMN_INDIVIDUAL_AGE_UNITS));
            individual.setPhoneNumber(extractString(cursor, COLUMN_INDIVIDUAL_PHONE_NUMBER));
            individual.setOtherPhoneNumber(extractString(cursor, COLUMN_INDIVIDUAL_OTHER_PHONE_NUMBER));
            individual.setPointOfContactName(extractString(cursor, COLUMN_INDIVIDUAL_POINT_OF_CONTACT_NAME));
            individual.setMemberStatus(extractString(cursor, COLUMN_INDIVIDUAL_STATUS));
            individual.setPointOfContactPhoneNumber(extractString(cursor, COLUMN_INDIVIDUAL_POINT_OF_CONTACT_PHONE_NUMBER));
            individual.setLanguagePreference(extractString(cursor, COLUMN_INDIVIDUAL_LANGUAGE_PREFERENCE));

            return individual;
        }

        @Override
        public ContentValues toContentValues(Individual individual) {
            ContentValues contentValues = new ContentValues();

            contentValues.put(COLUMN_INDIVIDUAL_EXTID, individual.getExtId());
            contentValues.put(COLUMN_INDIVIDUAL_FIRST_NAME, individual.getFirstName());
            contentValues.put(COLUMN_INDIVIDUAL_LAST_NAME, individual.getLastName());
            contentValues.put(COLUMN_INDIVIDUAL_DOB, individual.getDob());
            contentValues.put(COLUMN_INDIVIDUAL_GENDER, individual.getGender());
            contentValues.put(COLUMN_INDIVIDUAL_MOTHER, individual.getMother());
            contentValues.put(COLUMN_INDIVIDUAL_FATHER, individual.getFather());
            contentValues.put(COLUMN_INDIVIDUAL_RESIDENCE_LOCATION_EXTID, individual.getCurrentResidence());
            contentValues.put(COLUMN_INDIVIDUAL_RESIDENCE_END_TYPE, individual.getEndType());
            contentValues.put(COLUMN_INDIVIDUAL_OTHER_ID, individual.getOtherId());
            contentValues.put(COLUMN_INDIVIDUAL_OTHER_NAMES, individual.getOtherNames());
            contentValues.put(COLUMN_INDIVIDUAL_AGE, individual.getAge());
            contentValues.put(COLUMN_INDIVIDUAL_AGE_UNITS, individual.getAgeUnits());
            contentValues.put(COLUMN_INDIVIDUAL_PHONE_NUMBER, individual.getPhoneNumber());
            contentValues.put(COLUMN_INDIVIDUAL_OTHER_PHONE_NUMBER, individual.getOtherPhoneNumber());
            contentValues.put(COLUMN_INDIVIDUAL_POINT_OF_CONTACT_NAME, individual.getPointOfContactName());
            contentValues.put(COLUMN_INDIVIDUAL_STATUS, individual.getMemberStatus());
            contentValues.put(COLUMN_INDIVIDUAL_POINT_OF_CONTACT_PHONE_NUMBER, individual.getPointOfContactPhoneNumber());
            contentValues.put(COLUMN_INDIVIDUAL_LANGUAGE_PREFERENCE, individual.getLanguagePreference());

            return contentValues;
        }

        @Override
        public String getId(Individual individual) {
            return individual.getExtId();
        }
    }
}
