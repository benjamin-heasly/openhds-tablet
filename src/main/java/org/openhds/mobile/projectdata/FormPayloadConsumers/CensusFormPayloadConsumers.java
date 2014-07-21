package org.openhds.mobile.projectdata.FormPayloadConsumers;

import android.database.Cursor;
import org.openhds.mobile.activity.NavigateActivity;
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
import org.openhds.mobile.projectdata.ProjectActivityBuilder;
import org.openhds.mobile.projectdata.ProjectFormFields;

import java.util.Map;

public class CensusFormPayloadConsumers {

    private static Location insertOrUpdateLocation(
            Map<String, String> formPayLoad, NavigateActivity navigateActivity) {
        // Insert or Update the Location
        Location location = LocationAdapter.create(formPayLoad);
        LocationAdapter.insertOrUpdate(navigateActivity.getContentResolver(),
                location);
        return location;
    }

    private static Individual insertOrUpdateIndividual(
            Map<String, String> formPayLoad, NavigateActivity navigateActivity) {
        // Insert or Update the Individual
        Individual individual = IndividualAdapter.create(formPayLoad);
        IndividualAdapter.insertOrUpdate(navigateActivity.getContentResolver(),
                individual);

        return individual;
    }

    public static class AddLocation implements FormPayloadConsumer {

        @Override
        public boolean consumeFormPayload(Map<String, String> formPayload,
                                          NavigateActivity navigateActivity) {
            insertOrUpdateLocation(formPayload, navigateActivity);
            return false;
        }

        @Override
        public void postFillFormPayload(Map<String, String> formPayload) {
            // TODO Auto-generated method stub

        }
    }

    public static class AddMemberOfHousehold implements FormPayloadConsumer {

        @Override
        public boolean consumeFormPayload(Map<String, String> formPayload,
                                          NavigateActivity navigateActivity) {

            Map<String, QueryResult> hierarchyPath = navigateActivity
                    .getHierarchyPath();
            QueryResult selectedLocation = hierarchyPath
                    .get(ProjectActivityBuilder.CensusActivityModule.HOUSEHOLD_STATE);

            String relationshipType = formPayload
                    .get(ProjectFormFields.Individuals.RELATIONSHIP_TO_HEAD);
            String membershipStatus = formPayload
                    .get(ProjectFormFields.Individuals.MEMBER_STATUS);
            Individual individual = insertOrUpdateIndividual(formPayload,
                    navigateActivity);
            String startDate = formPayload
                    .get(ProjectFormFields.General.COLLECTED_DATE_TIME);

            Cursor cursor = Queries.getHeadOfHouseholdByHouseholdExtId(
                    navigateActivity.getContentResolver(),
                    selectedLocation.getExtId());
            cursor.moveToFirst();
            Individual currentHeadOfHousehold = Converter.toIndividual(cursor,
                    true);

            // INSERT or UPDATE RELATIONSHIP
            Relationship relationship = RelationshipAdapter.create(
                    individual, currentHeadOfHousehold, relationshipType,
                    startDate);
            RelationshipAdapter.insertOrUpdate(
                    navigateActivity.getContentResolver(), relationship);

            // INSERT or UPDATE MEMBERSHIP
            Cursor socialGroupCursor = Queries.getSocialGroupByExtId(
                    navigateActivity.getContentResolver(),
                    selectedLocation.getExtId());
            if (socialGroupCursor.moveToFirst()) {
                SocialGroup socialGroup = Converter.toSocialGroup(
                        socialGroupCursor, true);
                Membership membership = MembershipAdapter.create(individual,
                        socialGroup, relationshipType, membershipStatus);

                MembershipAdapter.insertOrUpdate(
                        navigateActivity.getContentResolver(), membership);
            }
            socialGroupCursor.close();

            return false;

        }

        @Override
        public void postFillFormPayload(Map<String, String> formPayload) {
            // TODO Auto-generated method stub

        }
    }

    public static class AddHeadOfHousehold implements FormPayloadConsumer {

        @Override
        public boolean consumeFormPayload(Map<String, String> formPayload,
                                          NavigateActivity navigateActivity) {

            postFillFormPayload(formPayload);
            boolean postFilled = true;



            Map<String, QueryResult> hierarchyPath = navigateActivity
                    .getHierarchyPath();
            QueryResult selectedLocation = hierarchyPath
                    .get(ProjectActivityBuilder.CensusActivityModule.HOUSEHOLD_STATE);

            // Pull out useful strings from the formPayload
            String relationshipType = formPayload
                    .get(ProjectFormFields.Individuals.RELATIONSHIP_TO_HEAD);
            String membershipStatus = formPayload
                    .get(ProjectFormFields.Individuals.MEMBER_STATUS);
            String startDate = formPayload
                    .get(ProjectFormFields.General.COLLECTED_DATE_TIME);
            Individual individual = insertOrUpdateIndividual(formPayload,
                    navigateActivity);

            // Update the name of the location
            Cursor locationCursor = Queries.getLocationByExtId(
                    navigateActivity.getContentResolver(),
                    selectedLocation.getExtId());
            locationCursor.moveToNext();
            Location location = Converter.toLocation(locationCursor, true);
            String locationName = individual.getLastName();
            location.setName(locationName);
            selectedLocation.setName(locationName);
            LocationAdapter.update(navigateActivity.getContentResolver(),
                    location);

            // create socialgroup
            SocialGroup socialGroup;
            socialGroup = SocialGroupAdapter.create(
                    selectedLocation.getExtId(), individual);
            SocialGroupAdapter.insertOrUpdate(
                    navigateActivity.getContentResolver(), socialGroup);

            // create membership
            Membership membership = MembershipAdapter.create(individual,
                    socialGroup, relationshipType, membershipStatus);
            MembershipAdapter.insertOrUpdate(
                    navigateActivity.getContentResolver(), membership);

            // Set head of household's relationship to himself.
            Relationship relationship = RelationshipAdapter.create(individual,
                    individual, relationshipType, startDate);
            RelationshipAdapter.insertOrUpdate(
                    navigateActivity.getContentResolver(), relationship);

            return postFilled;

        }

        @Override
        public void postFillFormPayload(Map<String, String> formPayload) {
            formPayload.put(ProjectFormFields.Individuals.RELATIONSHIP_TO_HEAD,
                    "1");

        }

    }

    public static class EditIndividual implements FormPayloadConsumer {

        @Override
        public boolean consumeFormPayload(Map<String, String> formPayload,
                                          NavigateActivity navigateActivity) {
            new AddMemberOfHousehold().consumeFormPayload(formPayload,
                    navigateActivity);
            return false;

        }

        @Override
        public void postFillFormPayload(Map<String, String> formPayload) {
            // TODO Auto-generated method stub

        }

    }
}
