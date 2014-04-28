package org.openhds.mobile.database;

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
import static org.openhds.mobile.OpenHDS.Individuals.COLUMN_INDIVIDUAL_RESIDENCE_END_TYPE;
import static org.openhds.mobile.OpenHDS.Individuals.COLUMN_INDIVIDUAL_RESIDENCE_LOCATION_EXTID;
import static org.openhds.mobile.OpenHDS.Individuals.CONTENT_ID_URI_BASE;

import java.util.Map;

import org.openhds.mobile.model.Individual;
import org.openhds.mobile.projectdata.ProjectFormFields;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;

public class IndividualAdapter {

	public static Individual create(Map<String, String> formInstanceData) {
		Individual individual = new Individual();

		individual.setExtId(formInstanceData.get(ProjectFormFields.Individuals
				.getFieldNameFromColumn(COLUMN_INDIVIDUAL_EXTID)));
		individual.setFirstName(formInstanceData.get(ProjectFormFields.Individuals
				.getFieldNameFromColumn(COLUMN_INDIVIDUAL_FIRST_NAME)));
		individual.setLastName(formInstanceData.get(ProjectFormFields.Individuals
				.getFieldNameFromColumn(COLUMN_INDIVIDUAL_LAST_NAME)));
		individual.setDob(formInstanceData.get(ProjectFormFields.Individuals
				.getFieldNameFromColumn(COLUMN_INDIVIDUAL_DOB)));
		individual.setGender(formInstanceData.get(ProjectFormFields.Individuals
				.getFieldNameFromColumn(COLUMN_INDIVIDUAL_GENDER)));
		individual.setMother(formInstanceData.get(ProjectFormFields.Individuals
				.getFieldNameFromColumn(COLUMN_INDIVIDUAL_MOTHER)));
		individual.setFather(formInstanceData.get(ProjectFormFields.Individuals
				.getFieldNameFromColumn(COLUMN_INDIVIDUAL_FATHER)));
		individual.setCurrentResidence(formInstanceData.get(ProjectFormFields.Individuals
				.getFieldNameFromColumn(COLUMN_INDIVIDUAL_RESIDENCE_LOCATION_EXTID)));
		individual.setEndType(formInstanceData.get(ProjectFormFields.Individuals
				.getFieldNameFromColumn(COLUMN_INDIVIDUAL_RESIDENCE_END_TYPE)));
		individual.setOtherId(formInstanceData.get(ProjectFormFields.Individuals
				.getFieldNameFromColumn(COLUMN_INDIVIDUAL_OTHER_ID)));
		individual.setOtherNames(formInstanceData.get(ProjectFormFields.Individuals
				.getFieldNameFromColumn(COLUMN_INDIVIDUAL_OTHER_NAMES)));
		individual.setAge(formInstanceData.get(ProjectFormFields.Individuals
				.getFieldNameFromColumn(COLUMN_INDIVIDUAL_AGE)));
		individual.setAgeUnits(formInstanceData.get(ProjectFormFields.Individuals
				.getFieldNameFromColumn(COLUMN_INDIVIDUAL_AGE_UNITS)));
		individual.setPhoneNumber(formInstanceData.get(ProjectFormFields.Individuals
				.getFieldNameFromColumn(COLUMN_INDIVIDUAL_PHONE_NUMBER)));
		individual.setOtherPhoneNumber(formInstanceData.get(ProjectFormFields.Individuals
				.getFieldNameFromColumn(COLUMN_INDIVIDUAL_OTHER_PHONE_NUMBER)));
		individual.setLanguagePreference(formInstanceData.get(ProjectFormFields.Individuals
				.getFieldNameFromColumn(COLUMN_INDIVIDUAL_LANGUAGE_PREFERENCE)));
		return individual;
	}

	public static Uri insert(ContentResolver resolver, Individual individual) {
		ContentValues cv = new ContentValues();

		cv.put(COLUMN_INDIVIDUAL_EXTID, individual.getExtId());
		cv.put(COLUMN_INDIVIDUAL_FIRST_NAME, individual.getFirstName());
		cv.put(COLUMN_INDIVIDUAL_LAST_NAME, individual.getLastName());
		cv.put(COLUMN_INDIVIDUAL_DOB, individual.getDob());
		cv.put(COLUMN_INDIVIDUAL_GENDER, individual.getGender());
		cv.put(COLUMN_INDIVIDUAL_MOTHER, individual.getMother());
		cv.put(COLUMN_INDIVIDUAL_FATHER, individual.getFather());
		cv.put(COLUMN_INDIVIDUAL_RESIDENCE_LOCATION_EXTID, individual.getCurrentResidence());
		cv.put(COLUMN_INDIVIDUAL_RESIDENCE_END_TYPE, individual.getEndType());

		cv.put(COLUMN_INDIVIDUAL_OTHER_ID, individual.getOtherId());
		cv.put(COLUMN_INDIVIDUAL_OTHER_NAMES, individual.getOtherNames());
		cv.put(COLUMN_INDIVIDUAL_AGE, individual.getAge());
		cv.put(COLUMN_INDIVIDUAL_AGE_UNITS, individual.getAgeUnits());
		cv.put(COLUMN_INDIVIDUAL_PHONE_NUMBER, individual.getPhoneNumber());
		cv.put(COLUMN_INDIVIDUAL_OTHER_PHONE_NUMBER, individual.getOtherPhoneNumber());
		cv.put(COLUMN_INDIVIDUAL_LANGUAGE_PREFERENCE, individual.getLanguagePreference());

		return resolver.insert(CONTENT_ID_URI_BASE, cv);
	}

}
