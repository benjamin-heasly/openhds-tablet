package org.openhds.mobile.projectdata;

import java.util.ArrayList;
import java.util.List;

import org.openhds.mobile.activity.CensusActivity;
import org.openhds.mobile.database.queries.Converter;
import org.openhds.mobile.database.queries.Queries;
import org.openhds.mobile.database.queries.QueryResult;
import org.openhds.mobile.model.Individual;
import org.openhds.mobile.model.Location;
import org.openhds.mobile.model.LocationHierarchy;

import android.content.ContentResolver;
import android.database.Cursor;

public class ProjectQueryHelper {

	// These must match the server data.
	// They come from the name column of the locationhierarchylevel table
	public static final String REGION_HIERARCHY_LEVEL_NAME = "Region";
	public static final String PROVINCE_HIERARCHY_LEVEL_NAME = "Province";
	public static final String DISTRICT_HIERARCHY_LEVEL_NAME = "District";
	public static final String MAP_AREA_HIERARCHY_LEVEL_NAME = "MapArea";
	public static final String SECTOR_HIERARCHY_LEVEL_NAME = "Sector";

	// These should be string resources instead of constants
	public static final String AGE_KEY = "age";
	public static final String LANGUAGE_KEY = "language preference";
	public static final String OTHER_NAMES_KEY = "other names";

	public static List<QueryResult> getAll(ContentResolver contentResolver, String state) {

		if (state.equals(CensusActivity.REGION_STATE)) {
			Cursor cursor = Queries.getHierarchysByLevel(contentResolver, REGION_HIERARCHY_LEVEL_NAME);
			return getHierarchyQueryResults(cursor, state);
		} else if (state.equals(CensusActivity.PROVINCE_STATE)) {
			Cursor cursor = Queries.getHierarchysByLevel(contentResolver, PROVINCE_HIERARCHY_LEVEL_NAME);
			return getHierarchyQueryResults(cursor, state);
		} else if (state.equals(CensusActivity.DISTRICT_STATE)) {
			Cursor cursor = Queries.getHierarchysByLevel(contentResolver, DISTRICT_HIERARCHY_LEVEL_NAME);
			return getHierarchyQueryResults(cursor, state);
		} else if (state.equals(CensusActivity.MAP_AREA_STATE)) {
			Cursor cursor = Queries.getHierarchysByLevel(contentResolver, MAP_AREA_HIERARCHY_LEVEL_NAME);
			return getHierarchyQueryResults(cursor, state);
		} else if (state.equals(CensusActivity.SECTOR_STATE)) {
			Cursor cursor = Queries.getHierarchysByLevel(contentResolver, SECTOR_HIERARCHY_LEVEL_NAME);
			return getHierarchyQueryResults(cursor, state);
		} else if (state.equals(CensusActivity.HOUSEHOLD_STATE)) {
			Cursor cursor = Queries.getAllLocations(contentResolver);
			return getLocationQueryResults(cursor, state);
		} else if (state.equals(CensusActivity.INDIVIDUAL_STATE)) {
			Cursor cursor = Queries.getAllIndividuals(contentResolver);
			return getIndividualQueryResults(cursor, state);
		}
		return new ArrayList<QueryResult>();
	}

	public static List<QueryResult> getChildren(ContentResolver contentResolver, QueryResult qr,
			String childState) {
		String state = qr.getState();

		if (state.equals(CensusActivity.REGION_STATE) || state.equals(CensusActivity.PROVINCE_STATE)
				|| state.equals(CensusActivity.DISTRICT_STATE) || state.equals(CensusActivity.MAP_AREA_STATE)) {
			Cursor cursor = Queries.getHierarchysByParent(contentResolver, qr.getExtId());
			return getHierarchyQueryResults(cursor, childState);
		} else if (state.equals(CensusActivity.SECTOR_STATE)) {
			Cursor cursor = Queries.getLocationsByHierachy(contentResolver, qr.getExtId());
			return getLocationQueryResults(cursor, childState);
		} else if (state.equals(CensusActivity.HOUSEHOLD_STATE)) {
			Cursor cursor = Queries.getIndividualsByResidency(contentResolver, qr.getExtId());
			return getIndividualQueryResults(cursor, childState);
		}

		return new ArrayList<QueryResult>();
	}

	private static List<QueryResult> getHierarchyQueryResults(Cursor cursor, String state) {
		List<QueryResult> results = new ArrayList<QueryResult>();

		if (null == cursor) {
			return results;
		}

		for (LocationHierarchy lh : Converter.toHierarchyList(cursor)) {
			QueryResult qr = new QueryResult();
			qr.setExtId(lh.getExtId());
			qr.setName(lh.getName());
			qr.setState(state);
			results.add(qr);
		}

		return results;
	}

	private static List<QueryResult> getLocationQueryResults(Cursor cursor, String state) {
		List<QueryResult> results = new ArrayList<QueryResult>();

		if (null == cursor) {
			return results;
		}

		for (Location location : Converter.toLocationList(cursor)) {
			QueryResult qr = new QueryResult();
			qr.setExtId(location.getExtId());
			qr.setName(location.getName());
			qr.setState(state);
			results.add(qr);
		}

		return results;
	}

	private static List<QueryResult> getIndividualQueryResults(Cursor cursor, String state) {
		List<QueryResult> results = new ArrayList<QueryResult>();

		if (null == cursor) {
			return results;
		}

		for (Individual individual : Converter.toIndividualList(cursor)) {
			QueryResult qr = new QueryResult();
			qr.setExtId(individual.getExtId());
			qr.setName(individual.getFirstName() + " " + individual.getLastName());
			qr.setState(state);

			qr.getPayLoad().put(OTHER_NAMES_KEY, individual.getOtherNames());
			qr.getPayLoad().put(AGE_KEY, individual.getAge() + " (" + individual.getAgeUnits() + ")");
			qr.getPayLoad().put(LANGUAGE_KEY, individual.getLanguagePreference());

			results.add(qr);
		}

		return results;
	}
}
