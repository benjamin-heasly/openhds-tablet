package org.openhds.mobile.projectdata.FormPayloadConsumers;

import android.content.ContentResolver;
import org.openhds.mobile.activity.NavigateActivity;
import org.openhds.mobile.model.Individual;
import org.openhds.mobile.model.Location;
import org.openhds.mobile.model.Membership;
import org.openhds.mobile.model.Relationship;
import org.openhds.mobile.model.SocialGroup;
import org.openhds.mobile.projectdata.FormAdapters.IndividualFormAdapter;
import org.openhds.mobile.projectdata.FormAdapters.LocationFormAdapter;
import org.openhds.mobile.projectdata.ProjectActivityBuilder;
import org.openhds.mobile.projectdata.ProjectFormFields;
import org.openhds.mobile.projectdata.ProjectResources;
import org.openhds.mobile.repository.DataWrapper;
import org.openhds.mobile.repository.GatewayRegistry;
import org.openhds.mobile.repository.gateway.IndividualGateway;
import org.openhds.mobile.repository.gateway.LocationGateway;
import org.openhds.mobile.repository.gateway.MembershipGateway;
import org.openhds.mobile.repository.gateway.RelationshipGateway;
import org.openhds.mobile.repository.gateway.SocialGroupGateway;

import java.util.HashMap;
import java.util.Map;

public class CensusFormPayloadConsumers {

    private static Location insertOrUpdateLocation(
            Map<String, String> formPayload, NavigateActivity navigateActivity) {

        Location location = LocationFormAdapter.fromForm(formPayload);

        LocationGateway locationGateway = GatewayRegistry.getLocationGateway();
        ContentResolver contentResolver = navigateActivity.getContentResolver();
        locationGateway.insertOrUpdate(contentResolver, location);

        return location;
    }

    private static Individual insertOrUpdateIndividual(
            Map<String, String> formPayLoad, NavigateActivity navigateActivity) {

        Individual individual = IndividualFormAdapter.fromForm(formPayLoad);
        individual.setEndType(ProjectResources.Individual.RESIDENCY_END_TYPE_NA);

        IndividualGateway individualGateway = GatewayRegistry.getIndividualGateway();
        ContentResolver contentResolver = navigateActivity.getContentResolver();
        individualGateway.insertOrUpdate(contentResolver, individual);

        return individual;
    }

    public static class AddLocation implements FormPayloadConsumer {

        @Override
        public ConsumerResults consumeFormPayload(Map<String, String> formPayload,
                                                  NavigateActivity navigateActivity) {
            insertOrUpdateLocation(formPayload, navigateActivity);

                return new ConsumerResults(false, null, null);
        }

        @Override
        public void postFillFormPayload(Map<String, String> formPayload) {
            // TODO Auto-generated method stub
        }
    }

    public static class EvaluateLocation implements FormPayloadConsumer {

        @Override
        public ConsumerResults consumeFormPayload(Map<String, String> formPayload, NavigateActivity navigateActivity) {
            return new ConsumerResults(false, null, null);
        }

        @Override
        public void postFillFormPayload(Map<String, String> formPayload) {

        }
    }

    public static class AddMemberOfHousehold implements FormPayloadConsumer {

        @Override
        public ConsumerResults consumeFormPayload(Map<String, String> formPayload,
                                                  NavigateActivity navigateActivity) {

            Map<String, DataWrapper> hierarchyPath = navigateActivity
                    .getHierarchyPath();
            DataWrapper selectedLocation = hierarchyPath
                    .get(ProjectActivityBuilder.BiokoHierarchy.HOUSEHOLD_STATE);

            String relationshipType = formPayload
                    .get(ProjectFormFields.Individuals.RELATIONSHIP_TO_HEAD);
            String membershipStatus = formPayload
                    .get(ProjectFormFields.Individuals.MEMBER_STATUS);
            Individual individual = insertOrUpdateIndividual(formPayload,
                    navigateActivity);
            String startDate = formPayload
                    .get(ProjectFormFields.General.COLLECTION_DATE_TIME);

            SocialGroupGateway socialGroupGateway = GatewayRegistry.getSocialGroupGateway();
            IndividualGateway individualGateway = GatewayRegistry.getIndividualGateway();
            ContentResolver contentResolver = navigateActivity.getContentResolver();

            // get head of household by household id
            SocialGroup socialGroup = socialGroupGateway.getFirst(contentResolver,
                    socialGroupGateway.findById(selectedLocation.getExtId()));
            Individual currentHeadOfHousehold = individualGateway.getFirst(contentResolver,
                    individualGateway.findById(socialGroup.getGroupHead()));

            // INSERT or UPDATE RELATIONSHIP
            RelationshipGateway relationshipGateway = GatewayRegistry.getRelationshipGateway();
            Relationship relationship = new Relationship(individual, currentHeadOfHousehold, relationshipType, startDate);
            relationshipGateway.insertOrUpdate(contentResolver, relationship);

            // INSERT or UPDATE MEMBERSHIP
            MembershipGateway membershipGateway = GatewayRegistry.getMembershipGateway();
            Membership membership = new Membership(individual, socialGroup, relationshipType);
            membershipGateway.insertOrUpdate(contentResolver, membership);

            return new ConsumerResults(false, null, null);
        }

