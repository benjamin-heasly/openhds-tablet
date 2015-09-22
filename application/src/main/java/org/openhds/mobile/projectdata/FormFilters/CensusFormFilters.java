package org.openhds.mobile.projectdata.FormFilters;

import org.openhds.mobile.activity.NavigateActivity;
import org.openhds.mobile.projectdata.ProjectActivityBuilder;
import org.openhds.mobile.repository.DataWrapper;
import org.openhds.mobile.repository.GatewayRegistry;
import org.openhds.mobile.repository.gateway.SocialGroupGateway;

import java.util.Map;

// These are not necessarily 1 to 1 with the form types, 
// but instead filter when a form's behaviour may or may not be appropriate
// i.e. after you 'add a head of household' you no longer have to display the
// button (aka it's amIValid() == false).
public class CensusFormFilters {

	private static boolean hasHeadOfHousehold(
			NavigateActivity navigateActivity,
			Map<String, DataWrapper> hierarchyPath) {

		String locationExtId = hierarchyPath.get(
				ProjectActivityBuilder.BiokoHierarchy.HOUSEHOLD_STATE)
				.getUuid();

		SocialGroupGateway socialGroupGateway = GatewayRegistry.getSocialGroupGateway();
		return null != socialGroupGateway.getFirst(navigateActivity.getContentResolver(),socialGroupGateway.findByExtId(locationExtId));
	}

	public static class AddLocation implements FormFilter {

		@Override
		public boolean amIValid(NavigateActivity navigateActivity) {

			return true;
		}
	}

	public static class EvaluateLocation implements FormFilter {

		@Override
		public boolean amIValid(NavigateActivity navigateActivity) {
			return true;
		}
	}

	public static class AddHeadOfHousehold implements FormFilter {

		@Override
		public boolean amIValid(NavigateActivity navigateActivity) {

			return !CensusFormFilters.hasHeadOfHousehold(navigateActivity,
					navigateActivity.getHierarchyPath());
		}
	}

	public static class AddMemberOfHousehold implements FormFilter {

		@Override
		public boolean amIValid(NavigateActivity navigateActivity) {

			return CensusFormFilters.hasHeadOfHousehold(navigateActivity,
					navigateActivity.getHierarchyPath());
		}
	}

	public static class EditIndividual implements FormFilter {

		@Override
		public boolean amIValid(NavigateActivity navigateActivity) {

			return true;
		}
	}
}
