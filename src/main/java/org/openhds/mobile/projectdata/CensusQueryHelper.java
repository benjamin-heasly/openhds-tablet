package org.openhds.mobile.projectdata;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openhds.mobile.R;
import org.openhds.mobile.activity.CensusActivity;
import org.openhds.mobile.database.queries.Converter;
import org.openhds.mobile.database.queries.Queries;
import org.openhds.mobile.database.queries.QueryResult;
import org.openhds.mobile.model.Individual;
import org.openhds.mobile.model.Location;
import org.openhds.mobile.model.LocationHierarchy;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;

public class CensusQueryHelper implements QueryHelper {

	// These must match the server data.
	// They come from the name column of the locationhierarchylevel table
	public static final String REGION_HIERARCHY_LEVEL_NAME = "Region";
	public static final String PROVINCE_HIERARCHY_LEVEL_NAME = "Province";
	public static final String DISTRICT_HIERARCHY_LEVEL_NAME = "District";
	public static final String MAP_AREA_HIERARCHY_LEVEL_NAME = "MapArea";
	public static final String SECTOR_HIERARCHY_LEVEL_NAME = "Sector";

	public CensusQueryHelper() {

	}

	public List<QueryResult> getAll(ContentResolver contentResolver,
			String state) {

		if (state.equals(CensusActivity.REGION_STATE)) {
			Cursor cursor = Queries.getHierarchysByLevel(contentResolver,
					REGION_HIERARCHY_LEVEL_NAME);
			return getHierarchyQueryResultList(cursor, state);
		} else if (state.equals(CensusActivity.PROVINCE_STATE)) {
			Cursor cursor = Queries.getHierarchysByLevel(contentResolver,
					PROVINCE_HIERARCHY_LEVEL_NAME);
			return getHierarchyQueryResultList(cursor, state);
		} else if (state.equals(CensusActivity.DISTRICT_STATE)) {
			Cursor cursor = Queries.getHierarchysByLevel(contentResolver,
					DISTRICT_HIERARCHY_LEVEL_NAME);
			return getHierarchyQueryResultList(cursor, state);
		} else if (state.equals(CensusActivity.MAP_AREA_STATE)) {
			Cursor cursor = Queries.getHierarchysByLevel(contentResolver,
					MAP_AREA_HIERARCHY_LEVEL_NAME);
			return getHierarchyQueryResultList(cursor, state);
		} else if (state.equals(CensusActivity.SECTOR_STATE)) {
			Cursor cursor = Queries.getHierarchysByLevel(contentResolver,
					SECTOR_HIERARCHY_LEVEL_NAME);
			return getHierarchyQueryResultList(cursor, state);
		} else if (state.equals(CensusActivity.HOUSEHOLD_STATE)) {
			Cursor cursor = Queries.getAllLocations(contentResolver);
			return getLocationQueryResultList(cursor, state);
		} else if (state.equals(CensusActivity.INDIVIDUAL_STATE)) {
			Cursor cursor = Queries.getAllIndividuals(contentResolver);
			return getIndividualQueryResultList(cursor, state);
		}
		return new ArrayList<QueryResult>();
	}

