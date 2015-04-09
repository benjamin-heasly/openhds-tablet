package org.openhds.mobile.projectdata.FormPayloadBuilders;

import android.content.ContentResolver;
import org.openhds.mobile.activity.NavigateActivity;
import org.openhds.mobile.model.core.Individual;
import org.openhds.mobile.model.core.SocialGroup;
import org.openhds.mobile.projectdata.ProjectActivityBuilder;
import org.openhds.mobile.projectdata.ProjectFormFields;
import org.openhds.mobile.repository.DataWrapper;
import org.openhds.mobile.repository.GatewayRegistry;
import org.openhds.mobile.repository.gateway.IndividualGateway;
import org.openhds.mobile.repository.gateway.SocialGroupGateway;
import org.openhds.mobile.utilities.IdHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;

public class UpdateFormPayloadBuilders {

    /**
     *
     * Helper methods for FormPayloadBuilders
     *
     */

    public static class StartAVisit implements FormPayloadBuilder {

        @Override
        public void buildFormPayload(Map<String, String> formPayload,
                                     NavigateActivity navigateActivity) {

            PayloadTools.addMinimalFormPayload(formPayload, navigateActivity);
            PayloadTools.flagForReview(formPayload, false);

            String visitDate = new SimpleDateFormat("yyyy-MM-dd").format(
                    Calendar.getInstance().getTime()).toString();
            String locationExtId = navigateActivity.getHierarchyPath()
                    .get(ProjectActivityBuilder.BiokoHierarchy.HOUSEHOLD_STATE).getExtId();
            String locationUuid= navigateActivity.getHierarchyPath()
                    .get(ProjectActivityBuilder.BiokoHierarchy.HOUSEHOLD_STATE).getUuid();
            String visitExtId = visitDate + "_" + locationExtId;


            formPayload.put(ProjectFormFields.Visits.VISIT_DATE, visitDate);
            formPayload.put(ProjectFormFields.Visits.LOCATION_UUID,
                    locationUuid);
            formPayload.put(ProjectFormFields.Visits.VISIT_EXTID, visitExtId);
            formPayload.put(ProjectFormFields.Visits.VISIT_UUID, IdHelper.generateEntityUuid());
            formPayload.put(ProjectFormFields.General.ENTITY_EXTID, locationExtId);
            formPayload.put(ProjectFormFields.General.ENTITY_UUID, locationUuid);

        }
    }


    //TODO: Individuals are never put into the Payload???
    //TODO: Missing = individualExtId, individualUuid, entityExtId, entityUuid
    public static class RegisterInMigration implements FormPayloadBuilder {

