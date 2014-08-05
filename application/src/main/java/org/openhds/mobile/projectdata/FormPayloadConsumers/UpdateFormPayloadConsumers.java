package org.openhds.mobile.projectdata.FormPayloadConsumers;

import java.util.Map;

import org.openhds.mobile.activity.NavigateActivity;
import org.openhds.mobile.database.VisitAdapter;
import org.openhds.mobile.model.Visit;
import org.openhds.mobile.projectdata.ProjectActivityBuilder;

public class UpdateFormPayloadConsumers {

	public static class StartAVisit implements FormPayloadConsumer {

		@Override
		public boolean consumeFormPayload(Map<String, String> formPayload,
				NavigateActivity navigateActivity) {
			// TODO Auto-generated method stub

            Visit visit = VisitAdapter.create(formPayload);

			navigateActivity.startVisit(visit);

			VisitAdapter.insert(navigateActivity.getContentResolver(),visit);

			return false;

		}

		@Override
		public void postFillFormPayload(Map<String, String> formPayload) {
			// TODO Auto-generated method stub

		}

	}

}
