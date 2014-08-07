package org.openhds.mobile.projectdata.FormPayloadBuilders;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.activity.NavigateActivity;
import org.openhds.mobile.database.queries.QueryResult;
import org.openhds.mobile.fragment.FieldWorkerLoginFragment;
import org.openhds.mobile.model.FieldWorker;
import org.openhds.mobile.projectdata.ProjectFormFields;
import org.openhds.mobile.projectdata.ProjectResources;

public class PayloadTools {

    public static void flagForReview(Map<String, String> formPayload, boolean shouldReview) {
        if (shouldReview) {
            formPayload.put(ProjectFormFields.General.NEEDS_REVIEW, ProjectResources.General.FORM_NEEDS_REVIEW);
        } else {
            formPayload.put(ProjectFormFields.General.NEEDS_REVIEW, ProjectResources.General.FORM_NO_REVIEW_NEEDED);
        }
    }

	public static void addMinimalFormPayload(Map<String, String> formPayload,
			NavigateActivity navigateActivity) {

		List<String> stateSequence = navigateActivity.getStateSequence();
		Map<String, QueryResult> hierarchyPath = navigateActivity
				.getHierarchyPath();

		// Add all the extIds from the HierarchyPath
		for (String state : stateSequence) {
			if (null != hierarchyPath.get(state)) {
				String fieldName = ProjectFormFields.General
						.getExtIdFieldNameFromState(state);
				formPayload.put(fieldName, hierarchyPath.get(state).getExtId());
			}
		}

		// add the FieldWorker's extId
		FieldWorker fieldWorker = (FieldWorker) navigateActivity.getIntent()
				.getExtras().get(FieldWorkerLoginFragment.FIELD_WORKER_EXTRA);
		formPayload.put(
				ProjectFormFields.General.COLLECTED_BY_FIELD_WORKER_EXTID,
				fieldWorker.getExtId());

		// add collected DateTime
		formPayload.put(
				ProjectFormFields.General.COLLECTION_DATE_TIME,
				new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(
						Calendar.getInstance().getTime()).toString());

	}

}
