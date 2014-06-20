package org.openhds.mobile.projectdata.FormPayloadConsumers;

import java.util.Map;

import org.openhds.mobile.activity.NavigateActivity;

public interface FormPayloadConsumer {
	public void consumeFormPayload(Map<String, String> formPayload,
			NavigateActivity navigateActivity);
}
