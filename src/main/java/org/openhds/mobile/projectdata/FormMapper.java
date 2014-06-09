package org.openhds.mobile.projectdata;

import java.util.Map;

import org.openhds.mobile.activity.Skeletor;

public interface FormMapper {
	public void addFormFieldMappings(Map<String, String> formFieldMap, Skeletor skeletor);
}
