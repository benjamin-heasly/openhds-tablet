package org.openhds.mobile.projectdata;

import static org.openhds.mobile.database.queries.Queries.getIndividualsExtIdsByPrefix;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.activity.Skeletor;
import org.openhds.mobile.database.IndividualAdapter;
import org.openhds.mobile.database.queries.Converter;
import org.openhds.mobile.database.queries.Queries;
import org.openhds.mobile.database.queries.QueryResult;
import org.openhds.mobile.fragment.FieldWorkerLoginFragment;
import org.openhds.mobile.model.FieldWorker;
import org.openhds.mobile.model.Individual;
import org.openhds.mobile.model.Membership;
import org.openhds.mobile.utilities.LuhnValidator;

import android.database.Cursor;

public class CensusFormPayloadBuilders {

	/**
	 * 
	 * Helper methods for FormPayloadBuilders
	 * 
	 */
	private static void addMinimalFormPayload(Map<String, String> formPayload,
			Skeletor skeletor) {

		List<String> stateSequence = skeletor.getStateSequence();
		Map<String, QueryResult> hierarchyPath = skeletor.getHierarchyPath();

		// Add all the extIds from the HierarchyPath
		for (String state : stateSequence) {
			if (null != hierarchyPath.get(state)) {
				String fieldName = ProjectFormFields.General
						.getExtIdFieldNameFromState(state);
				formPayload.put(fieldName, hierarchyPath.get(state).getExtId());
			}
		}

		// add the FieldWorker's extId
		FieldWorker fieldWorker = (FieldWorker) skeletor.getIntent()
				.getExtras().get(FieldWorkerLoginFragment.FIELD_WORKER_EXTRA);
		formPayload.put(
				ProjectFormFields.General.COLLECTED_BY_FIELD_WORKER_EXTID,
				fieldWorker.getExtId());

		// add collected DateTime
		formPayload.put(ProjectFormFields.General.COLLECTED_DATE_TIME, Calendar
				.getInstance().getTime().toString());

	}

	private static void addNewIndividualPayload(
			Map<String, String> formPayload, Skeletor skeletor) {

		FieldWorker fieldWorker = (FieldWorker) skeletor.getIntent()
				.getExtras().get(FieldWorkerLoginFragment.FIELD_WORKER_EXTRA);

		String generatedIdPrefix = fieldWorker.getCollectedIdPrefix();

		Cursor cursor = getIndividualsExtIdsByPrefix(
				skeletor.getContentResolver(), generatedIdPrefix);
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
				Skeletor skeletor) {

			addMinimalFormPayload(formPayload, skeletor);
			addNewIndividualPayload(formPayload, skeletor);

		}

	}

	public static class AddHeadOfHousehold implements FormPayloadBuilder {

		@Override
		public void buildFormPayload(Map<String, String> formPayload,
				Skeletor skeletor) {

			addMinimalFormPayload(formPayload, skeletor);
			addNewIndividualPayload(formPayload, skeletor);

			formPayload.put(ProjectFormFields.Individuals.RELATIONSHIP_TO_HEAD,
					ProjectResources.Relationship.RELATION_TO_HOH_TYPE_HEAD);

		}

	}

	public static class EditIndividual implements FormPayloadBuilder {

		@Override
		public void buildFormPayload(Map<String, String> formPayload,
				Skeletor skeletor) {
			
			addMinimalFormPayload(formPayload, skeletor);
			
			// build complete individual form
			Map<String, QueryResult> hierarchyPath = skeletor
					.getHierarchyPath();

			String individualExtId = hierarchyPath
					.get(ProjectActivityBuilder.CensusActivityBuilder.INDIVIDUAL_STATE)
					.getExtId();
			String householdExtId = hierarchyPath
					.get(ProjectActivityBuilder.CensusActivityBuilder.HOUSEHOLD_STATE)
					.getExtId();

			Cursor cursor = Queries.getIndividualByExtId(
					skeletor.getContentResolver(), individualExtId);
			cursor.moveToFirst();

			Individual individual = Converter.toIndividual(cursor, true);

			formPayload.putAll(IndividualAdapter
					.individualToFormFields(individual));

			cursor = Queries.getMembershipByHouseholdAndIndividualExtId(
					skeletor.getContentResolver(), householdExtId,
					individualExtId);
			cursor.moveToFirst();

			Membership membership = Converter.toMembership(cursor, true);
			formPayload.put(ProjectFormFields.Individuals.RELATIONSHIP_TO_HEAD,
					membership.getRelationshipToHead());
			formPayload.put(ProjectFormFields.Individuals.MEMBER_STATUS,
					membership.getStatus());

		}
	}

}
