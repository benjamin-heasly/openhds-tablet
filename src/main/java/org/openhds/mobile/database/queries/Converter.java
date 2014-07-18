package org.openhds.mobile.database.queries;

import java.util.ArrayList;
import java.util.List;

import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.model.FieldWorker;
import org.openhds.mobile.model.Individual;
import org.openhds.mobile.model.Location;
import org.openhds.mobile.model.LocationHierarchy;
import org.openhds.mobile.model.Membership;
import org.openhds.mobile.model.Relationship;
import org.openhds.mobile.model.SocialGroup;

import android.database.Cursor;

/**
 * Converts a cursor into a corresponding model class or a list of model class
 */
public class Converter {

	public static Individual toIndividual(Cursor cursor, boolean close) {
		Individual individual = new Individual();

		if (cursor.getPosition() > -1) {
			populateIndividual(cursor, individual);
		}

		if (close) {
			cursor.close();
		}

		return individual;
	}

	private static void populateIndividual(Cursor cursor, Individual individual) {
		individual
				.setCurrentResidence(cursor.getString(cursor
						.getColumnIndex(OpenHDS.Individuals.COLUMN_INDIVIDUAL_RESIDENCE_LOCATION_EXTID)));
        individual
                .setEndType(cursor.getString(cursor
                        .getColumnIndex(OpenHDS.Individuals.COLUMN_INDIVIDUAL_RESIDENCE_END_TYPE)));
		individual.setDob(cursor.getString(cursor
				.getColumnIndex(OpenHDS.Individuals.COLUMN_INDIVIDUAL_DOB)));
		individual.setExtId(cursor.getString(cursor
				.getColumnIndex(OpenHDS.Individuals.COLUMN_INDIVIDUAL_EXTID)));
		individual.setFather(cursor.getString(cursor
				.getColumnIndex(OpenHDS.Individuals.COLUMN_INDIVIDUAL_FATHER)));
		individual
				.setFirstName(cursor.getString(cursor
						.getColumnIndex(OpenHDS.Individuals.COLUMN_INDIVIDUAL_FIRST_NAME)));
		individual.setGender(cursor.getString(cursor
				.getColumnIndex(OpenHDS.Individuals.COLUMN_INDIVIDUAL_GENDER)));
		individual
				.setLastName(cursor.getString(cursor
						.getColumnIndex(OpenHDS.Individuals.COLUMN_INDIVIDUAL_LAST_NAME)));
		individual
				.setOtherNames(cursor.getString(cursor
						.getColumnIndex(OpenHDS.Individuals.COLUMN_INDIVIDUAL_OTHER_NAMES)));
		individual.setMother(cursor.getString(cursor
				.getColumnIndex(OpenHDS.Individuals.COLUMN_INDIVIDUAL_MOTHER)));

		individual.setAge(cursor.getString(cursor
				.getColumnIndex(OpenHDS.Individuals.COLUMN_INDIVIDUAL_AGE)));
		individual
				.setAgeUnits(cursor.getString(cursor
						.getColumnIndex(OpenHDS.Individuals.COLUMN_INDIVIDUAL_AGE_UNITS)));
		individual
				.setLanguagePreference(cursor.getString(cursor
						.getColumnIndex(OpenHDS.Individuals.COLUMN_INDIVIDUAL_LANGUAGE_PREFERENCE)));

		individual.setMemberStatus(cursor.getString(cursor
				.getColumnIndex(OpenHDS.Individuals.COLUMN_INDIVIDUAL_STATUS)));
		individual
				.setOtherId(cursor.getString(cursor
						.getColumnIndex(OpenHDS.Individuals.COLUMN_INDIVIDUAL_OTHER_ID)));
		individual
				.setPhoneNumber(cursor.getString(cursor
						.getColumnIndex(OpenHDS.Individuals.COLUMN_INDIVIDUAL_PHONE_NUMBER)));
		individual
				.setOtherPhoneNumber(cursor.getString(cursor
						.getColumnIndex(OpenHDS.Individuals.COLUMN_INDIVIDUAL_OTHER_PHONE_NUMBER)));
		individual
				.setPointOfContactName(cursor.getString(cursor
						.getColumnIndex(OpenHDS.Individuals.COLUMN_INDIVIDUAL_POINT_OF_CONTACT_NAME)));
		individual
				.setPointOfContactPhoneNumber(cursor.getString(cursor
						.getColumnIndex(OpenHDS.Individuals.COLUMN_INDIVIDUAL_POINT_OF_CONTACT_PHONE_NUMBER)));
	}

	public static Location toLocation(Cursor cursor, boolean close) {
		Location location = new Location();

		if (cursor.getPosition() > -1) {
			populateLocation(cursor, location);
		}

		if (close) {
			cursor.close();
		}

		return location;
	}

