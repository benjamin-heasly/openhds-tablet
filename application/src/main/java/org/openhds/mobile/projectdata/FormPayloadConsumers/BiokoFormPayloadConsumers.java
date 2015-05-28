package org.openhds.mobile.projectdata.FormPayloadConsumers;

import org.openhds.mobile.activity.NavigateActivity;
import org.openhds.mobile.model.core.Location;
import org.openhds.mobile.repository.GatewayRegistry;
import org.openhds.mobile.repository.gateway.LocationGateway;

import java.util.Map;

public class BiokoFormPayloadConsumers {

    public static class DistributeBednets implements FormPayloadConsumer {

        @Override
        public ConsumerResults consumeFormPayload(Map<String, String> formPayload,
                                                  NavigateActivity navigateActivity) {

            LocationGateway locationGateway = GatewayRegistry.getLocationGateway();

            Location location = locationGateway.getFirst(navigateActivity.getContentResolver(),
                    locationGateway.findById(navigateActivity.getCurrentSelection().getUuid()));

            location.setHasReceivedBedNets("true");

            locationGateway.insertOrUpdate(navigateActivity.getContentResolver(), location);

            return new ConsumerResults(false, null, null);
        }

        @Override
        public void postFillFormPayload(Map<String, String> formPayload) {
            // TODO Auto-generated method stub

        }

    }

    public static class SprayHousehold implements FormPayloadConsumer {

        @Override
        public ConsumerResults consumeFormPayload(Map<String, String> formPayload, NavigateActivity navigateActivity) {
            return new ConsumerResults(false, null, null);
        }

        @Override
        public void postFillFormPayload(Map<String, String> formPayload) {
            // TODO Auto-generated method stub
        }
    }

}
