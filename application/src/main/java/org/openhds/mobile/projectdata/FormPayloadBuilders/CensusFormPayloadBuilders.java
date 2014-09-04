package org.openhds.mobile.projectdata.FormPayloadBuilders;

import android.content.ContentResolver;
import org.openhds.mobile.activity.NavigateActivity;
import org.openhds.mobile.projectdata.FormAdapters.IndividualFormAdapter;
import org.openhds.mobile.repository.QueryResult;
import org.openhds.mobile.fragment.FieldWorkerLoginFragment;
import org.openhds.mobile.model.FieldWorker;
import org.openhds.mobile.model.Individual;
import org.openhds.mobile.model.Location;
import org.openhds.mobile.model.LocationHierarchy;
import org.openhds.mobile.model.Membership;
import org.openhds.mobile.projectdata.ProjectActivityBuilder;
import org.openhds.mobile.projectdata.ProjectFormFields;
import org.openhds.mobile.repository.GatewayRegistry;
import org.openhds.mobile.repository.gateway.IndividualGateway;
import org.openhds.mobile.repository.gateway.LocationGateway;
import org.openhds.mobile.repository.gateway.LocationHierarchyGateway;
import org.openhds.mobile.repository.gateway.MembershipGateway;
import org.openhds.mobile.utilities.LuhnValidator;

import java.util.Iterator;
import java.util.Map;

public class CensusFormPayloadBuilders {

    /**
     *
     * Helper methods for FormPayloadBuilders
     *
     */

    private static void addNewLocationPayload(
            Map<String, String> formPayload, NavigateActivity navigateActivity) {

        QueryResult sectorQueryResult =
                navigateActivity.getHierarchyPath().get(ProjectActivityBuilder.CensusActivityModule.SECTOR_STATE);
        ContentResolver contentResolver = navigateActivity.getContentResolver();

        // sector extid is <hierarchyExtId>
        // sector name is <sectorname>
        LocationHierarchyGateway locationHierarchyGateway = GatewayRegistry.getLocationHierarchyGateway();
        LocationHierarchy sector = locationHierarchyGateway.getFirst(contentResolver,
                locationHierarchyGateway.findById(sectorQueryResult.getExtId()));
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

        formPayload.put(ProjectFormFields.Locations.BUILDING_NUMBER, String.format("%02d", buildingNumber));
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
        String generatedIdSeqNum = String.format("%05d", nextSequence);

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
            Map<String, QueryResult> hierarchyPath = navigateActivity
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
            formPayload.put(ProjectFormFields.Individuals.RELATIONSHIP_TO_HEAD,
                    membership.getRelationshipToHead());
        }
    }
}
