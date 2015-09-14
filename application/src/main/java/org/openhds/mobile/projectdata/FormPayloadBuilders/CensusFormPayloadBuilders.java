package org.openhds.mobile.projectdata.FormPayloadBuilders;

import android.content.ContentResolver;
import org.openhds.mobile.activity.NavigateActivity;
import org.openhds.mobile.model.core.*;
import org.openhds.mobile.projectdata.FormAdapters.IndividualFormAdapter;
import org.openhds.mobile.repository.DataWrapper;
import org.openhds.mobile.fragment.FieldWorkerLoginFragment;
import org.openhds.mobile.projectdata.ProjectActivityBuilder;
import org.openhds.mobile.projectdata.ProjectFormFields;
import org.openhds.mobile.repository.GatewayRegistry;
import org.openhds.mobile.repository.gateway.*;
import org.openhds.mobile.utilities.IdHelper;

import java.util.Iterator;
import java.util.Map;

import static org.openhds.mobile.repository.RepositoryUtils.LIKE_WILD_CARD;


public class CensusFormPayloadBuilders {

    /**
     *
     * Helper methods for FormPayloadBuilders
     *
     */

    private static void addNewLocationPayload(
            Map<String, String> formPayload, NavigateActivity navigateActivity) {

        DataWrapper sectorDataWrapper =
                navigateActivity.getHierarchyPath().get(ProjectActivityBuilder.BiokoHierarchy.SECTOR_STATE);
        ContentResolver contentResolver = navigateActivity.getContentResolver();

        // sector extid is <hierarchyExtId>
        // sector name is <sectorname>
        LocationHierarchyGateway locationHierarchyGateway = GatewayRegistry.getLocationHierarchyGateway();
        LocationHierarchy sector = locationHierarchyGateway.getFirst(contentResolver,
                locationHierarchyGateway.findById(sectorDataWrapper.getUuid()));
        formPayload.put(ProjectFormFields.Locations.HIERERCHY_EXTID, sector.getExtId());
        formPayload.put(ProjectFormFields.Locations.HIERERCHY_UUID, sector.getUuid());
        formPayload.put(ProjectFormFields.Locations.HIERERCHY_PARENT_UUID, sector.getParentUuid());
        formPayload.put(ProjectFormFields.Locations.SECTOR_NAME, sector.getName());

        // map area name is <mapAreaName>
        LocationHierarchy mapArea = locationHierarchyGateway.getFirst(contentResolver,
                locationHierarchyGateway.findById(sector.getParentUuid()));
        formPayload.put(ProjectFormFields.Locations.MAP_AREA_NAME, mapArea.getName());

        // locality is <localityName>
        LocationHierarchy locality = locationHierarchyGateway.getFirst(contentResolver,
                locationHierarchyGateway.findById(mapArea.getParentUuid()));
        formPayload.put(ProjectFormFields.Locations.LOCALITY_NAME, locality.getName());

        // default to 1 for <locationFloorNumber />
        formPayload.put(ProjectFormFields.Locations.FLOOR_NUMBER, "1");

        LocationGateway locationGateway = GatewayRegistry.getLocationGateway();

        // location with largest building number <locationBuildingNumber />
        int buildingNumber = 1;
        // name and code of community will default to empty String if this sector has no neighboring location
        String communityName = "";
        String communityCode = "";
        Iterator<Location> locationIterator = locationGateway.getIterator(contentResolver,
                locationGateway.findByHierarchyDescendingBuildingNumber(sector.getUuid()));
        if (locationIterator.hasNext()) {
            Location location = locationIterator.next();
            buildingNumber += location.getBuildingNumber();
            communityName = location.getCommunityName();
            communityCode = location.getCommunityCode();
        }

        formPayload.put(ProjectFormFields.Locations.BUILDING_NUMBER, String.format(LIKE_WILD_CARD + "02d", buildingNumber));
        formPayload.put(ProjectFormFields.Locations.COMMUNITY_NAME, communityName);
        formPayload.put(ProjectFormFields.Locations.COMMUNITY_CODE, communityCode);
        formPayload.put(ProjectFormFields.General.ENTITY_UUID, IdHelper.generateEntityUuid());

    }