	private static void populateLocation(Cursor cursor, Location location) {
		location.setExtId(cursor.getString(cursor
				.getColumnIndex(OpenHDS.Locations.COLUMN_LOCATION_EXTID)));
		location.setHierarchyExtId(cursor.getString(cursor
				.getColumnIndex(OpenHDS.Locations.COLUMN_LOCATION_HIERARCHY)));
		location.setLatitude(cursor.getString(cursor
				.getColumnIndex(OpenHDS.Locations.COLUMN_LOCATION_LATITUDE)));
		location.setLongitude(cursor.getString(cursor
				.getColumnIndex(OpenHDS.Locations.COLUMN_LOCATION_LONGITUDE)));
		location.setName(cursor.getString(cursor
				.getColumnIndex(OpenHDS.Locations.COLUMN_LOCATION_NAME)));
		location.setCommunityName(cursor.getString(cursor
				.getColumnIndex(OpenHDS.Locations.COLUMN_LOCATION_COMMUNITY_NAME)));
		location.setLocalityName(cursor.getString(cursor
				.getColumnIndex(OpenHDS.Locations.COLUMN_LOCATION_LOCALITY_NAME)));
		location.setMapAreaName(cursor.getString(cursor
				.getColumnIndex(OpenHDS.Locations.COLUMN_LOCATION_MAP_AREA_NAME)));
		location.setSectorName(cursor.getString(cursor
				.getColumnIndex(OpenHDS.Locations.COLUMN_LOCATION_SECTOR_NAME)));
	}

	public static LocationHierarchy toHierarchy(Cursor cursor, boolean close) {
		LocationHierarchy hierarchy = new LocationHierarchy();

		if (cursor.getPosition() > -1) {
			populateHierarchy(cursor, hierarchy);
		}

		if (close) {
			cursor.close();
		}

		return hierarchy;
	}

	public static LocationHierarchy convertToHierarchy(Cursor cursor) {
		LocationHierarchy hierarchy = new LocationHierarchy();
		populateHierarchy(cursor, hierarchy);
		return hierarchy;
	}

	private static void populateHierarchy(Cursor cursor,
			LocationHierarchy hierarchy) {
		hierarchy
				.setExtId(cursor.getString(cursor
						.getColumnIndex(OpenHDS.HierarchyItems.COLUMN_HIERARCHY_EXTID)));
		hierarchy
				.setLevel(cursor.getString(cursor
						.getColumnIndex(OpenHDS.HierarchyItems.COLUMN_HIERARCHY_LEVEL)));
		hierarchy.setName(cursor.getString(cursor
				.getColumnIndex(OpenHDS.HierarchyItems.COLUMN_HIERARCHY_NAME)));
		hierarchy
				.setParent(cursor.getString(cursor
						.getColumnIndex(OpenHDS.HierarchyItems.COLUMN_HIERARCHY_PARENT)));
	}

	public static FieldWorker toFieldWorker(Cursor cursor, boolean close) {
		FieldWorker fw = new FieldWorker();

		if (cursor.getPosition() > -1) {

			populateFieldWorker(cursor, fw);

			// temporary way to get field worker Id prefix. should be actual
			// attribute of field worker.
			int id = cursor.getInt(cursor
					.getColumnIndex(OpenHDS.FieldWorkers._ID));
			String idString = String.format("%02d", id);
			fw.setCollectedIdPrefix(idString);
		}

		if (close) {
			cursor.close();
		}

		return fw;
	}

	private static void populateFieldWorker(Cursor cursor, FieldWorker fw) {

		fw.setExtId(cursor.getString(cursor
				.getColumnIndex(OpenHDS.FieldWorkers.COLUMN_FIELD_WORKER_EXTID)));
		fw.setFirstName(cursor.getString(cursor
				.getColumnIndex(OpenHDS.FieldWorkers.COLUMN_FIELD_WORKER_FIRST_NAME)));
		fw.setLastName(cursor.getString(cursor
				.getColumnIndex(OpenHDS.FieldWorkers.COLUMN_FIELD_WORKER_LAST_NAME)));
		fw.setPassword(cursor.getString(cursor
				.getColumnIndex(OpenHDS.FieldWorkers.COLUMN_FIELD_WORKER_PASSWORD)));
	}

	public static SocialGroup toSocialGroup(Cursor cursor, boolean close) {
		SocialGroup sg = new SocialGroup();

		if (cursor.getPosition() > -1) {
			populateSocialGroup(cursor, sg);
		}

		if (close) {
			cursor.close();
		}

		return sg;
	}

	private static void populateSocialGroup(Cursor cursor, SocialGroup sg) {
		sg.setExtId(cursor.getString(cursor
				.getColumnIndex(OpenHDS.SocialGroups.COLUMN_SOCIAL_GROUP_EXTID)));
		sg.setGroupHead(cursor.getString(cursor
				.getColumnIndex(OpenHDS.SocialGroups.COLUMN_SOCIAL_GROUP_HEAD_INDIVIDUAL_EXTID)));
		sg.setGroupName(cursor.getString(cursor
				.getColumnIndex(OpenHDS.SocialGroups.COLUMN_SOCIAL_GROUP_NAME)));
	}

	public static List<SocialGroup> toSocialGroupList(Cursor cursor) {
		List<SocialGroup> socialGroups = new ArrayList<SocialGroup>();

		while (cursor.moveToNext()) {
			SocialGroup sg = new SocialGroup();
			populateSocialGroup(cursor, sg);
			socialGroups.add(sg);
		}

		cursor.close();

		return socialGroups;
	}

