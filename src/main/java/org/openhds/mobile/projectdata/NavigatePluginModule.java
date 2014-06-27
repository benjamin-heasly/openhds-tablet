package org.openhds.mobile.projectdata;

import java.util.List;
import java.util.Map;

import org.openhds.mobile.fragment.detailfragments.DetailFragment;
import org.openhds.mobile.model.FormBehaviour;
import org.openhds.mobile.projectdata.QueryHelpers.QueryHelper;

public interface NavigatePluginModule {

	public Map<String, Integer> getStateLabels();

	public List<String> getStateSequence();

	public QueryHelper getQueryHelper();

	public Map<String, List<FormBehaviour>> getFormsforstates();

	public Map<String, DetailFragment> getDetailFragsForStates();
	
}