    private static void addNewIndividualPayload(
            Map<String, String> formPayload, NavigateActivity navigateActivity) {

        DataWrapper locationDataWrapper = navigateActivity.getHierarchyPath().get(ProjectActivityBuilder.BiokoHierarchy.HOUSEHOLD_STATE);

        String individualExtId = IdHelper.generateIndividualExtId(navigateActivity.getContentResolver(), locationDataWrapper);

        formPayload.put(ProjectFormFields.Individuals.INDIVIDUAL_EXTID,
                individualExtId);

        formPayload.put(ProjectFormFields.Individuals.HOUSEHOLD_UUID, navigateActivity.getCurrentSelection().getUuid());

        formPayload.put(ProjectFormFields.General.ENTITY_EXTID,
                individualExtId);
        formPayload.put(ProjectFormFields.General.ENTITY_UUID,
                IdHelper.generateEntityUuid());

    }

    /**
     *
     * Census Form Payload Builders
     *
     */

    public static class AddLocation implements FormPayloadBuilder {

        @Override
        public void buildFormPayload(Map<String, String> formPayload,
                                     NavigateActivity navigateActivity) {

            PayloadTools.addMinimalFormPayload(formPayload, navigateActivity);
            PayloadTools.flagForReview(formPayload, false);
            addNewLocationPayload(formPayload, navigateActivity);

        }
    }

    public static class EvaluateLocation implements FormPayloadBuilder {

        @Override
        public void buildFormPayload(Map<String, String> formPayload,
                                     NavigateActivity navigateActivity) {

            PayloadTools.addMinimalFormPayload(formPayload, navigateActivity);
            PayloadTools.flagForReview(formPayload, false);

            String locationExtId = navigateActivity.getHierarchyPath()
                    .get(ProjectActivityBuilder.BiokoHierarchy.HOUSEHOLD_STATE).getExtId();
            String locationUuid = navigateActivity.getHierarchyPath()
                    .get(ProjectActivityBuilder.BiokoHierarchy.HOUSEHOLD_STATE).getUuid();
            formPayload.put(ProjectFormFields.Locations.LOCATION_EXTID, locationExtId);
            formPayload.put(ProjectFormFields.Locations.LOCATION_UUID, locationUuid);
            formPayload.put(ProjectFormFields.General.ENTITY_EXTID, locationExtId);
            formPayload.put(ProjectFormFields.General.ENTITY_UUID, locationUuid);

        }
    }

    public static class AddMemberOfHousehold implements FormPayloadBuilder {

        @Override
        public void buildFormPayload(Map<String, String> formPayload,
                                     NavigateActivity navigateActivity) {

            PayloadTools.addMinimalFormPayload(formPayload, navigateActivity);
            PayloadTools.flagForReview(formPayload, false);
            addNewIndividualPayload(formPayload, navigateActivity);

            ContentResolver resolver = navigateActivity.getContentResolver();

            SocialGroupGateway socialGroupGateway = new SocialGroupGateway();
            SocialGroup socialGroup = socialGroupGateway.getFirst(resolver,
                    socialGroupGateway.findByExtId(navigateActivity.getCurrentSelection().getExtId()));

            IndividualGateway individualGateway = new IndividualGateway();
            //HoH is found by searching by extId, since we're currently dependent on the groupHead property of socialgroup
            //Set as the individual's extId
            Individual headOfHousehold = individualGateway.getFirst(resolver, individualGateway.findById(socialGroup.getGroupHeadUuid()));

            // set's the member's point of contact info to the HoH
            if(null != headOfHousehold.getPhoneNumber() && !headOfHousehold.getPhoneNumber().isEmpty()) {
                formPayload.put(ProjectFormFields.Individuals.POINT_OF_CONTACT_NAME, Individual.getFullName(headOfHousehold));
                formPayload.put(ProjectFormFields.Individuals.POINT_OF_CONTACT_PHONE_NUMBER, headOfHousehold.getPhoneNumber());
            } else {
                formPayload.put(ProjectFormFields.Individuals.POINT_OF_CONTACT_NAME, headOfHousehold.getPointOfContactName());
                formPayload.put(ProjectFormFields.Individuals.POINT_OF_CONTACT_PHONE_NUMBER, headOfHousehold.getPointOfContactPhoneNumber());
            }

            // we need to add the socialgroup, membership, and relationship UUID for when they're created in
            // the consumers. We add them now so they are a part of the form when it is passed up.
            formPayload.put(ProjectFormFields.Individuals.RELATIONSHIP_UUID, IdHelper.generateEntityUuid());
            formPayload.put(ProjectFormFields.Individuals.MEMBERSHIP_UUID, IdHelper.generateEntityUuid());
            formPayload.put(ProjectFormFields.Individuals.SOCIALGROUP_UUID, socialGroup.getUuid());


        }

    }

