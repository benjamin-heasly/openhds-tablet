package org.openhds.mobile.projectdata.QueryHelpers;

import java.util.ArrayList;
import java.util.List;

import org.openhds.mobile.R;
import org.openhds.mobile.database.queries.Converter;
import org.openhds.mobile.database.queries.Queries;
import org.openhds.mobile.database.queries.QueryResult;
import org.openhds.mobile.model.Individual;
import org.openhds.mobile.model.Location;
import org.openhds.mobile.model.LocationHierarchy;
import org.openhds.mobile.model.Membership;
import org.openhds.mobile.projectdata.ProjectActivityBuilder;
import org.openhds.mobile.projectdata.ProjectResources;

import android.content.ContentResolver;
import android.database.Cursor;

public class CensusQueryHelper implements QueryHelper {

	// These must match the server data.
	// They come from the name column of the locationhierarchylevel table
	public static final String REGION_HIERARCHY_LEVEL_NAME = "Region";
	public static final String PROVINCE_HIERARCHY_LEVEL_NAME = "Province";
	public static final String DISTRICT_HIERARCHY_LEVEL_NAME = "District";
	public static final String LOCALITY_HIERARCHY_LEVEL_NAME = "Locality";
	public static final String MAP_AREA_HIERARCHY_LEVEL_NAME = "MapArea";
	public static final String SECTOR_HIERARCHY_LEVEL_NAME = "Sector";

	public CensusQueryHelper() {

	}

	public List<QueryResult> getAll(ContentResolver contentResolver,
			String state) {

		if (state
				.equals(ProjectActivityBuilder.CensusActivityModule.REGION_STATE)) {
			Cursor cursor = Queries.getHierarchysByLevel(contentResolver,
					REGION_HIERARCHY_LEVEL_NAME);
			return getHierarchyQueryResultList(cursor, state);
		} else if (state
				.equals(ProjectActivityBuilder.CensusActivityModule.PROVINCE_STATE)) {
			Cursor cursor = Queries.getHierarchysByLevel(contentResolver,
					PROVINCE_HIERARCHY_LEVEL_NAME);
			return getHierarchyQueryResultList(cursor, state);
		} else if (state
				.equals(ProjectActivityBuilder.CensusActivityModule.DISTRICT_STATE)) {
			Cursor cursor = Queries.getHierarchysByLevel(contentResolver,
					DISTRICT_HIERARCHY_LEVEL_NAME);
			return getHierarchyQueryResultList(cursor, state);
		} else if (state
				.equals(ProjectActivityBuilder.CensusActivityModule.LOCALITY_STATE)) {
			Cursor cursor = Queries.getHierarchysByLevel(contentResolver,
					LOCALITY_HIERARCHY_LEVEL_NAME);
			return getHierarchyQueryResultList(cursor, state);
		} else if (state
				.equals(ProjectActivityBuilder.CensusActivityModule.MAP_AREA_STATE)) {
			Cursor cursor = Queries.getHierarchysByLevel(contentResolver,
					MAP_AREA_HIERARCHY_LEVEL_NAME);
			return getHierarchyQueryResultList(cursor, state);
		} else if (state
				.equals(ProjectActivityBuilder.CensusActivityModule.SECTOR_STATE)) {
			Cursor cursor = Queries.getHierarchysByLevel(contentResolver,
					SECTOR_HIERARCHY_LEVEL_NAME);
			return getHierarchyQueryResultList(cursor, state);
		} else if (state
				.equals(ProjectActivityBuilder.CensusActivityModule.HOUSEHOLD_STATE)) {
			Cursor cursor = Queries.getAllLocations(contentResolver);
			return getLocationQueryResultList(cursor, state);
		} else if (state
				.equals(ProjectActivityBuilder.CensusActivityModule.INDIVIDUAL_STATE)) {
			Cursor cursor = Queries.getAllIndividuals(contentResolver);
			return getIndividualQueryResultList(cursor, contentResolver, state);
		}
		return new ArrayList<QueryResult>();
	}

