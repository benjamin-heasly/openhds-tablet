package org.openhds.mobile.projectdata;

import java.util.Map;

import org.openhds.mobile.activity.Skeletor;

public interface FormPayloadConsumer {
	public void consumeFormPayload(Map<String, String> formPayload,
			Skeletor skeletor);
}
