package org.openhds.mobile.projectdata.FormPayloadConsumers;

import android.content.ContentResolver;
import org.openhds.mobile.activity.NavigateActivity;
import org.openhds.mobile.model.Individual;
import org.openhds.mobile.model.Visit;
import org.openhds.mobile.projectdata.FormAdapters.VisitFormAdapter;
import org.openhds.mobile.projectdata.ProjectFormFields;
import org.openhds.mobile.projectdata.ProjectResources;
import org.openhds.mobile.repository.GatewayRegistry;
import org.openhds.mobile.repository.gateway.IndividualGateway;
import org.openhds.mobile.repository.gateway.VisitGateway;

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

            deceasedIndividual.setEndType(ProjectResources.Individual.END_TYPE_DEATH);

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
            String individualExtId = formPayload.get(ProjectFormFields.OutMigrations.OUT_MIGRATION_INDIVIDUAL_EXTID);
            IndividualGateway individualGateway = GatewayRegistry.getIndividualGateway();
            Individual individual = individualGateway.getFirst(navigateActivity.getContentResolver(),
                    individualGateway.findById(individualExtId));
            if (null == individual) {
                return new ConsumerResults(false, null, null);
            }

            individual.setEndType(ProjectResources.Individual.RESIDENCY_END_TYPE_OMG);
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
            // update the individual's residency
            String locationExtId = formPayload.get(ProjectFormFields.Locations.LOCATION_EXTID);
            String individualExtId = formPayload.get(ProjectFormFields.Individuals.INDIVIDUAL_EXTID);

            IndividualGateway individualGateway = GatewayRegistry.getIndividualGateway();
            Individual individual = individualGateway.getFirst(navigateActivity.getContentResolver(),
                    individualGateway.findById(individualExtId));
            if (null == individual) {
                return new ConsumerResults(false, null, null);
            }

            individual.setCurrentResidence(locationExtId);
            individual.setEndType(ProjectResources.Individual.RESIDENCY_END_TYPE_NA);
            individualGateway.insertOrUpdate(navigateActivity.getContentResolver(), individual);
            return new ConsumerResults(false, null, null);
        }

        @Override
        public void postFillFormPayload(Map<String, String> formPayload) {

        }
    }
}