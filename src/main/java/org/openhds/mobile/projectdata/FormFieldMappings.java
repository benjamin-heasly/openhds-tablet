package org.openhds.mobile.projectdata;

import java.util.HashMap;
import java.util.Map;

import org.openhds.mobile.OpenHDS;

public class FormFieldMappings {

	 public static final class Individuals{
		 
		 private static Map<String,String> columnsToFieldNames = new HashMap<String,String>();
		 public static final String COLLECTION_DATE_TIME = "collectionDateTime";
		 public static final String HOUSEHOLD_EXTID = "householdExtId";
		 public static final String INDIVIDUAL_EXTID = "individualExtId";
		 public static final String FIRST_NAME = "individualFirstName";
		 public static final String LAST_NAME = "individualLastName";
		 public static final String OTHER_NAMES = "individualOtherNames";
		 public static final String DATE_OF_BIRTH = "individualDateOfBirth";
		 public static final String AGE = "individualAge";
		 public static final String AGE_UNITS = "individualAgeUnits";
		 public static final String GENDER = "individualGender";
		 public static final String RELATIONSHIP_TO_HEAD = "individualRelationshipToHeadOfHousehold";
		 public static final String PHONE_NUMBER = "individualPhoneNumber";
		 public static final String OTHER_PHONE_NUMBER = "individualOtherPhoneNumber";
		 public static final String LANGUAGE_PREFERENCE = "individualLanguagePreference";
		 public static final String DIP = "individualDip";
		 public static final String MEMBER_STATUS = "individualMemberStatus";
		 
		static{
			
			columnsToFieldNames.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_COLLECTION_DATETIME, COLLECTION_DATE_TIME);
			columnsToFieldNames.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_EXTID, INDIVIDUAL_EXTID);
			columnsToFieldNames.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_FIRSTNAME, FIRST_NAME);
			columnsToFieldNames.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_LASTNAME, LAST_NAME);
			columnsToFieldNames.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_OTHERNAMES, OTHER_NAMES);
			columnsToFieldNames.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_DOB, DATE_OF_BIRTH);
			columnsToFieldNames.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_AGE, AGE);
			columnsToFieldNames.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_AGE_UNITS, AGE_UNITS);
			columnsToFieldNames.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_GENDER, GENDER);
			columnsToFieldNames.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_PHONE_NUMBER,PHONE_NUMBER);
			columnsToFieldNames.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_OTHER_PHONE_NUMBER, OTHER_PHONE_NUMBER);
			columnsToFieldNames.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_LANGUAGE_PREFERENCE, LANGUAGE_PREFERENCE);
			columnsToFieldNames.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_DIP, DIP);
			columnsToFieldNames.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_MEMBER_STATUS, MEMBER_STATUS);
		}
		 
		 public String getFieldNameFromColumn(String column){
			 return columnsToFieldNames.get(column);
		 }
		 
	 }
	
	
}