	public QueryResult getIfExists(ContentResolver contentResolver,
			String state, String extId) {

		if (state
				.equals(ProjectActivityBuilder.CensusActivityModule.REGION_STATE)) {
			Cursor cursor = Queries.getHierarchyByExtId(contentResolver, extId);
			cursor.moveToFirst();
			return getHierarchyQueryResult(cursor, state, true);
		} else if (state
				.equals(ProjectActivityBuilder.CensusActivityModule.PROVINCE_STATE)) {
			Cursor cursor = Queries.getHierarchyByExtId(contentResolver, extId);
			cursor.moveToFirst();
			return getHierarchyQueryResult(cursor, state, true);
		} else if (state
				.equals(ProjectActivityBuilder.CensusActivityModule.DISTRICT_STATE)) {
			Cursor cursor = Queries.getHierarchyByExtId(contentResolver, extId);
			cursor.moveToFirst();
			return getHierarchyQueryResult(cursor, state, true);
		} else if (state
				.equals(ProjectActivityBuilder.CensusActivityModule.LOCALITY_STATE)) {
			Cursor cursor = Queries.getHierarchyByExtId(contentResolver, extId);
			cursor.moveToFirst();
			return getHierarchyQueryResult(cursor, state, true);
		} else if (state
				.equals(ProjectActivityBuilder.CensusActivityModule.MAP_AREA_STATE)) {
			Cursor cursor = Queries.getHierarchyByExtId(contentResolver, extId);
			cursor.moveToFirst();
			return getHierarchyQueryResult(cursor, state, true);
		} else if (state
				.equals(ProjectActivityBuilder.CensusActivityModule.SECTOR_STATE)) {
			Cursor cursor = Queries.getHierarchyByExtId(contentResolver, extId);
			cursor.moveToFirst();
			return getHierarchyQueryResult(cursor, state, true);
		} else if (state
				.equals(ProjectActivityBuilder.CensusActivityModule.HOUSEHOLD_STATE)) {
			Cursor cursor = Queries.getLocationByExtId(contentResolver, extId);
			cursor.moveToFirst();
			return getLocationQueryResult(cursor, state, true);
		} else if (state
				.equals(ProjectActivityBuilder.CensusActivityModule.INDIVIDUAL_STATE)) {
			Cursor cursor = Queries
					.getIndividualByExtId(contentResolver, extId);
			cursor.moveToFirst();
			return getIndividualQueryResult(cursor, contentResolver, state,
					true);
		}

		return null;
	}

	public List<QueryResult> getChildren(ContentResolver contentResolver,
			QueryResult qr, String childState) {
		String state = qr.getState();

		if (state
				.equals(ProjectActivityBuilder.CensusActivityModule.REGION_STATE)
				|| state.equals(ProjectActivityBuilder.CensusActivityModule.PROVINCE_STATE)
				|| state.equals(ProjectActivityBuilder.CensusActivityModule.DISTRICT_STATE)
				|| state.equals(ProjectActivityBuilder.CensusActivityModule.LOCALITY_STATE)
				|| state.equals(ProjectActivityBuilder.CensusActivityModule.MAP_AREA_STATE)) {
			Cursor cursor = Queries.getHierarchysByParent(contentResolver,
					qr.getExtId());
			return getHierarchyQueryResultList(cursor, childState);
		} else if (state
				.equals(ProjectActivityBuilder.CensusActivityModule.SECTOR_STATE)) {
			
			Cursor cursor = Queries.getHierarchyByExtId(contentResolver,
					qr.getExtId());
			cursor.moveToFirst();
			LocationHierarchy sector = Converter.toHierarchy(cursor, true);

			
			Cursor mapAreaCursor = Queries.getHierarchyByExtId(contentResolver, sector.getParent());
			mapAreaCursor.moveToFirst();
			LocationHierarchy mapArea = Converter.toHierarchy(mapAreaCursor, true);
			
			Cursor locationCursor = Queries.getLocationsBySectorNameAndMapAreaName(
					contentResolver, sector.getName(), mapArea.getName());

			return getLocationQueryResultList(locationCursor, childState);
		} else if (state
				.equals(ProjectActivityBuilder.CensusActivityModule.HOUSEHOLD_STATE)) {
			Cursor cursor = Queries.getIndividualsByResidency(contentResolver,
					qr.getExtId());

			return getIndividualQueryResultList(cursor, contentResolver,
					childState);
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
			Cursor cursor, ContentResolver resolver, String state) {
		List<QueryResult> results = new ArrayList<QueryResult>();

		if (null == cursor || cursor.getCount() < 1) {
			return results;
		}

		while (cursor.moveToNext()) {
			results.add(getIndividualQueryResult(cursor, resolver, state, false));
		}

		cursor.close();

		return results;
	}

	private static QueryResult getIndividualQueryResult(Cursor cursor,
			ContentResolver resolver, String state, boolean close) {

		if (null == cursor || cursor.getCount() < 1) {
			return null;
		}

		Individual individual = Converter.toIndividual(cursor, close);
		QueryResult qr = new QueryResult();
		qr.setExtId(individual.getExtId());
		qr.setName(Individual.getFullName(individual));
		qr.setState(state);

		// might be the gross way to do this...

		qr.getStringsPayLoad().put(R.string.individual_other_names_label,
				individual.getOtherNames());
		qr.getStringsPayLoad().put(R.string.individual_age_label,
				Individual.getAgeWithUnits(individual));
		qr.getStringsPayLoad().put(
				R.string.individual_language_preference_label,
				individual.getLanguagePreference());

		if (null != resolver) {

			Cursor membershipCursor = Queries
					.getMembershipByHouseholdAndIndividualExtId(resolver,
							individual.getCurrentResidence(),
							individual.getExtId());

			membershipCursor.moveToFirst();
			Membership membership = Converter.toMembership(membershipCursor,
					true);

			qr.getStringIdsPayLoad().put(
					R.string.individual_relationship_to_head_label,
					ProjectResources.Relationship
							.getRelationshipStringId(membership
									.getRelationshipToHead()));
		}

		return qr;
	}
}
