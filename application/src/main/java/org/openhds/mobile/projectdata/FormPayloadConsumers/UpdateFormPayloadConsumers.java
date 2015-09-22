package org.openhds.mobile.projectdata.FormPayloadConsumers;

import android.content.ContentResolver;

import org.openhds.mobile.activity.NavigateActivity;
import org.openhds.mobile.model.core.Individual;
import org.openhds.mobile.model.core.Location;
import org.openhds.mobile.model.core.Membership;
import org.openhds.mobile.model.core.Relationship;
import org.openhds.mobile.model.core.SocialGroup;
import org.openhds.mobile.model.update.Visit;
import org.openhds.mobile.projectdata.FormAdapters.IndividualFormAdapter;
import org.openhds.mobile.projectdata.FormAdapters.VisitFormAdapter;
import org.openhds.mobile.projectdata.ProjectActivityBuilder;
import org.openhds.mobile.projectdata.ProjectFormFields;
import org.openhds.mobile.repository.DataWrapper;
import org.openhds.mobile.repository.GatewayRegistry;
import org.openhds.mobile.repository.gateway.IndividualGateway;
import org.openhds.mobile.repository.gateway.LocationGateway;
import org.openhds.mobile.repository.gateway.MembershipGateway;
import org.openhds.mobile.repository.gateway.RelationshipGateway;
import org.openhds.mobile.repository.gateway.SocialGroupGateway;
import org.openhds.mobile.repository.gateway.VisitGateway;

import java.util.HashMap;
import java.util.Map;

public class UpdateFormPayloadConsumers {

    public static class StartAVisit implements FormPayloadConsumer {

        @Override
        public ConsumerResults consumeFormPayload(Map<String, String> formPayload, NavigateActivity navigateActivity) {

            Visit visit = VisitFormAdapter.fromForm(formPayload);

            VisitGateway visitGateway = GatewayRegistry.getVisitGateway();
            ContentResolver contentResolver = navigateActivity.getContentResolver();
            visitGateway.insertOrUpdate(contentResolver, visit);

            navigateActivity.startVisit(visit);

            return new ConsumerResults(false, null, null);
        }

        @Override
        public void postFillFormPayload(Map<String, String> formPayload) {
            // TODO Auto-generated method stub

        }
    }

    public static class RegisterDeath implements FormPayloadConsumer {

        @Override
        public ConsumerResults consumeFormPayload(Map<String, String> formPayload, NavigateActivity navigateActivity) {

            IndividualGateway individualGateway = GatewayRegistry.getIndividualGateway();
            ContentResolver contentResolver = navigateActivity.getContentResolver();

            String individualExtId = formPayload.get(ProjectFormFields.Individuals.INDIVIDUAL_EXTID);
            Individual deceasedIndividual = individualGateway.getFirst(contentResolver,
                    individualGateway.findByExtIdPrefixDescending(individualExtId));

            individualGateway.insertOrUpdate(contentResolver, deceasedIndividual);

            return new ConsumerResults(false, null, null);
        }

        @Override
        public void postFillFormPayload(Map<String, String> formPayload) {
            // TODO Auto-generated method stub
        }
    }

    public static class RegisterOutMigration implements FormPayloadConsumer {
        @Override
        public ConsumerResults consumeFormPayload(Map<String, String> formPayload, NavigateActivity navigateActivity) {
            // update the individual's residency end type
            String individualUuid = formPayload.get(ProjectFormFields.Individuals.INDIVIDUAL_UUID);
            IndividualGateway individualGateway = GatewayRegistry.getIndividualGateway();
            Individual individual = individualGateway.getFirst(navigateActivity.getContentResolver(),
                    individualGateway.findById(individualUuid));
            if (null == individual) {
                return new ConsumerResults(false, null, null);
            }

            individualGateway.insertOrUpdate(navigateActivity.getContentResolver(), individual);
            return new ConsumerResults(false, null, null);
        }

        @Override
        public void postFillFormPayload(Map<String, String> formPayload) {

        }
    }

