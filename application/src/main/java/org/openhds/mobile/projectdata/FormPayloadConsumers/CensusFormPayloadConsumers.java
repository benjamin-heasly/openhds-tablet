package org.openhds.mobile.projectdata.FormPayloadConsumers;

import android.content.ContentResolver;
import org.openhds.mobile.activity.NavigateActivity;
import org.openhds.mobile.model.*;
import org.openhds.mobile.projectdata.FormAdapters.IndividualFormAdapter;
import org.openhds.mobile.projectdata.FormAdapters.LocationFormAdapter;
import org.openhds.mobile.projectdata.FormAdapters.VisitFormAdapter;
import org.openhds.mobile.projectdata.ProjectActivityBuilder;
import org.openhds.mobile.projectdata.ProjectFormFields;
import org.openhds.mobile.projectdata.ProjectResources;
import org.openhds.mobile.repository.DataWrapper;
import org.openhds.mobile.repository.GatewayRegistry;
import org.openhds.mobile.repository.gateway.*;

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

            LocationGateway locationGateway = GatewayRegistry.getLocationGateway();

            Location location = locationGateway.getFirst(navigateActivity.getContentResolver(),
                    locationGateway.findById(formPayload.get(ProjectFormFields.Locations.LOCATION_EXTID)));

            location.setStatus(formPayload.get(ProjectFormFields.Locations.EVALUATION));

            locationGateway.insertOrUpdate(navigateActivity.getContentResolver(), location);

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

            ConsumerResults pregnantResults;
            if(null !=  (pregnantResults = checkIfPregnant(formPayload))){
                return pregnantResults;
            }
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

            ConsumerResults pregnantResults;
            if(null !=  (pregnantResults = checkIfPregnant(formPayload))){
                return pregnantResults;
            }
            return new ConsumerResults(false, null, null);
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

    private static ConsumerResults checkIfPregnant(Map<String,String> formPayload){

        String pregnant = formPayload.get(ProjectFormFields.Individuals.IS_PREGNANT_FLAG);

        if(null != pregnant && pregnant.equals("Yes")) {
            Map<String, String> hints = new HashMap<>();
            hints.put(ProjectFormFields.Individuals.INDIVIDUAL_EXTID, formPayload.get(ProjectFormFields.Individuals.INDIVIDUAL_EXTID));
            return new ConsumerResults(false, ProjectActivityBuilder.CensusActivityModule.visitPregObFormBehaviour, hints);
        }

        return null;
    }


    // Used for Form Launch Sequences
    public static class StartAVisitForPregnancyObservation implements FormPayloadConsumer {

        @Override
        public ConsumerResults consumeFormPayload(Map<String, String> formPayload, NavigateActivity navigateActivity) {

            Visit visit = VisitFormAdapter.fromForm(formPayload);

            VisitGateway visitGateway = GatewayRegistry.getVisitGateway();
            ContentResolver contentResolver = navigateActivity.getContentResolver();
            visitGateway.insertOrUpdate(contentResolver, visit);

            navigateActivity.startVisit(visit);

            return new ConsumerResults(false, ProjectActivityBuilder.CensusActivityModule.PregObFormBehaviour, navigateActivity.getPreviousConsumerResults().getFollowUpFormHints());
        }

        @Override
        public void postFillFormPayload(Map<String, String> formPayload) {
            // TODO Auto-generated method stub

        }
    }

    public static class PregnancyObservation implements FormPayloadConsumer {

        @Override
        public ConsumerResults consumeFormPayload(Map<String, String> formPayload, NavigateActivity navigateActivity) {

            // Since this is happening as part of a sequence it made sense to me to automatically close the
            // visit after completion of the sequence.
            navigateActivity.finishVisit();

            return new ConsumerResults(false, null, null);
        }

        @Override
        public void postFillFormPayload(Map<String, String> formPayload) {

        }

    }
}
