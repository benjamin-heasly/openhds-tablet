package org.openhds.mobile.projectdata;

import java.util.Map;

import org.openhds.mobile.activity.Skeletor;
import org.openhds.mobile.database.IndividualAdapter;
import org.openhds.mobile.database.LocationAdapter;
import org.openhds.mobile.database.MembershipAdapter;
import org.openhds.mobile.database.RelationshipAdapter;
import org.openhds.mobile.database.SocialGroupAdapter;
import org.openhds.mobile.database.queries.Converter;
import org.openhds.mobile.database.queries.Queries;
import org.openhds.mobile.database.queries.QueryResult;
import org.openhds.mobile.model.Individual;
import org.openhds.mobile.model.Location;
import org.openhds.mobile.model.Membership;
import org.openhds.mobile.model.Relationship;
import org.openhds.mobile.model.SocialGroup;

import android.database.Cursor;

public class CensusFormPayloadConsumers {

	private static Individual insertOrUpdateIndividual(
			Map<String, String> formPayLoad, Skeletor skeletor) {
		// Insert or Update the Individual
		Individual individual = IndividualAdapter.create(formPayLoad);
		IndividualAdapter.insertOrUpdate(skeletor.getContentResolver(),
				individual);

		return individual;
	}

	public static class AddMemberOfHousehold implements FormPayloadConsumer {

		@Override
		public void consumeFormPayload(Map<String, String> formPayload,
				Skeletor skeletor) {

			Map<String, QueryResult> hierarchyPath = skeletor
					.getHierarchyPath();
			QueryResult selectedLocation = hierarchyPath
					.get(ProjectActivityBuilder.CensusActivityBuilder.HOUSEHOLD_STATE);

			String relationshipType = formPayload
					.get(ProjectFormFields.Individuals.RELATIONSHIP_TO_HEAD);
			String membershipStatus = formPayload
					.get(ProjectFormFields.Individuals.MEMBER_STATUS);
			Individual individual = insertOrUpdateIndividual(formPayload,
					skeletor);
			String startDate = formPayload
					.get(ProjectFormFields.General.COLLECTED_DATE_TIME);

			Cursor cursor = Queries.getHeadOfHouseholdByHouseholdExtId(
					skeletor.getContentResolver(), selectedLocation.getExtId());
			cursor.moveToFirst();
			Individual currentHeadOfHousehold = Converter.toIndividual(cursor,
					true);

			// INSERT or UPDATE RELATIONSHIP
			Relationship relationship = RelationshipAdapter.create(
					currentHeadOfHousehold, individual, relationshipType,
					startDate);
			RelationshipAdapter.insertOrUpdate(skeletor.getContentResolver(),
					relationship);

			// INSERT or UPDATE MEMBERSHIP
			Cursor socialGroupCursor = Queries.getSocialGroupByExtId(
					skeletor.getContentResolver(), selectedLocation.getExtId());
			if (socialGroupCursor.moveToFirst()) {
				SocialGroup socialGroup = Converter.toSocialGroup(
						socialGroupCursor, true);
				Membership membership = MembershipAdapter.create(individual,
						socialGroup, relationshipType, membershipStatus);

				MembershipAdapter.insertOrUpdate(skeletor.getContentResolver(),
						membership);
			}
			socialGroupCursor.close();

		}
	}

	public static class AddHeadOfHousehold implements FormPayloadConsumer {

		@Override
		public void consumeFormPayload(Map<String, String> formPayload,
				Skeletor skeletor) {
			// TODO Auto-generated method stub

			Map<String, QueryResult> hierarchyPath = skeletor
					.getHierarchyPath();
			QueryResult selectedLocation = hierarchyPath
					.get(ProjectActivityBuilder.CensusActivityBuilder.HOUSEHOLD_STATE);

			// Pull out useful strings from the formPayload
			String relationshipType = formPayload
					.get(ProjectFormFields.Individuals.RELATIONSHIP_TO_HEAD);
			String membershipStatus = formPayload
					.get(ProjectFormFields.Individuals.MEMBER_STATUS);
			String startDate = formPayload
					.get(ProjectFormFields.General.COLLECTED_DATE_TIME);
			Individual individual = insertOrUpdateIndividual(formPayload,
					skeletor);

			// Update the name of the location
			Cursor locationCursor = Queries.getLocationByExtId(
					skeletor.getContentResolver(), selectedLocation.getExtId());
			locationCursor.moveToNext();
			Location location = Converter.toLocation(locationCursor, true);
			String locationName = individual.getLastName();
			location.setName(locationName);
			selectedLocation.setName(locationName);
			LocationAdapter.update(skeletor.getContentResolver(), location);

			// Insert or Create SocialGroup
			Cursor socialGroupCursor = Queries.getSocialGroupByExtId(
					skeletor.getContentResolver(), selectedLocation.getExtId());
			SocialGroup socialGroup;

			if (socialGroupCursor.moveToFirst()) {
				// update the SocialGroup
				socialGroup = Converter.toSocialGroup(socialGroupCursor, true);
				socialGroup.setGroupHead(individual.getExtId());
				socialGroup.setGroupName(locationName);
				SocialGroupAdapter.update(skeletor.getContentResolver(),
						socialGroup);
			} else {
				// create the SocialGroup
				socialGroup = SocialGroupAdapter.create(
						selectedLocation.getExtId(), individual);
				SocialGroupAdapter.insertOrUpdate(
						skeletor.getContentResolver(), socialGroup);
			}
			socialGroupCursor.close();

			// create membership of Head of Household in own
			// household
			Membership membership = MembershipAdapter.create(individual,
					socialGroup, relationshipType, membershipStatus);
			MembershipAdapter.insertOrUpdate(skeletor.getContentResolver(),
					membership);

			// Set head of household's relationship to himself.
			Relationship relationship = RelationshipAdapter.create(individual,
					individual, relationshipType, startDate);
			RelationshipAdapter.insertOrUpdate(skeletor.getContentResolver(),
					relationship);

		}

	}

	public static class EditIndividual implements FormPayloadConsumer {

		@Override
		public void consumeFormPayload(Map<String, String> formPayload,
				Skeletor skeletor) {
			// TODO Auto-generated method stub
			new AddMemberOfHousehold().consumeFormPayload(formPayload, skeletor);
		}

	}
}
