package org.openhds.mobile.activity;

import java.util.List;
import java.util.Map;

import org.openhds.mobile.repository.DataWrapper;
import org.openhds.mobile.forms.FormBehaviour;

public interface HierarchyNavigator {

	Map<String, String> getLevelLabels();

	List<String> getLevelSequence();

	void jumpUp(String state);

	void stepDown(DataWrapper qr);
	
	void launchForm(FormBehaviour form);
}