	public static List<LocationHierarchy> toHierarchyList(Cursor cursor) {
		List<LocationHierarchy> hierarchys = new ArrayList<LocationHierarchy>();

		while (cursor.moveToNext()) {
			LocationHierarchy hierarchy = new LocationHierarchy();
			populateHierarchy(cursor, hierarchy);
			hierarchys.add(hierarchy);
		}

		cursor.close();

		return hierarchys;
	}

	public static List<Location> toLocationList(Cursor cursor) {
		List<Location> locations = new ArrayList<Location>();

		while (cursor.moveToNext()) {
			Location location = new Location();
			populateLocation(cursor, location);
			locations.add(location);
		}

		cursor.close();

		return locations;
	}

	public static List<Individual> toIndividualList(Cursor cursor) {
		List<Individual> individuals = new ArrayList<Individual>();

		while (cursor.moveToNext()) {
			Individual individual = new Individual();
			populateIndividual(cursor, individual);
			individuals.add(individual);
		}

		cursor.close();

		return individuals;
	}

	public static Relationship toRelationship(Cursor cursor, boolean close) {
		Relationship relationship = new Relationship();

		if (cursor.getPosition() > -1) {
			relationship
					.setIndividualA(cursor.getString(cursor
							.getColumnIndex(OpenHDS.Relationships.COLUMN_RELATIONSHIP_INDIVIDUAL_A)));
			relationship
					.setIndividualB(cursor.getString(cursor
							.getColumnIndex(OpenHDS.Relationships.COLUMN_RELATIONSHIP_INDIVIDUAL_B)));
			relationship
					.setStartDate(cursor.getString(cursor
							.getColumnIndex(OpenHDS.Relationships.COLUMN_RELATIONSHIP_STARTDATE)));
			relationship
					.setType(cursor.getString(cursor
							.getColumnIndex(OpenHDS.Relationships.COLUMN_RELATIONSHIP_TYPE)));
		}

		if (close) {
			cursor.close();
		}
		return relationship;
	}

	public static List<Relationship> toRelationshipList(Cursor cursor) {
		List<Relationship> relationships = new ArrayList<Relationship>();

		while (cursor.moveToNext()) {
			Relationship rel = new Relationship();
			rel.setIndividualA(cursor.getString(cursor
					.getColumnIndex(OpenHDS.Relationships.COLUMN_RELATIONSHIP_INDIVIDUAL_A)));
			rel.setIndividualB(cursor.getString(cursor
					.getColumnIndex(OpenHDS.Relationships.COLUMN_RELATIONSHIP_INDIVIDUAL_B)));
			rel.setStartDate(cursor.getString(cursor
					.getColumnIndex(OpenHDS.Relationships.COLUMN_RELATIONSHIP_STARTDATE)));

			relationships.add(rel);
		}

		cursor.close();

		return relationships;
	}

	public static List<Relationship> toRelationshipListSwapped(Cursor cursor) {
		List<Relationship> relationships = new ArrayList<Relationship>();

		while (cursor.moveToNext()) {
			Relationship rel = new Relationship();
			rel.setIndividualA(cursor.getString(cursor
					.getColumnIndex(OpenHDS.Relationships.COLUMN_RELATIONSHIP_INDIVIDUAL_B)));
			rel.setIndividualB(cursor.getString(cursor
					.getColumnIndex(OpenHDS.Relationships.COLUMN_RELATIONSHIP_INDIVIDUAL_A)));
			rel.setStartDate(cursor.getString(cursor
					.getColumnIndex(OpenHDS.Relationships.COLUMN_RELATIONSHIP_STARTDATE)));

			relationships.add(rel);
		}

		cursor.close();

		return relationships;
	}

	private static void populateMembership(Cursor cursor, Membership membership) {
		membership
				.setSocialGroupExtId(cursor.getString(cursor
						.getColumnIndexOrThrow(OpenHDS.Memberships.COLUMN_SOCIAL_GROUP_EXTID)));
		membership.setIndividualExtId(cursor.getString(cursor
				.getColumnIndex(OpenHDS.Memberships.COLUMN_INDIVIDUAL_EXTID)));
		membership
				.setRelationshipToHead(cursor.getString(cursor
						.getColumnIndex(OpenHDS.Memberships.COLUMN_MEMBERSHIP_RELATIONSHIP_TO_HEAD)));

	}

	public static Membership toMembership(Cursor cursor, boolean close) {
		Membership membership = new Membership();

		if (cursor.getPosition() > -1) {
			populateMembership(cursor, membership);
		}
		if (close) {
			cursor.close();
		}
		return membership;
	}

	public static ArrayList<Membership> toMembershipList(Cursor cursor,
			boolean close) {
		ArrayList<Membership> memberships = new ArrayList<Membership>();

		while (cursor.moveToNext()) {
			Membership membership = new Membership();
			populateMembership(cursor, membership);
			memberships.add(membership);
		}
		if (close) {
			cursor.close();
		}
		return memberships;
	}

}
