package org.openhds.mobile.projectdata.FormPayloadBuilders;

import android.database.Cursor;
import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.activity.NavigateActivity;
import org.openhds.mobile.database.IndividualAdapter;
import org.openhds.mobile.database.queries.Converter;
import org.openhds.mobile.database.queries.Queries;
import org.openhds.mobile.database.queries.QueryResult;
import org.openhds.mobile.fragment.FieldWorkerLoginFragment;
import org.openhds.mobile.model.FieldWorker;
import org.openhds.mobile.model.Individual;
import org.openhds.mobile.model.LocationHierarchy;
import org.openhds.mobile.model.Membership;
import org.openhds.mobile.projectdata.ProjectActivityBuilder;
import org.openhds.mobile.projectdata.ProjectFormFields;
import org.openhds.mobile.utilities.LuhnValidator;

import java.util.Map;

import static org.openhds.mobile.database.queries.Queries.getIndividualsExtIdsByPrefix;

public class CensusFormPayloadBuilders {

    /**
     *
     * Helper methods for FormPayloadBuilders
     *
     */

    private static void addNewLocationPayload(
            Map<String, String> formPayload, NavigateActivity navigateActivity) {

        // sector extId -> <hierarchyExtId />
        // sector name -> <sectorName />
        QueryResult sectorQueryResult =
                navigateActivity.getHierarchyPath().get(ProjectActivityBuilder.CensusActivityModule.SECTOR_STATE);
        Cursor cursor =
                Queries.getHierarchyByExtId(navigateActivity.getContentResolver(), sectorQueryResult.getExtId());
        if( !cursor.moveToFirst()) {
            cursor.close();
            return;
        }
        LocationHierarchy sector = Converter.toHierarchy(cursor, true);
        formPayload.put(ProjectFormFields.Locations.HIERERCHY_EXTID, sector.getExtId());
        formPayload.put(ProjectFormFields.Locations.SECTOR_NAME, sector.getName());

        // <mapAreaName /> mapArea name, not extId
        cursor =
                Queries.getHierarchyByExtId(navigateActivity.getContentResolver(), sector.getParent());
        if( !cursor.moveToFirst()) {
            cursor.close();
            return;
        }
        LocationHierarchy mapArea = Converter.toHierarchy(cursor, true);
        formPayload.put(ProjectFormFields.Locations.MAP_AREA_NAME, mapArea.getName());


        // <localityName /> locality name, not extId
        cursor =
                Queries.getHierarchyByExtId(navigateActivity.getContentResolver(), mapArea.getParent());
        if( !cursor.moveToFirst()) {
            cursor.close();
            return;
        }
        LocationHierarchy locality = Converter.toHierarchy(cursor, true);
        formPayload.put(ProjectFormFields.Locations.LOCALITY_NAME, locality.getName());
    }

    private static void addNewIndividualPayload(
            Map<String, String> formPayload, NavigateActivity navigateActivity) {

        FieldWorker fieldWorker = (FieldWorker) navigateActivity.getIntent()
                .getExtras().get(FieldWorkerLoginFragment.FIELD_WORKER_EXTRA);

        String generatedIdPrefix = fieldWorker.getCollectedIdPrefix();

        Cursor cursor = getIndividualsExtIdsByPrefix(
                navigateActivity.getContentResolver(), generatedIdPrefix);
        int nextSequence = 0;
        if (cursor.moveToLast()) {
            String lastExtId = cursor
                    .getString(cursor
                            .getColumnIndex(OpenHDS.Individuals.COLUMN_INDIVIDUAL_EXTID));
            int prefixLength = generatedIdPrefix.length();
            int checkDigitLength = 1;
            String lastSequenceNumber = lastExtId.substring(prefixLength + 1,
                    lastExtId.length() - checkDigitLength);
            nextSequence = Integer.parseInt(lastSequenceNumber) + 1;
        }
        cursor.close();

        // TODO: break out 5-digit number format, don't use string literal here.
        String generatedIdSeqNum = String.format("%05d", nextSequence);

        Character generatedIdCheck = LuhnValidator
                .generateCheckCharacter(generatedIdSeqNum + generatedIdSeqNum);

        String individualExtId = generatedIdPrefix + generatedIdSeqNum
                + generatedIdCheck.toString();

        formPayload.put(ProjectFormFields.Individuals.INDIVIDUAL_EXTID,
                individualExtId);

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

            Cursor cursor = Queries.getIndividualByExtId(
                    navigateActivity.getContentResolver(), individualExtId);
            cursor.moveToFirst();

            Individual individual = Converter.toIndividual(cursor, true);

            formPayload.putAll(IndividualAdapter
                    .individualToFormFields(individual));

            //TODO: Change the birthday to either a simple date object or find a better way to handle this functionality.
            String truncatedDate = formPayload.get(ProjectFormFields.Individuals.DATE_OF_BIRTH).substring(0, 10);
            formPayload.remove(ProjectFormFields.Individuals.DATE_OF_BIRTH);
            formPayload.put(ProjectFormFields.Individuals.DATE_OF_BIRTH, truncatedDate);

            cursor = Queries.getMembershipByHouseholdAndIndividualExtId(
                    navigateActivity.getContentResolver(), householdExtId,
                    individualExtId);
            cursor.moveToFirst();

            Membership membership = Converter.toMembership(cursor, true);
            formPayload.put(ProjectFormFields.Individuals.RELATIONSHIP_TO_HEAD,
                    membership.getRelationshipToHead());

        }
    }

}
