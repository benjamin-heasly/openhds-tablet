package org.openhds.mobile.projectdata.FormFilters;

import org.openhds.mobile.activity.NavigateActivity;
import org.openhds.mobile.model.Individual;
import org.openhds.mobile.projectdata.ProjectActivityBuilder;
import org.openhds.mobile.projectdata.ProjectResources;
import org.openhds.mobile.repository.DataWrapper;
import org.openhds.mobile.repository.GatewayRegistry;
import org.openhds.mobile.repository.gateway.IndividualGateway;

import java.util.Map;

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

    public static class RegisterOutMigration implements FormFilter {

        @Override
        public boolean amIValid(NavigateActivity navigateActivity) {

            if (null != navigateActivity.getCurrentVisit()) {
                return true;
            }

            return false;
        }
    }

    public static class RegisterDeath implements FormFilter {

        @Override
        public boolean amIValid(NavigateActivity navigateActivity) {

            boolean deceased = UpdateFormFilters.isIndividualDeceased(navigateActivity, navigateActivity.getHierarchyPath());

            if (null != navigateActivity.getCurrentVisit() && !deceased) {
                return true;
            }

            return false;
        }
    }

    private static boolean isIndividualDeceased(
            NavigateActivity navigateActivity,
            Map<String, DataWrapper> hierarchyPath) {

        String individualExtId = hierarchyPath.get(
                ProjectActivityBuilder.UpdateActivityModule.INDIVIDUAL_STATE)
                .getExtId();

        IndividualGateway individualGateway = GatewayRegistry.getIndividualGateway();
        Individual selectedIndividual = individualGateway.getFirst(navigateActivity.getContentResolver(),
                individualGateway.findByExtIdPrefixDescending(individualExtId));

        return selectedIndividual.getEndType().equals(ProjectResources.Individual.END_TYPE_DEATH);
    }
}
