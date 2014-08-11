package org.openhds.mobile.projectdata.FormPayloadConsumers;

import org.openhds.mobile.activity.NavigateActivity;

import java.util.Map;

public class BiokoFormPayloadConsumers {

    public static class DistributeBednets implements FormPayloadConsumer {

        @Override
        public boolean consumeFormPayload(Map<String, String> formPayload,
                                          NavigateActivity navigateActivity) {
            return true;
        }

        @Override
        public void postFillFormPayload(Map<String, String> formPayload) {
            // TODO Auto-generated method stub

        }

    }

}
