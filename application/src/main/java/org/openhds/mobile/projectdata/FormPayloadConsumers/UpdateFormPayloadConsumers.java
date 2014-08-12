package org.openhds.mobile.projectdata.FormPayloadConsumers;

import android.content.ContentResolver;
import org.openhds.mobile.activity.NavigateActivity;
import org.openhds.mobile.model.Visit;
import org.openhds.mobile.projectdata.FormAdapters.VisitFormAdapter;
import org.openhds.mobile.repository.GatewayRegistry;
import org.openhds.mobile.repository.gateway.VisitGateway;

import java.util.Map;

public class UpdateFormPayloadConsumers {

    public static class StartAVisit implements FormPayloadConsumer {

        @Override
        public boolean consumeFormPayload(Map<String, String> formPayload, NavigateActivity navigateActivity) {

            Visit visit = VisitFormAdapter.fromForm(formPayload);

            VisitGateway visitGateway = GatewayRegistry.getVisitGateway();
            ContentResolver contentResolver = navigateActivity.getContentResolver();
            visitGateway.insertOrUpdate(contentResolver, visit);

            navigateActivity.startVisit(visit);

            return false;
        }

        @Override
        public void postFillFormPayload(Map<String, String> formPayload) {
            // TODO Auto-generated method stub

        }
    }
}