	public QueryResult getIfExists(ContentResolver contentResolver,
			String state, String extId) {

		if (state.equals(CensusActivity.REGION_STATE)) {
			Cursor cursor = Queries.getHierarchyByExtId(contentResolver, extId);
			cursor.moveToFirst();
			return getHierarchyQueryResult(cursor, state, true);
		} else if (state.equals(CensusActivity.PROVINCE_STATE)) {
			Cursor cursor = Queries.getHierarchyByExtId(contentResolver, extId);
			cursor.moveToFirst();
			return getHierarchyQueryResult(cursor, state, true);
		} else if (state.equals(CensusActivity.DISTRICT_STATE)) {
			Cursor cursor = Queries.getHierarchyByExtId(contentResolver, extId);
			cursor.moveToFirst();
			return getHierarchyQueryResult(cursor, state, true);
		} else if (state.equals(CensusActivity.MAP_AREA_STATE)) {
			Cursor cursor = Queries.getHierarchyByExtId(contentResolver, extId);
			cursor.moveToFirst();
			return getHierarchyQueryResult(cursor, state, true);
		} else if (state.equals(CensusActivity.SECTOR_STATE)) {
			Cursor cursor = Queries.getHierarchyByExtId(contentResolver, extId);
			cursor.moveToFirst();
			return getHierarchyQueryResult(cursor, state, true);
		} else if (state.equals(CensusActivity.HOUSEHOLD_STATE)) {
			Cursor cursor = Queries.getLocationByExtId(contentResolver, extId);
			cursor.moveToFirst();
			return getLocationQueryResult(cursor, state, true);
		} else if (state.equals(CensusActivity.INDIVIDUAL_STATE)) {
			Cursor cursor = Queries
					.getIndividualByExtId(contentResolver, extId);
			cursor.moveToFirst();
			return getIndividualQueryResult(cursor, state, true);
		}

		return null;
	}

	public List<QueryResult> getChildren(ContentResolver contentResolver,
			QueryResult qr, String childState) {
		String state = qr.getState();

		if (state.equals(CensusActivity.REGION_STATE)
				|| state.equals(CensusActivity.PROVINCE_STATE)
				|| state.equals(CensusActivity.DISTRICT_STATE)
				|| state.equals(CensusActivity.MAP_AREA_STATE)) {
			Cursor cursor = Queries.getHierarchysByParent(contentResolver,
					qr.getExtId());
			return getHierarchyQueryResultList(cursor, childState);
		} else if (state.equals(CensusActivity.SECTOR_STATE)) {
			Cursor cursor = Queries.getLocationsByHierachy(contentResolver,
					qr.getExtId());
			return getLocationQueryResultList(cursor, childState);
		} else if (state.equals(CensusActivity.HOUSEHOLD_STATE)) {
			Cursor cursor = Queries.getIndividualsByResidency(contentResolver,
					qr.getExtId());
			return getIndividualQueryResultList(cursor, childState);
		}

		return new ArrayList<QueryResult>();
	}

	private static List<QueryResult> getHierarchyQueryResultList(Cursor cursor,
			String state) {
		List<QueryResult> results = new ArrayList<QueryResult>();

		if (null == cursor || cursor.getCount() < 1) {
			return results;
		}

		while (cursor.moveToNext()) {
			results.add(getHierarchyQueryResult(cursor, state, false));
		}

		cursor.close();

		return results;
	}

	private static QueryResult getHierarchyQueryResult(Cursor cursor,
			String state, boolean close) {

		if (null == cursor || cursor.getCount() < 1) {
			return null;
		}

		LocationHierarchy hierarchy = Converter.toHierarchy(cursor, close);
		QueryResult qr = new QueryResult();
		qr.setExtId(hierarchy.getExtId());
		qr.setName(hierarchy.getName());
		qr.setState(state);

		return qr;
	}

	private static List<QueryResult> getLocationQueryResultList(Cursor cursor,
			String state) {
		List<QueryResult> results = new ArrayList<QueryResult>();

		if (null == cursor || cursor.getCount() < 1) {
			return results;
		}

		while (cursor.moveToNext()) {
			results.add(getLocationQueryResult(cursor, state, false));
		}

		cursor.close();

		return results;
	}

	private static QueryResult getLocationQueryResult(Cursor cursor,
			String state, boolean close) {

		if (null == cursor || cursor.getCount() < 1) {
			return null;
		}

		Location location = Converter.toLocation(cursor, close);
		QueryResult qr = new QueryResult();
		qr.setExtId(location.getExtId());
		qr.setName(location.getName());
		qr.setState(state);

		return qr;
	}

	private static List<QueryResult> getIndividualQueryResultList(
			Cursor cursor, String state) {
		List<QueryResult> results = new ArrayList<QueryResult>();

		if (null == cursor || cursor.getCount() < 1) {
			return results;
		}

		while (cursor.moveToNext()) {
			results.add(getIndividualQueryResult(cursor, state, false));
		}

		cursor.close();

		return results;
	}

