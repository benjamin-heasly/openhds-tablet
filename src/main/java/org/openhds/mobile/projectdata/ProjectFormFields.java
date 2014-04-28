package org.openhds.mobile.projectdata;

import java.util.HashMap;
import java.util.Map;

import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.R;
import org.openhds.mobile.activity.CensusActivity;

public class ProjectFormFields {

	public static final class General {
		public static final String COLLECTED_DATE_TIME = "collectionDateTime";
		public static final String COLLECTED_BY_FIELD_WORKER_EXTID = "fieldWorkerExtId";

		public static final String REGION_STATE_FIELD_NAME = "regionExtId";
		public static final String PROVINCE_STATE_FIELD_NAME = "provinceExtId";
		public static final String DISTRICT_STATE_FIELD_NAME = "districtExtId";
		public static final String MAP_AREA_STATE_FIELD_NAME = "mapAreaExtId";
		public static final String SECTOR_STATE_FIELD_NAME = "sectorExtId";
		public static final String HOUSEHOLD_STATE_FIELD_NAME = "householdExtId";
		public static final String INDIVIDUAL_STATE_FIELD_NAME = "individualExtId";
		public static final String BOTTOM_STATE_FIELD_NAME = "bottomExtId";

		private static final Map<String, String> stateFieldNames = new HashMap<String, String>();

		static {
			stateFieldNames.put(CensusActivity.REGION_STATE, REGION_STATE_FIELD_NAME);
			stateFieldNames.put(CensusActivity.PROVINCE_STATE, PROVINCE_STATE_FIELD_NAME);
			stateFieldNames.put(CensusActivity.DISTRICT_STATE, DISTRICT_STATE_FIELD_NAME);
			stateFieldNames.put(CensusActivity.MAP_AREA_STATE, MAP_AREA_STATE_FIELD_NAME);
			stateFieldNames.put(CensusActivity.SECTOR_STATE, SECTOR_STATE_FIELD_NAME);
			stateFieldNames.put(CensusActivity.HOUSEHOLD_STATE, HOUSEHOLD_STATE_FIELD_NAME);
			stateFieldNames.put(CensusActivity.INDIVIDUAL_STATE, INDIVIDUAL_STATE_FIELD_NAME);
			stateFieldNames.put(CensusActivity.BOTTOM_STATE, BOTTOM_STATE_FIELD_NAME);
		}

		public static String getExtIdFieldNameFromState(String state) {
			if (stateFieldNames.containsKey(state)) {
				return stateFieldNames.get(state);
			} else {
				return null;
			}
		}
	}

	public static final class Individuals {

		// for individuals table
		public static final String INDIVIDUAL_EXTID = "individualExtId";
		public static final String FIRST_NAME = "individualFirstName";
		public static final String LAST_NAME = "individualLastName";
		public static final String OTHER_NAMES = "individualOtherNames";
		public static final String DATE_OF_BIRTH = "individualDateOfBirth";
		public static final String AGE = "individualAge";
		public static final String AGE_UNITS = "individualAgeUnits";
		public static final String GENDER = "individualGender";
		public static final String PHONE_NUMBER = "individualPhoneNumber";
		public static final String OTHER_PHONE_NUMBER = "individualOtherPhoneNumber";
		public static final String LANGUAGE_PREFERENCE = "individualLanguagePreference";
		public static final String DIP = "individualDip";
		public static final String MOTHER_EXTID = "individualMotherExtId";
		public static final String FATHER_EXTID = "individualFatherExtId";

		// for relationships and memberships tables
		public static final String RELATIONSHIP_TO_HEAD = "individualRelationshipToHeadOfHousehold";
		public static final String HOUSEHOLD_EXTID = "householdExtId";
		public static final String MEMBER_STATUS = "individualMemberStatus";

		private static Map<String, String> columnsToFieldNames = new HashMap<String, String>();

		static {
			columnsToFieldNames.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_EXTID, INDIVIDUAL_EXTID);
			columnsToFieldNames.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_FIRST_NAME, FIRST_NAME);
			columnsToFieldNames.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_LAST_NAME, LAST_NAME);
			columnsToFieldNames.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_OTHER_NAMES, OTHER_NAMES);
			columnsToFieldNames.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_DOB, DATE_OF_BIRTH);
			columnsToFieldNames.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_AGE, AGE);
			columnsToFieldNames.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_AGE_UNITS, AGE_UNITS);
			columnsToFieldNames.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_GENDER, GENDER);
			columnsToFieldNames.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_PHONE_NUMBER, PHONE_NUMBER);
			columnsToFieldNames.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_OTHER_PHONE_NUMBER,
					OTHER_PHONE_NUMBER);
			columnsToFieldNames.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_LANGUAGE_PREFERENCE,
					LANGUAGE_PREFERENCE);
			columnsToFieldNames.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_OTHER_ID, DIP);
			columnsToFieldNames.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_MOTHER, MOTHER_EXTID);
			columnsToFieldNames.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_FATHER, FATHER_EXTID);
			columnsToFieldNames.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_RESIDENCE_LOCATION_EXTID,
					HOUSEHOLD_EXTID);
		}

		public static String getFieldNameFromColumn(String column) {
			if (columnsToFieldNames.containsKey(column)) {
				return columnsToFieldNames.get(column);
			} else {
				return null;
			}
		}
	}
}