        @Override
        public void buildFormPayload(Map<String, String> formPayload, NavigateActivity navigateActivity) {

            PayloadTools.addMinimalFormPayload(formPayload, navigateActivity);
            PayloadTools.flagForReview(formPayload, false);

            formPayload.put(ProjectFormFields.Visits.VISIT_EXTID, navigateActivity.getCurrentVisit().getExtId());
            formPayload.put(ProjectFormFields.Visits.VISIT_UUID, navigateActivity.getCurrentVisit().getUuid());

            String locationExtId = navigateActivity.getHierarchyPath().get(ProjectActivityBuilder.BiokoHierarchy.HOUSEHOLD_STATE).getExtId();
            String locationUuid = navigateActivity.getHierarchyPath().get(ProjectActivityBuilder.BiokoHierarchy.HOUSEHOLD_STATE).getUuid();
            formPayload.put(ProjectFormFields.Locations.LOCATION_EXTID, locationExtId);
            formPayload.put(ProjectFormFields.Locations.LOCATION_UUID, locationUuid);

            formPayload.put(ProjectFormFields.InMigrations.IN_MIGRATION_TYPE, ProjectFormFields.InMigrations.IN_MIGRATION_INTERNAL);

            String migrationDate = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime()).toString();
            formPayload.put(ProjectFormFields.InMigrations.IN_MIGRATION_DATE, migrationDate);
        }
    }

    public static class RegisterOutMigration implements FormPayloadBuilder {

        @Override
        public void buildFormPayload(Map<String, String> formPayload,
                                     NavigateActivity navigateActivity) {

            PayloadTools.addMinimalFormPayload(formPayload, navigateActivity);
            PayloadTools.flagForReview(formPayload, false);

            String outMigrationDate = new SimpleDateFormat("yyyy-MM-dd").format(
                    Calendar.getInstance().getTime()).toString();

            String individualExtId = navigateActivity.getHierarchyPath()
                    .get(ProjectActivityBuilder.BiokoHierarchy.INDIVIDUAL_STATE).getExtId();
            String individualUuid = navigateActivity.getHierarchyPath()
                    .get(ProjectActivityBuilder.BiokoHierarchy.INDIVIDUAL_STATE).getUuid();

            formPayload.put(ProjectFormFields.OutMigrations.OUT_MIGRATION_DATE, outMigrationDate);

            formPayload.put(ProjectFormFields.Individuals.INDIVIDUAL_EXTID, individualExtId);
            formPayload.put(ProjectFormFields.Individuals.INDIVIDUAL_UUID, individualUuid);

            formPayload.put(ProjectFormFields.General.ENTITY_EXTID, individualExtId);
            formPayload.put(ProjectFormFields.General.ENTITY_UUID, individualUuid);

            formPayload.put(ProjectFormFields.Visits.VISIT_EXTID, navigateActivity.getCurrentVisit().getExtId());
            formPayload.put(ProjectFormFields.Visits.VISIT_UUID, navigateActivity.getCurrentVisit().getUuid());


        }
    }

    public static class RegisterDeath implements FormPayloadBuilder {

        @Override
        public void buildFormPayload(Map<String, String> formPayload,
                                     NavigateActivity navigateActivity) {

            PayloadTools.addMinimalFormPayload(formPayload, navigateActivity);
            PayloadTools.flagForReview(formPayload, true);

            formPayload.put(ProjectFormFields.Visits.VISIT_EXTID, navigateActivity.getCurrentVisit().getExtId());

        }
    }

    public static class RecordPregnancyObservation implements FormPayloadBuilder {

        @Override
        public void buildFormPayload(Map<String, String> formPayload,
                                     NavigateActivity navigateActivity) {

            PayloadTools.addMinimalFormPayload(formPayload, navigateActivity);
            PayloadTools.flagForReview(formPayload, false);

            String observationDate = new SimpleDateFormat("yyyy-MM-dd").format(
                    Calendar.getInstance().getTime()).toString();

            String individualExtId = navigateActivity.getHierarchyPath()
                    .get(ProjectActivityBuilder.BiokoHierarchy.INDIVIDUAL_STATE).getExtId();
            String individualUuid = navigateActivity.getHierarchyPath()
                    .get(ProjectActivityBuilder.BiokoHierarchy.INDIVIDUAL_STATE).getUuid();

            formPayload.put(ProjectFormFields.Individuals.INDIVIDUAL_UUID, individualUuid);
            formPayload.put(ProjectFormFields.Individuals.INDIVIDUAL_EXTID, individualExtId);

            formPayload.put(ProjectFormFields.General.ENTITY_UUID, individualUuid);
            formPayload.put(ProjectFormFields.General.ENTITY_EXTID, individualExtId);

            formPayload.put(ProjectFormFields.Visits.VISIT_EXTID, navigateActivity.getCurrentVisit().getExtId());
            formPayload.put(ProjectFormFields.Visits.VISIT_UUID, navigateActivity.getCurrentVisit().getUuid());

            formPayload.put(ProjectFormFields.PregnancyObservation.PREGNANCY_OBSERVATION_RECORDED_DATE, observationDate);

        }
    }

    public static class RecordPregnancyOutcome implements FormPayloadBuilder {

        @Override
        public void buildFormPayload(Map<String, String> formPayload,
                                     NavigateActivity navigateActivity) {

            PayloadTools.addMinimalFormPayload(formPayload, navigateActivity);
            PayloadTools.flagForReview(formPayload, false);

            SocialGroupGateway socialGroupGateway = new SocialGroupGateway();
            SocialGroup socialGroup = socialGroupGateway.getFirst(navigateActivity.getContentResolver(),
                    socialGroupGateway.findByLocationUuid(navigateActivity.getHierarchyPath()
                            .get(ProjectActivityBuilder.BiokoHierarchy.HOUSEHOLD_STATE).getUuid()));

            String motherExtId = navigateActivity.getHierarchyPath()
                    .get(ProjectActivityBuilder.BiokoHierarchy.INDIVIDUAL_STATE).getExtId();
            String motherUuid = navigateActivity.getHierarchyPath()
                    .get(ProjectActivityBuilder.BiokoHierarchy.INDIVIDUAL_STATE).getUuid();

            formPayload.put(ProjectFormFields.PregnancyOutcome.MOTHER_UUID, motherUuid);
            formPayload.put(ProjectFormFields.General.ENTITY_UUID, motherUuid);
            formPayload.put(ProjectFormFields.General.ENTITY_EXTID, motherExtId);
            formPayload.put(ProjectFormFields.Visits.VISIT_UUID, navigateActivity.getCurrentVisit().getUuid());
            formPayload.put(ProjectFormFields.PregnancyOutcome.SOCIALGROUP_UUID, socialGroup.getUuid());

        }
    }
}