        @Override
        public void postFillFormPayload(Map<String, String> formPayload) {
            // TODO Auto-generated method stub

        }
    }

    public static class AddHeadOfHousehold implements FormPayloadConsumer {

        @Override
        public ConsumerResults consumeFormPayload(Map<String, String> formPayload,
                                                  NavigateActivity navigateActivity) {

            postFillFormPayload(formPayload);

            Map<String, DataWrapper> hierarchyPath = navigateActivity
                    .getHierarchyPath();
            DataWrapper selectedLocation = hierarchyPath
                    .get(ProjectActivityBuilder.BiokoHierarchy.HOUSEHOLD_STATE);

            // Pull out useful strings from the formPayload
            String relationshipType = formPayload
                    .get(ProjectFormFields.Individuals.RELATIONSHIP_TO_HEAD);
            String membershipStatus = formPayload
                    .get(ProjectFormFields.Individuals.MEMBER_STATUS);
            String startDate = formPayload
                    .get(ProjectFormFields.General.COLLECTION_DATE_TIME);
            Individual individual = insertOrUpdateIndividual(formPayload,
                    navigateActivity);

            LocationGateway locationGateway = GatewayRegistry.getLocationGateway();
            ContentResolver contentResolver = navigateActivity.getContentResolver();

            // Update the name of the location
            Location location = locationGateway.getFirst(contentResolver,
                    locationGateway.findById(selectedLocation.getExtId()));
            String locationName = individual.getLastName();
            location.setName(locationName);
            selectedLocation.setName(locationName);
            locationGateway.insertOrUpdate(contentResolver, location);

            // create social group
            SocialGroupGateway socialGroupGateway = GatewayRegistry.getSocialGroupGateway();
            SocialGroup socialGroup = new SocialGroup(selectedLocation.getExtId(), selectedLocation.getExtId(), individual);
            socialGroupGateway.insertOrUpdate(contentResolver, socialGroup);

            // create membership
            MembershipGateway membershipGateway = GatewayRegistry.getMembershipGateway();
            Membership membership = new Membership(individual, socialGroup, relationshipType);
            membershipGateway.insertOrUpdate(contentResolver, membership);

            // Set head of household's relationship to himself.
            RelationshipGateway relationshipGateway = GatewayRegistry.getRelationshipGateway();
            Relationship relationship = new Relationship(individual, individual, relationshipType, startDate);
            relationshipGateway.insertOrUpdate(contentResolver, relationship);

            return new ConsumerResults(true, null, null);
        }

        @Override
        public void postFillFormPayload(Map<String, String> formPayload) {
            formPayload.put(ProjectFormFields.Individuals.RELATIONSHIP_TO_HEAD,
                    "1");

        }

    }

    public static class EditIndividual implements FormPayloadConsumer {

        @Override
        public ConsumerResults consumeFormPayload(Map<String, String> formPayload,
                                                  NavigateActivity navigateActivity) {
            new AddMemberOfHousehold().consumeFormPayload(formPayload,
                    navigateActivity);
            return new ConsumerResults(false, null, null);

        }

        @Override
        public void postFillFormPayload(Map<String, String> formPayload) {
            // TODO Auto-generated method stub

        }

    }
}
