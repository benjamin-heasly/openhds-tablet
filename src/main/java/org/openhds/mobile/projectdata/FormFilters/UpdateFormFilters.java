package org.openhds.mobile.projectdata.FormFilters;

import org.openhds.mobile.activity.NavigateActivity;
import org.openhds.mobile.projectdata.ProjectActivityBuilder;

// These are not necessarily 1 to 1 with the form types, 
// but instead filter when a form's behaviour may or may not be appropriate
// i.e. after you 'add a head of household' you no longer have to display the
// button (aka it's amIValid() == false).
public class UpdateFormFilters {

	public static class StartAVisit implements FormFilter {

		@Override
		public boolean amIValid(NavigateActivity navigateActivity) {

			if (null == navigateActivity.getCurrentVisit()) {
				return true;
			}

			return false;
		}
	}

}