	private static QueryResult getIndividualQueryResult(Cursor cursor,
			String state, boolean close) {

		if (null == cursor || cursor.getCount() < 1) {
			return null;
		}

		Individual individual = Converter.toIndividual(cursor, close);
		QueryResult qr = new QueryResult();
		qr.setExtId(individual.getExtId());
		qr.setName(Individual.getFullName(individual));
		qr.setState(state);
		

		if (state.equals(CensusActivity.BOTTOM_STATE)) {
			// TODO: display detailed view of individual.

		} else {

			qr.getStringsPayLoad().put(R.string.other_names_label,
					individual.getOtherNames());
			qr.getStringsPayLoad().put(R.string.age_label,
					Individual.getAgeWithUnits(individual));
			qr.getStringsPayLoad().put(R.string.language_preference_label,
					individual.getLanguagePreference());

			

		}

		return qr;
	}

	public static QueryResult createCompleteIndividualQueryResult(
			Map<String, String> formFieldMap, String state) {
		QueryResult qr = new QueryResult();

		qr.setExtId(formFieldMap
				.get(ProjectFormFields.Individuals.INDIVIDUAL_EXTID));
		qr.setName(formFieldMap.get(ProjectFormFields.Individuals.FIRST_NAME)
				+ " "
				+ formFieldMap.get(ProjectFormFields.Individuals.LAST_NAME));
		qr.setState(state);

		qr.getStringsPayLoad().put(R.string.other_names_label,
				formFieldMap.get(ProjectFormFields.Individuals.OTHER_NAMES));
		qr.getStringsPayLoad().put(
				ProjectResources.Individual.getIndividualStringId(formFieldMap
						.get(ProjectFormFields.Individuals.AGE_UNITS)),
				formFieldMap.get(ProjectFormFields.Individuals.AGE));

		qr.getStringsPayLoad().put(R.string.date_of_birth_label,
				formFieldMap.get(ProjectFormFields.Individuals.DATE_OF_BIRTH));
		qr.getStringIdsPayLoad().put(
				R.string.gender_lbl,
				ProjectResources.Individual.getIndividualStringId(formFieldMap
						.get(ProjectFormFields.Individuals.GENDER)));
		qr.getStringIdsPayLoad()
				.put(R.string.relationship_to_head_label,
						ProjectResources.Relationship.getRelationshipStringId(formFieldMap
								.get(ProjectFormFields.Individuals.RELATIONSHIP_TO_HEAD)));
		qr.getStringsPayLoad().put(R.string.personal_phone_number_label,
				formFieldMap.get(ProjectFormFields.Individuals.PHONE_NUMBER));
		qr.getStringsPayLoad().put(
				R.string.other_phone_number_label,
				formFieldMap
						.get(ProjectFormFields.Individuals.OTHER_PHONE_NUMBER));
		qr.getStringsPayLoad()
				.put(R.string.point_of_contact_label,
						formFieldMap
								.get(ProjectFormFields.Individuals.POINT_OF_CONTACT_NAME));
		qr.getStringsPayLoad()
				.put(R.string.point_of_contact_phone_number_label,
						formFieldMap
								.get(ProjectFormFields.Individuals.POINT_OF_CONTACT_PHONE_NUMBER));
		qr.getStringIdsPayLoad()
				.put(R.string.language_preference_label,
						ProjectResources.Individual.getIndividualStringId(formFieldMap
								.get(ProjectFormFields.Individuals.LANGUAGE_PREFERENCE)));
		qr.getStringsPayLoad().put(R.string.dip_label,
				formFieldMap.get(ProjectFormFields.Individuals.DIP));
		qr.getStringIdsPayLoad().put(
				R.string.member_status_label,
				ProjectResources.Individual.getIndividualStringId(formFieldMap
						.get(ProjectFormFields.Individuals.MEMBER_STATUS)));

		return qr;
	}
}
