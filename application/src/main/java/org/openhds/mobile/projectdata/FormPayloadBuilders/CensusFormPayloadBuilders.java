package org.openhds.mobile.projectdata.FormPayloadBuilders;

import android.content.ContentResolver;
import org.openhds.mobile.activity.NavigateActivity;
import org.openhds.mobile.model.*;
import org.openhds.mobile.projectdata.FormAdapters.IndividualFormAdapter;
import org.openhds.mobile.projectdata.ProjectResources;
import org.openhds.mobile.repository.DataWrapper;
import org.openhds.mobile.fragment.FieldWorkerLoginFragment;
import org.openhds.mobile.projectdata.ProjectActivityBuilder;
import org.openhds.mobile.projectdata.ProjectFormFields;
import org.openhds.mobile.repository.GatewayRegistry;
import org.openhds.mobile.repository.Query;
import org.openhds.mobile.repository.gateway.*;
import org.openhds.mobile.utilities.LuhnValidator;

import java.lang.reflect.Member;
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
                navigateActivity.getHierarchyPath().get(ProjectActivityBuilder.CensusActivityModule.SECTOR_STATE);
        ContentResolver contentResolver = navigateActivity.getContentResolver();

        // sector extid is <hierarchyExtId>
        // sector name is <sectorname>
        LocationHierarchyGateway locationHierarchyGateway = GatewayRegistry.getLocationHierarchyGateway();
        LocationHierarchy sector = locationHierarchyGateway.getFirst(contentResolver,
                locationHierarchyGateway.findById(sectorDataWrapper.getExtId()));
        formPayload.put(ProjectFormFields.Locations.HIERERCHY_EXTID, sector.getExtId());
        formPayload.put(ProjectFormFields.Locations.SECTOR_NAME, sector.getName());

        // map area name is <mapAreaName>
        LocationHierarchy mapArea = locationHierarchyGateway.getFirst(contentResolver,
                locationHierarchyGateway.findById(sector.getParent()));
        formPayload.put(ProjectFormFields.Locations.MAP_AREA_NAME, mapArea.getName());

        // locality is <localityName>
        LocationHierarchy locality = locationHierarchyGateway.getFirst(contentResolver,
                locationHierarchyGateway.findById(mapArea.getParent()));
        formPayload.put(ProjectFormFields.Locations.LOCALITY_NAME, locality.getName());

        // default to 1 for <locationFloorNumber />
        formPayload.put(ProjectFormFields.Locations.FLOOR_NUMBER, "1");

        LocationGateway locationGateway = GatewayRegistry.getLocationGateway();

        // location with largest building number <locationBuildingNumber />
        int buildingNumber = 1;
        Iterator<Location> locationIterator = locationGateway.getIterator(contentResolver,
                locationGateway.findByHierarchyDescendingBuildingNumber(sector.getExtId()));
        if (locationIterator.hasNext()) {
            buildingNumber += locationIterator.next().getBuildingNumber();
        }

        formPayload.put(ProjectFormFields.Locations.BUILDING_NUMBER, String.format(LIKE_WILD_CARD + "02d", buildingNumber));
    }

    private static void addNewIndividualPayload(
            Map<String, String> formPayload, NavigateActivity navigateActivity) {

        FieldWorker fieldWorker = (FieldWorker) navigateActivity.getIntent()
                .getExtras().get(FieldWorkerLoginFragment.FIELD_WORKER_EXTRA);

        String generatedIdPrefix = fieldWorker.getCollectedIdPrefix();

        IndividualGateway individualGateway = GatewayRegistry.getIndividualGateway();
        ContentResolver contentResolver = navigateActivity.getContentResolver();

        Iterator<Individual> individualIterator = individualGateway.getIterator(contentResolver,
                individualGateway.findByExtIdPrefixDescending(generatedIdPrefix));
        int nextSequence = 0;
        if (individualIterator.hasNext()) {
            String lastExtId = individualIterator.next().getExtId();
            int prefixLength = generatedIdPrefix.length();
            int checkDigitLength = 1;
            String lastSequenceNumber = lastExtId.substring(prefixLength + 1,
                    lastExtId.length() - checkDigitLength);
            nextSequence = Integer.parseInt(lastSequenceNumber) + 1;
        }

        // TODO: break out 5-digit number format, don't use string literal here.
        String generatedIdSeqNum = String.format(LIKE_WILD_CARD + "05d", nextSequence);

        Character generatedIdCheck = LuhnValidator
                .generateCheckCharacter(generatedIdSeqNum + generatedIdSeqNum);

        String individualExtId = generatedIdPrefix + generatedIdSeqNum
                + generatedIdCheck.toString();

        formPayload.put(ProjectFormFields.Individuals.INDIVIDUAL_EXTID,
                individualExtId);
        formPayload.put(ProjectFormFields.Individuals.AGE_UNITS, "Years");


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
                    socialGroupGateway.findById(navigateActivity.getCurrentSelection().getExtId()));

            IndividualGateway individualGateway = new IndividualGateway();
            Individual headOfHousehold = individualGateway.getFirst(resolver, individualGateway.findById(socialGroup.getGroupHead()));



            // set's the member's "House Name" to the Location's name
            formPayload.put(ProjectFormFields.Individuals.OTHER_NAMES, headOfHousehold.getOtherNames());

            // set's the member's point of contanct info to the HoH
            if(null != headOfHousehold.getPhoneNumber() && !headOfHousehold.getPhoneNumber().isEmpty()) {
                formPayload.put(ProjectFormFields.Individuals.POINT_OF_CONTACT_NAME, Individual.getFullName(headOfHousehold));
                formPayload.put(ProjectFormFields.Individuals.POINT_OF_CONTACT_PHONE_NUMBER, headOfHousehold.getPhoneNumber());
            } else {
                formPayload.put(ProjectFormFields.Individuals.POINT_OF_CONTACT_NAME, headOfHousehold.getPointOfContactName());
                formPayload.put(ProjectFormFields.Individuals.POINT_OF_CONTACT_PHONE_NUMBER, headOfHousehold.getPointOfContactPhoneNumber());
            }


        }

    }

    public static class AddHeadOfHousehold implements FormPayloadBuilder {

        @Override
        public void buildFormPayload(Map<String, String> formPayload,
                                     NavigateActivity navigateActivity) {

            PayloadTools.addMinimalFormPayload(formPayload, navigateActivity);
            PayloadTools.flagForReview(formPayload, false);
            addNewIndividualPayload(formPayload, navigateActivity);

            formPayload.put(ProjectFormFields.Individuals.HEAD_PREFILLED_FLAG, "true");

        }

    }

    public static class EditIndividual implements FormPayloadBuilder {

        @Override
        public void buildFormPayload(Map<String, String> formPayload,
                                     NavigateActivity navigateActivity) {

            PayloadTools.addMinimalFormPayload(formPayload, navigateActivity);
            PayloadTools.flagForReview(formPayload, true);

            // build complete individual form
            Map<String, DataWrapper> hierarchyPath = navigateActivity
                    .getHierarchyPath();

            String individualExtId = hierarchyPath
                    .get(ProjectActivityBuilder.CensusActivityModule.INDIVIDUAL_STATE)
                    .getExtId();
            String householdExtId = hierarchyPath
                    .get(ProjectActivityBuilder.CensusActivityModule.HOUSEHOLD_STATE)
                    .getExtId();

            IndividualGateway individualGateway = GatewayRegistry.getIndividualGateway();
            ContentResolver contentResolver = navigateActivity.getContentResolver();
            Individual individual = individualGateway.getFirst(contentResolver,
                    individualGateway.findById(individualExtId));

            formPayload.putAll(IndividualFormAdapter.toForm(individual));

            //TODO: Change the birthday to either a simple date object or find a better way to handle this functionality.
            String truncatedDate = formPayload.get(ProjectFormFields.Individuals.DATE_OF_BIRTH).substring(0, 10);
            formPayload.remove(ProjectFormFields.Individuals.DATE_OF_BIRTH);
            formPayload.put(ProjectFormFields.Individuals.DATE_OF_BIRTH, truncatedDate);

            MembershipGateway membershipGateway = GatewayRegistry.getMembershipGateway();
            Membership membership = membershipGateway.getFirst(contentResolver,
                    membershipGateway.findBySocialGroupAndIndividual(householdExtId, individualExtId));
            if (null != membership) {
                formPayload.put(ProjectFormFields.Individuals.RELATIONSHIP_TO_HEAD,
                        membership.getRelationshipToHead());
            }
        }
    }
}
