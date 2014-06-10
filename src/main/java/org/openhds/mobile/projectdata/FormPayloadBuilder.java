package org.openhds.mobile.projectdata;

import java.util.Map;

import org.openhds.mobile.activity.Skeletor;

public interface FormPayloadBuilder {
	public void buildFormPayload(Map<String, String> formPayload, Skeletor skeletor);
}