    public static class AddHeadOfHousehold implements FormPayloadBuilder {

        @Override
        public void buildFormPayload(Map<String, String> formPayload,
                                     NavigateActivity navigateActivity) {

            PayloadTools.addMinimalFormPayload(formPayload, navigateActivity);
            PayloadTools.flagForReview(formPayload, false);
            addNewIndividualPayload(formPayload, navigateActivity);

            // we need to add the socialgroup, membership, and relationship UUID for when they're created in
            // the consumers. We add them now so they are a part of the form when it is passed up.
            formPayload.put(ProjectFormFields.Individuals.SOCIALGROUP_UUID, IdHelper.generateEntityUuid());
            formPayload.put(ProjectFormFields.Individuals.RELATIONSHIP_UUID, IdHelper.generateEntityUuid());
            formPayload.put(ProjectFormFields.Individuals.MEMBERSHIP_UUID, IdHelper.generateEntityUuid());

            formPayload.put(ProjectFormFields.Individuals.HEAD_PREFILLED_FLAG, "true");

        }

    }

    // This is (as of ascii-asteroid 2) not being called/utilized
    public static class EditIndividual implements FormPayloadBuilder {

        @Override
        public void buildFormPayload(Map<String, String> formPayload,
                                     NavigateActivity navigateActivity) {

            PayloadTools.addMinimalFormPayload(formPayload, navigateActivity);
            PayloadTools.flagForReview(formPayload, true);

            // build complete individual form
            Map<String, DataWrapper> hierarchyPath = navigateActivity
                    .getHierarchyPath();

            String individualUuid = hierarchyPath
                    .get(ProjectActivityBuilder.BiokoHierarchy.INDIVIDUAL_STATE)
                    .getUuid();
            String householdUuid = hierarchyPath
                    .get(ProjectActivityBuilder.BiokoHierarchy.HOUSEHOLD_STATE)
                    .getUuid();

            IndividualGateway individualGateway = GatewayRegistry.getIndividualGateway();
            ContentResolver contentResolver = navigateActivity.getContentResolver();
            Individual individual = individualGateway.getFirst(contentResolver,
                    individualGateway.findById(individualUuid));

            formPayload.putAll(IndividualFormAdapter.toForm(individual));

            //TODO: Change the birthday to either a simple date object or find a better way to handle this functionality.
            String truncatedDate = formPayload.get(ProjectFormFields.Individuals.DATE_OF_BIRTH).substring(0, 10);
            formPayload.remove(ProjectFormFields.Individuals.DATE_OF_BIRTH);
            formPayload.put(ProjectFormFields.Individuals.DATE_OF_BIRTH, truncatedDate);

            MembershipGateway membershipGateway = GatewayRegistry.getMembershipGateway();
            Membership membership = membershipGateway.getFirst(contentResolver,
                    membershipGateway.findBySocialGroupAndIndividual(householdUuid, individualUuid));
            if (null != membership) {
                formPayload.put(ProjectFormFields.Individuals.RELATIONSHIP_TO_HEAD,
                        membership.getRelationshipToHead());
            }
        }
    }
}