    public static class RegisterInMigration implements FormPayloadConsumer {
        @Override
        public ConsumerResults consumeFormPayload(Map<String, String> formPayload, NavigateActivity navigateActivity) {
            // find the migrating individual
            String individualUuid = formPayload.get(ProjectFormFields.Individuals.INDIVIDUAL_UUID);
            IndividualGateway individualGateway = GatewayRegistry.getIndividualGateway();
            Individual individual = individualGateway.getFirst(
                    navigateActivity.getContentResolver(),
                    individualGateway.findById(individualUuid));
            if (null == individual) {
                return new ConsumerResults(true, null, null);
            }

            // update the individual's residency
            String locationUuid = formPayload.get(ProjectFormFields.Locations.LOCATION_UUID);
            individualGateway.insertOrUpdate(navigateActivity.getContentResolver(), individual);

            // post-fill individual extId into the form for display in UI
            formPayload.put(ProjectFormFields.Individuals.INDIVIDUAL_EXTID, individual.getExtId());
            return new ConsumerResults(true, null, null);
        }

        @Override
        public void postFillFormPayload(Map<String, String> formPayload) {
            formPayload.put(ProjectFormFields.General.ENTITY_EXTID, formPayload.get(ProjectFormFields.Individuals.INDIVIDUAL_EXTID));
            formPayload.put(ProjectFormFields.General.ENTITY_UUID, formPayload.get(ProjectFormFields.Individuals.INDIVIDUAL_UUID));
        }
    }

    public static class AddIndividualFromInMigration implements FormPayloadConsumer {

        @Override
        public ConsumerResults consumeFormPayload(Map<String, String> formPayload,
                                                  NavigateActivity navigateActivity) {

            Map<String, DataWrapper> hierarchyPath = navigateActivity
                    .getHierarchyPath();
            DataWrapper selectedLocation = hierarchyPath
                    .get(ProjectActivityBuilder.BiokoHierarchy.HOUSEHOLD_STATE);

            ContentResolver contentResolver = navigateActivity.getContentResolver();

            String relationshipType = formPayload.get(ProjectFormFields.Individuals.RELATIONSHIP_TO_HEAD);

            // Pull out useful strings from the formPayload
            String membershipStatus = formPayload
                    .get(ProjectFormFields.Individuals.MEMBER_STATUS);
            String startDate = formPayload
                    .get(ProjectFormFields.General.COLLECTION_DATE_TIME);


            // insert or update individual
            Individual individual = IndividualFormAdapter.fromForm(formPayload);
            IndividualGateway individualGateway = GatewayRegistry.getIndividualGateway();
            individualGateway.insertOrUpdate(contentResolver, individual);


            // Update the name of the location
            LocationGateway locationGateway = GatewayRegistry.getLocationGateway();
            Location location = locationGateway.getFirst(contentResolver,
                    locationGateway.findById(selectedLocation.getUuid()));
            String locationName = individual.getLastName();
            location.setName(locationName);
            selectedLocation.setName(locationName);
            locationGateway.insertOrUpdate(contentResolver, location);


            // create social group
            SocialGroupGateway socialGroupGateway = GatewayRegistry.getSocialGroupGateway();
            SocialGroup socialGroup = new SocialGroup(selectedLocation.getName(), individual, formPayload.get(ProjectFormFields.Individuals.SOCIALGROUP_UUID), selectedLocation.getExtId());
            socialGroupGateway.insertOrUpdate(contentResolver, socialGroup);

            // create membership
            MembershipGateway membershipGateway = GatewayRegistry.getMembershipGateway();
            Membership membership = new Membership(individual, socialGroup, relationshipType, formPayload.get(ProjectFormFields.Individuals.MEMBERSHIP_UUID));
            membershipGateway.insertOrUpdate(contentResolver, membership);

            // Set head of household's relationship to himself.
            RelationshipGateway relationshipGateway = GatewayRegistry.getRelationshipGateway();
            Relationship relationship = new Relationship(individual, individual, relationshipType, startDate, formPayload.get(ProjectFormFields.Individuals.RELATIONSHIP_UUID));
            relationshipGateway.insertOrUpdate(contentResolver, relationship);


            Map<String, String> hints = new HashMap<>();
            hints.put(ProjectFormFields.Individuals.INDIVIDUAL_EXTID, formPayload.get(ProjectFormFields.Individuals.INDIVIDUAL_EXTID));
            hints.put(ProjectFormFields.Individuals.INDIVIDUAL_UUID, formPayload.get(ProjectFormFields.General.ENTITY_UUID));
            hints.put(ProjectFormFields.Locations.LOCATION_EXTID, formPayload.get(ProjectFormFields.General.HOUSEHOLD_STATE_FIELD_NAME));
            return new ConsumerResults(false, ProjectActivityBuilder.UpdateActivityModule.externalInMigrationFormBehaviour, hints);
        }

        @Override
        public void postFillFormPayload(Map<String, String> formPayload) {

        }

    }
}