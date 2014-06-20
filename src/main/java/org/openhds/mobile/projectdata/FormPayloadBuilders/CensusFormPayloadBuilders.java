package org.openhds.mobile.projectdata.FormPayloadBuilders;

import static org.openhds.mobile.database.queries.Queries.getIndividualsExtIdsByPrefix;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.activity.NavigateActivity;
import org.openhds.mobile.database.IndividualAdapter;
import org.openhds.mobile.database.queries.Converter;
import org.openhds.mobile.database.queries.Queries;
import org.openhds.mobile.database.queries.QueryResult;
import org.openhds.mobile.fragment.FieldWorkerLoginFragment;
import org.openhds.mobile.model.FieldWorker;
import org.openhds.mobile.model.Individual;
import org.openhds.mobile.model.Membership;
import org.openhds.mobile.projectdata.ProjectActivityBuilder;
import org.openhds.mobile.projectdata.ProjectFormFields;
import org.openhds.mobile.projectdata.ProjectResources;
import org.openhds.mobile.projectdata.ProjectActivityBuilder.CensusActivityBuilder;
import org.openhds.mobile.projectdata.ProjectFormFields.General;
import org.openhds.mobile.projectdata.ProjectFormFields.Individuals;
import org.openhds.mobile.projectdata.ProjectResources.Relationship;
import org.openhds.mobile.utilities.LuhnValidator;

import android.database.Cursor;

public class CensusFormPayloadBuilders {

	/**
	 * 
	 * Helper methods for FormPayloadBuilders
	 * 
	 */
	private static void addMinimalFormPayload(Map<String, String> formPayload,
			NavigateActivity navigateActivity) {

		List<String> stateSequence = navigateActivity.getStateSequence();
		Map<String, QueryResult> hierarchyPath = navigateActivity.getHierarchyPath();

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
				ProjectFormFields.General.COLLECTED_DATE_TIME,
				new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(
						Calendar.getInstance().getTime()).toString());

	}

	private static void addNewIndividualPayload(
			Map<String, String> formPayload, NavigateActivity navigateActivity) {

		FieldWorker fieldWorker = (FieldWorker) navigateActivity.getIntent()
				.getExtras().get(FieldWorkerLoginFragment.FIELD_WORKER_EXTRA);

		String generatedIdPrefix = fieldWorker.getCollectedIdPrefix();

		Cursor cursor = getIndividualsExtIdsByPrefix(
				navigateActivity.getContentResolver(), generatedIdPrefix);
		int nextSequence = 0;
		if (cursor.moveToLast()) {
			String lastExtId = cursor
					.getString(cursor
							.getColumnIndex(OpenHDS.Individuals.COLUMN_INDIVIDUAL_EXTID));
			int prefixLength = generatedIdPrefix.length();
			int checkDigitLength = 1;
			String lastSequenceNumber = lastExtId.substring(prefixLength + 1,
					lastExtId.length() - checkDigitLength);
			nextSequence = Integer.parseInt(lastSequenceNumber) + 1;
		}
		cursor.close();

		// TODO: break out 5-digit number format, don't use string literal here.
		String generatedIdSeqNum = String.format("%05d", nextSequence);

		Character generatedIdCheck = LuhnValidator
				.generateCheckCharacter(generatedIdSeqNum + generatedIdSeqNum);

		String individualExtId = generatedIdPrefix + generatedIdSeqNum
				+ generatedIdCheck.toString();

		formPayload.put(ProjectFormFields.Individuals.INDIVIDUAL_EXTID,
				individualExtId);

	}

	/**
	 * 
	 * Form Payload Builders
	 *
	 */

	public static class AddMemberOfHousehold implements FormPayloadBuilder {

		@Override
		public void buildFormPayload(Map<String, String> formPayload,
				NavigateActivity navigateActivity) {

			addMinimalFormPayload(formPayload, navigateActivity);
			addNewIndividualPayload(formPayload, navigateActivity);

		}

	}

	public static class AddHeadOfHousehold implements FormPayloadBuilder {

		@Override
		public void buildFormPayload(Map<String, String> formPayload,
				NavigateActivity navigateActivity) {

			addMinimalFormPayload(formPayload, navigateActivity);
			addNewIndividualPayload(formPayload, navigateActivity);

			formPayload.put(ProjectFormFields.Individuals.RELATIONSHIP_TO_HEAD,
					ProjectResources.Relationship.RELATION_TO_HOH_TYPE_HEAD);

		}

	}

	public static class EditIndividual implements FormPayloadBuilder {

		@Override
		public void buildFormPayload(Map<String, String> formPayload,
				NavigateActivity navigateActivity) {

			addMinimalFormPayload(formPayload, navigateActivity);

			// build complete individual form
			Map<String, QueryResult> hierarchyPath = navigateActivity
					.getHierarchyPath();

			String individualExtId = hierarchyPath
					.get(ProjectActivityBuilder.CensusActivityBuilder.INDIVIDUAL_STATE)
					.getExtId();
			String householdExtId = hierarchyPath
					.get(ProjectActivityBuilder.CensusActivityBuilder.HOUSEHOLD_STATE)
					.getExtId();

			Cursor cursor = Queries.getIndividualByExtId(
					navigateActivity.getContentResolver(), individualExtId);
			cursor.moveToFirst();

			Individual individual = Converter.toIndividual(cursor, true);

			formPayload.putAll(IndividualAdapter
					.individualToFormFields(individual));
			
			//TODO: Change the birthday to either a simple date object or find a better way to handle this functionality.
			String truncatedDate = formPayload.get(ProjectFormFields.Individuals.DATE_OF_BIRTH).substring(0, 10);
			formPayload.remove(ProjectFormFields.Individuals.DATE_OF_BIRTH);
			formPayload.put(ProjectFormFields.Individuals.DATE_OF_BIRTH, truncatedDate);
			
			cursor = Queries.getMembershipByHouseholdAndIndividualExtId(
					navigateActivity.getContentResolver(), householdExtId,
					individualExtId);
			cursor.moveToFirst();

			Membership membership = Converter.toMembership(cursor, true);
			formPayload.put(ProjectFormFields.Individuals.RELATIONSHIP_TO_HEAD,
					membership.getRelationshipToHead());

		}
	}

}
