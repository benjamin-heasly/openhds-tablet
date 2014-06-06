package org.openhds.mobile.activity;

import static org.openhds.mobile.database.queries.Queries.getIndividualsExtIdsByPrefix;
import static org.openhds.mobile.utilities.ConfigUtils.getResourceString;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.R;
import org.openhds.mobile.database.IndividualAdapter;
import org.openhds.mobile.database.LocationAdapter;
import org.openhds.mobile.database.MembershipAdapter;
import org.openhds.mobile.database.RelationshipAdapter;
import org.openhds.mobile.database.SocialGroupAdapter;
import org.openhds.mobile.database.queries.Converter;
import org.openhds.mobile.database.queries.Queries;
import org.openhds.mobile.database.queries.QueryResult;
import org.openhds.mobile.fragment.FieldWorkerLoginFragment;
import org.openhds.mobile.fragment.HierarchyFormFragment;
import org.openhds.mobile.fragment.HierarchySelectionFragment;
import org.openhds.mobile.fragment.HierarchyValueFragment;
import org.openhds.mobile.model.FieldWorker;
import org.openhds.mobile.model.FormBehaviour;
import org.openhds.mobile.model.FormHelper;
import org.openhds.mobile.model.FormInstance;
import org.openhds.mobile.model.Individual;
import org.openhds.mobile.model.Location;
import org.openhds.mobile.model.Membership;
import org.openhds.mobile.model.Relationship;
import org.openhds.mobile.model.SocialGroup;
import org.openhds.mobile.model.StateMachine;
import org.openhds.mobile.model.StateMachine.StateListener;
import org.openhds.mobile.projectdata.ProjectFormFields;
import org.openhds.mobile.projectdata.ProjectQueryHelper;
import org.openhds.mobile.projectdata.ProjectResources;
import org.openhds.mobile.utilities.EncryptionHelper;
import org.openhds.mobile.utilities.LuhnValidator;
import org.openhds.mobile.utilities.OdkCollectHelper;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

public class CensusActivity extends Activity implements HierarchyNavigator {

	public static final int EDIT_FORM_INTENT = 1;

	public static final String REGION_STATE = "region";
	public static final String PROVINCE_STATE = "province";
	public static final String DISTRICT_STATE = "district";
	public static final String MAP_AREA_STATE = "mapArea";
	public static final String SECTOR_STATE = "sector";
	public static final String HOUSEHOLD_STATE = "household";
	public static final String INDIVIDUAL_STATE = "individual";
	public static final String BOTTOM_STATE = "bottom";

	private static final List<String> stateSequence = new ArrayList<String>();
	private static final Map<String, Integer> stateLabels = new HashMap<String, Integer>();
	private static final Map<String, List<FormBehaviour>> formsForStates = new HashMap<String, List<FormBehaviour>>();

	static {
		stateSequence.add(REGION_STATE);
		stateSequence.add(PROVINCE_STATE);
		stateSequence.add(DISTRICT_STATE);
		stateSequence.add(MAP_AREA_STATE);
		stateSequence.add(SECTOR_STATE);
		stateSequence.add(HOUSEHOLD_STATE);
		stateSequence.add(INDIVIDUAL_STATE);
		stateSequence.add(BOTTOM_STATE);

		stateLabels.put(REGION_STATE, R.string.region_label);
		stateLabels.put(PROVINCE_STATE, R.string.province_label);
		stateLabels.put(DISTRICT_STATE, R.string.district_label);
		stateLabels.put(MAP_AREA_STATE, R.string.map_area_label);
		stateLabels.put(SECTOR_STATE, R.string.sector_label);
		stateLabels.put(HOUSEHOLD_STATE, R.string.household_label);
		stateLabels.put(INDIVIDUAL_STATE, R.string.individual_label);
		stateLabels.put(BOTTOM_STATE, R.string.bottom_label);

		ArrayList<FormBehaviour> regionFormList = new ArrayList<FormBehaviour>();
		ArrayList<FormBehaviour> provinceFormList = new ArrayList<FormBehaviour>();
		ArrayList<FormBehaviour> districtFormList = new ArrayList<FormBehaviour>();
		ArrayList<FormBehaviour> mapAreaFormList = new ArrayList<FormBehaviour>();
		ArrayList<FormBehaviour> sectorFormList = new ArrayList<FormBehaviour>();
		ArrayList<FormBehaviour> householdFormList = new ArrayList<FormBehaviour>();
		ArrayList<FormBehaviour> individualFormList = new ArrayList<FormBehaviour>();
		ArrayList<FormBehaviour> bottomFormList = new ArrayList<FormBehaviour>();

		individualFormList.add(new FormBehaviour("Individual",
				R.string.create_head_of_household_label, null));
		individualFormList.add(new FormBehaviour("Individual",
				R.string.add_member_of_household_label, null));

		bottomFormList.add(new FormBehaviour("Individual",
				R.string.edit_individual_label, INDIVIDUAL_STATE));

		formsForStates.put(REGION_STATE, regionFormList);
		formsForStates.put(PROVINCE_STATE, provinceFormList);
		formsForStates.put(DISTRICT_STATE, districtFormList);
		formsForStates.put(MAP_AREA_STATE, mapAreaFormList);
		formsForStates.put(SECTOR_STATE, sectorFormList);
		formsForStates.put(HOUSEHOLD_STATE, householdFormList);
		formsForStates.put(INDIVIDUAL_STATE, individualFormList);
		formsForStates.put(BOTTOM_STATE, bottomFormList);
	}

	private static final String SELECTION_FRAGMENT_TAG = "hierarchySelectionFragment";
	private static final String VALUE_FRAGMENT_TAG = "hierarchyValueFragment";
	private static final String FORM_FRAGMENT_TAG = "hierarchyFormFragment";

	private StateMachine stateMachine;
	private FormHelper formHelper;
	private Map<String, QueryResult> hierarchyPath;
	private List<QueryResult> currentResults;
	private HierarchySelectionFragment selectionFragment;
	private HierarchyValueFragment valueFragment;
	private HierarchyFormFragment formFragment;

	// TODO: reconsider where to maintain the current head of household
	private Individual currentHeadOfHousehold;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.census_activity);
		hierarchyPath = new HashMap<String, QueryResult>();
		stateMachine = new StateMachine(new HashSet<String>(stateSequence),
				stateSequence.get(0));
		for (String state : stateSequence) {
			stateMachine.registerListener(state, new HierarchyStateListener());
		}

		formHelper = new FormHelper(getContentResolver());

		if (null == savedInstanceState) {
			// create fresh activity
			selectionFragment = new HierarchySelectionFragment();
			selectionFragment.setNavigator(this);
			valueFragment = new HierarchyValueFragment();
			valueFragment.setNavigator(this);
			formFragment = new HierarchyFormFragment();
			formFragment.setNavigator(this);

			getFragmentManager()
					.beginTransaction()
					.add(R.id.left_column, selectionFragment,
							SELECTION_FRAGMENT_TAG)
					.add(R.id.middle_column, valueFragment, VALUE_FRAGMENT_TAG)
					.add(R.id.right_column, formFragment, FORM_FRAGMENT_TAG)
					.commit();

		} else {
			// restore saved activity state
			selectionFragment = (HierarchySelectionFragment) getFragmentManager()
					.findFragmentByTag(SELECTION_FRAGMENT_TAG);
			selectionFragment.setNavigator(this);
			valueFragment = (HierarchyValueFragment) getFragmentManager()
					.findFragmentByTag(VALUE_FRAGMENT_TAG);
			valueFragment.setNavigator(this);
			formFragment = (HierarchyFormFragment) getFragmentManager()
					.findFragmentByTag(FORM_FRAGMENT_TAG);
			formFragment.setNavigator(this);

			// try to re-fetch selected data all the way down the hierarchy path
			for (String state : stateSequence) {
				if (savedInstanceState.containsKey(state)) {
					String extId = savedInstanceState.getString(state);

					if (null == extId) {
						break;
					}
					QueryResult qr = ProjectQueryHelper.getIfExists(
							getContentResolver(), state, extId);
					if (null == qr) {
						break;
					} else {
						hierarchyPath.put(state, qr);
					}
				} else {
					break;
				}
			}
		}
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		// save extIds all down the hierarchy path
		for (String state : stateSequence) {
			if (hierarchyPath.containsKey(state)) {
				QueryResult selected = hierarchyPath.get(state);
				savedInstanceState.putString(state, selected.getExtId());
			}
		}
		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	protected void onResume() {
		super.onResume();
		hierarchySetup();
	}

	private void hierarchySetup() {
		int stateIndex = 0;
		for (String state : stateSequence) {
			if (hierarchyPath.containsKey(state)) {
				updateButtonLabel(state);
				selectionFragment.setButtonAllowed(state, true);
				stateIndex++;
			} else {
				break;
			}
		}

		String state = stateSequence.get(stateIndex);
		if (0 == stateIndex) {
			selectionFragment.setButtonAllowed(state, true);
			currentResults = ProjectQueryHelper.getAll(getContentResolver(),
					stateSequence.get(0));
		} else {
			String previousState = stateSequence.get(stateIndex - 1);
			QueryResult previousSelection = hierarchyPath.get(previousState);
			currentResults = ProjectQueryHelper.getChildren(
					getContentResolver(), previousSelection, state);
		}
		// make sure that listeners will fire for the current state
		stateMachine.transitionTo(stateSequence.get(0));
		stateMachine.transitionTo(state);

		valueFragment.populateValues(currentResults);
	}

	private void updateButtonLabel(String state) {

		QueryResult selected = hierarchyPath.get(state);
		if (null == selected) {
			String stateLabel = getResourceString(CensusActivity.this,
					stateLabels.get(state));
			selectionFragment.setButtonLabel(state, stateLabel, null);
			selectionFragment.setButtonHighlighted(state, true);
		} else {
			selectionFragment.setButtonLabel(state, selected.getName(),
					selected.getExtId());
			selectionFragment.setButtonHighlighted(state, false);
		}
	}

	@Override
	public Map<String, Integer> getStateLabels() {
		return stateLabels;
	}

	@Override
	public List<String> getStateSequence() {
		return stateSequence;
	}

	// Required to update currentResults for targetState and transition to the
	// targetState.
	@Override
	public void jumpUp(String targetState) {
		int targetIndex = stateSequence.indexOf(targetState);
		if (targetIndex < 0) {
			throw new IllegalStateException("Target state <" + targetState
					+ "> is not a valid state");
		}

		String currentState = stateMachine.getState();
		int currentIndex = stateSequence.indexOf(currentState);
		if (targetIndex >= currentIndex) {
			// use stepDown() to go down the hierarchy
			return;
		}

		// un-traverse the hierarchy up to the target state
		for (int i = currentIndex; i >= targetIndex; i--) {
			String state = stateSequence.get(i);
			selectionFragment.setButtonAllowed(state, false);
			hierarchyPath.remove(state);
		}

		// prepare to stepDown() from this target state
		if (0 == targetIndex) {
			// root of the hierarchy
			currentResults = ProjectQueryHelper.getAll(getContentResolver(),
					stateSequence.get(0));
		} else {
			// middle of the hierarchy
			String previousState = stateSequence.get(targetIndex - 1);
			QueryResult previousSelection = hierarchyPath.get(previousState);
			currentResults = ProjectQueryHelper.getChildren(
					getContentResolver(), previousSelection, targetState);
		}
		stateMachine.transitionTo(targetState);
	}

	// Required to update currentResults with children of selected, and
	// transition to the next state down.
	@Override
	public void stepDown(QueryResult selected) {

		String currentState = stateMachine.getState();
		if (!currentState.equals(selected.getState())) {
			throw new IllegalStateException("Selected state <"
					+ selected.getState() + "> mismatch with current state <"
					+ currentState + ">");
		}
		//
		int currentIndex = stateSequence.indexOf(currentState);
		if (currentIndex >= 0 && currentIndex < stateSequence.size() - 1) {
			String nextState = stateSequence.get(currentIndex + 1);

			currentResults = ProjectQueryHelper.getChildren(
					getContentResolver(), selected, nextState);

			hierarchyPath.put(currentState, selected);
			stateMachine.transitionTo(nextState);
		}
	}

	private class HierarchyStateListener implements StateListener {

		@Override
		public void onEnterState() {
			String state = stateMachine.getState();
			updateButtonLabel(state);
			if (!state.equals(stateSequence.get(stateSequence.size() - 1))) {
				selectionFragment.setButtonAllowed(state, true);
			}

			if (state.equals(INDIVIDUAL_STATE)) {
				// Does a household exist at this location
				// and does it have a valid head of household
				boolean headOfHouseholdDefined = false;
				currentHeadOfHousehold = null;
				String locationExtId = hierarchyPath.get(HOUSEHOLD_STATE)
						.getExtId();
				String socialGroupExtId = locationExtId;
				Cursor socialGroupCursor = Queries.getSocialGroupByExtId(
						getContentResolver(), socialGroupExtId);
				if (socialGroupCursor.moveToFirst()) {
					SocialGroup socialGroup = Converter.toSocialGroup(
							socialGroupCursor, true);
					String headExtId = socialGroup.getGroupHead();
					if (!"UNK".equals(headExtId)) {
						Cursor individualCursor = Queries.getIndividualByExtId(
								getContentResolver(), headExtId);
						if (individualCursor.moveToFirst()) {
							headOfHouseholdDefined = true;
							currentHeadOfHousehold = Converter.toIndividual(
									individualCursor, true);

							for (QueryResult qr : currentResults) {
								Cursor relationshipCursor = Queries
										.getRelationshipByBothIndividuals(
												getContentResolver(),
												currentHeadOfHousehold
														.getExtId(), qr
														.getExtId());
								if (relationshipCursor.moveToFirst()) {
									Relationship relationship = Converter
											.toRelationship(relationshipCursor,
													true);
									qr.getStringsPayLoad()
											.put(R.string.relationship_to_head_label,
													getString(ProjectResources.Relationship
															.getRelationshipStringId((relationship
																	.getType()))));
								}
								relationshipCursor.close();
							}
						}
						individualCursor.close();
					}
				}
				socialGroupCursor.close();

				List<FormBehaviour> individualForms = formsForStates.get(state);
				if (headOfHouseholdDefined) {
					formFragment.createFormButtons(individualForms
							.subList(1, 2));
				} else {
					formFragment.createFormButtons(individualForms
							.subList(0, 1));
				}

			} else if (state.equals(BOTTOM_STATE)) {
				Map<String, String> formFieldNames = makeIndividualFormMap(
						hierarchyPath.get(HOUSEHOLD_STATE).getExtId(),
						hierarchyPath.get(INDIVIDUAL_STATE).getExtId());
				currentResults = new ArrayList<QueryResult>();

				currentResults.add(ProjectQueryHelper
						.createCompleteIndividualQueryResult(formFieldNames,
								state, getBaseContext()));

				formFragment.createFormButtons(formsForStates.get(state));

			} else {
				formFragment.createFormButtons(formsForStates.get(state));
			}

			valueFragment.populateValues(currentResults);

		}

		@Override
		public void onExitState() {
			String state = stateMachine.getState();
			updateButtonLabel(state);

		}
	}

	private Map<String, String> makeIndividualFormMap(String householdExtID,
			String individualExtId) {
		Map<String, String> formFieldMap = getFormFieldNameMap(null);
		Cursor cursor = Queries.getIndividualByExtId(getContentResolver(),
				individualExtId);
		cursor.moveToFirst();
		Individual individualToEdit = Converter.toIndividual(cursor, true);

		formFieldMap = getFormFieldNameMap(IndividualAdapter
				.individualToFormFields(individualToEdit));

		cursor = Queries.getMembershipByHouseholdAndIndividualExtId(
				getContentResolver(), householdExtID, individualExtId);
		cursor.moveToFirst();
		Membership membership = Converter.toMembership(cursor, true);
		formFieldMap.put(ProjectFormFields.Individuals.RELATIONSHIP_TO_HEAD,
				membership.getRelationshipToHead());
		formFieldMap.put(ProjectFormFields.Individuals.MEMBER_STATUS,
				membership.getStatus());

		return formFieldMap;

	}

	@Override
	public void launchForm(FormBehaviour form) {
		Map<String, String> formFieldMap = getFormFieldNameMap(null);
		String editState;

		if (null != (editState = form.getEditForState())) {
			String extId = hierarchyPath.get(form.getEditForState()).getExtId();

			switch (editState) {
			case INDIVIDUAL_STATE:
				formFieldMap = makeIndividualFormMap(
						hierarchyPath.get(HOUSEHOLD_STATE).getExtId(), extId);
				break;
			}

		}

		formHelper.newFormInstance(form, formFieldMap);
		Intent intent = formHelper.buildEditFormInstanceIntent();
		startActivityForResult(intent, EDIT_FORM_INTENT);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == EDIT_FORM_INTENT) {

			if (resultCode == RESULT_OK) {

				// CURRENTLY: only handles individual related forms.
				// TODO: Allow checking for forms from other states.

				if (formHelper.checkFormInstanceStatus()) {

					QueryResult selectedLocation = hierarchyPath
							.get(HOUSEHOLD_STATE);

					Map<String, String> formInstanceData = formHelper
							.getFormInstanceData();

					// File encryption
					EncryptionHelper
							.encryptFiles(
									FormInstance
											.toListOfFiles(OdkCollectHelper
													.getAllFormInstances(getContentResolver())),
									this);

					// INSERT or UPDATE INDIVIDUAL
					Individual individual = IndividualAdapter
							.create(formInstanceData);
					IndividualAdapter.insertOrUpdate(getContentResolver(),
							individual);

					// Pull relevant strings from the FormRecord.
					FormBehaviour formBehaviors = formHelper.getForm();
					String locationExtId = hierarchyPath.get(HOUSEHOLD_STATE)
							.getExtId();
					String socialGroupExtId = locationExtId;
					String relationshipType = formInstanceData
							.get(ProjectFormFields.Individuals.RELATIONSHIP_TO_HEAD);
					String membershipStatus = formInstanceData
							.get(ProjectFormFields.Individuals.MEMBER_STATUS);

					// HANDLE HEAD OF HOUSEHOLD CREATION
					if (formBehaviors.getFormLabelId() == R.string.create_head_of_household_label) {
						// update name of location
						Cursor locationCursor = Queries.getLocationByExtId(
								getContentResolver(), locationExtId);
						locationCursor.moveToNext();
						Location location = Converter.toLocation(
								locationCursor, true);
						String locationName = individual.getLastName();
						location.setName(locationName);
						selectedLocation.setName(locationName);
						LocationAdapter.update(getContentResolver(), location);

						// get the social group associated with current location
						Cursor socialGroupCursor = Queries
								.getSocialGroupByExtId(getContentResolver(),
										socialGroupExtId);
						SocialGroup socialGroup;
						// if socialGroup already exists, update it
						if (socialGroupCursor.moveToFirst()) {
							socialGroup = Converter.toSocialGroup(
									socialGroupCursor, true);
							socialGroup.setGroupHead(individual.getExtId());
							socialGroup.setGroupName(locationName);
							SocialGroupAdapter.update(getContentResolver(),
									socialGroup);
							// if socialGroup doesn't exist, create it and
							// insert it.
						} else {
							socialGroup = SocialGroupAdapter.create(
									socialGroupExtId, individual);

							SocialGroupAdapter.insertOrUpdate(
									getContentResolver(), socialGroup);
						}
						socialGroupCursor.close();

						// create membership of Head of Household in own
						// household
						Membership membership = MembershipAdapter.create(
								individual, socialGroup, relationshipType,
								membershipStatus);
						MembershipAdapter.insertOrUpdate(getContentResolver(),
								membership);

						String startDate = formInstanceData
								.get(ProjectFormFields.General.COLLECTED_DATE_TIME);

						// Set head of household's relationship to himself.
						Relationship relationship = RelationshipAdapter.create(
								individual, individual, relationshipType,
								startDate);
						RelationshipAdapter.insertOrUpdate(
								getContentResolver(), relationship);

						// NON-HEAD OF HOUSEHOLD FORM CREATIONS/EDITS
					} else {

						String startDate = formInstanceData
								.get(ProjectFormFields.General.COLLECTED_DATE_TIME);

						// INSERT or UPDATE RELATIONSHIP
						Relationship relationship = RelationshipAdapter.create(
								currentHeadOfHousehold, individual,
								relationshipType, startDate);
						RelationshipAdapter.insertOrUpdate(
								getContentResolver(), relationship);

						// INSERT or UPDATE MEMBERSHIP
						Cursor socialGroupCursor = Queries
								.getSocialGroupByExtId(getContentResolver(),
										socialGroupExtId);
						if (socialGroupCursor.moveToFirst()) {
							SocialGroup socialGroup = Converter.toSocialGroup(
									socialGroupCursor, true);
							Membership membership = MembershipAdapter.create(
									individual, socialGroup, relationshipType,
									membershipStatus);

							MembershipAdapter.insertOrUpdate(
									getContentResolver(), membership);
						}
						socialGroupCursor.close();
					}

					// refresh to reflect new individual
					jumpUp(HOUSEHOLD_STATE);
					stepDown(selectedLocation);

				} else {
					// TODO: otherstuff
				}

			}
			if (resultCode == RESULT_CANCELED) {
				// Write your code if there's no result
			}
		}

	}

	// Takes in optional map to merge with
	public Map<String, String> getFormFieldNameMap(Map<String, String> inputMap) {

		HashMap<String, String> formFieldNames = new HashMap<String, String>();

		for (String state : stateSequence) {
			if (null != hierarchyPath.get(state)) {
				String fieldName = ProjectFormFields.General
						.getExtIdFieldNameFromState(state);
				formFieldNames.put(fieldName, hierarchyPath.get(state)
						.getExtId());
			}
		}

		FieldWorker fieldWorker = (FieldWorker) getIntent().getExtras().get(
				FieldWorkerLoginFragment.FIELD_WORKER_EXTRA);
		formFieldNames.put(
				ProjectFormFields.General.COLLECTED_BY_FIELD_WORKER_EXTID,
				fieldWorker.getExtId());

		Calendar c = Calendar.getInstance();
		formFieldNames.put(ProjectFormFields.General.COLLECTED_DATE_TIME, c
				.getTime().toString());
		
		if (currentHeadOfHousehold == null) {

			formFieldNames.put(
					ProjectFormFields.Individuals.RELATIONSHIP_TO_HEAD, "1");

		}

		String generatedIdPrefix = fieldWorker.getCollectedIdPrefix();

		Cursor cursor = getIndividualsExtIdsByPrefix(getContentResolver(),
				generatedIdPrefix);
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

		formFieldNames.put(ProjectFormFields.Individuals.INDIVIDUAL_EXTID,
				individualExtId);

		if (null != inputMap) {
			for (String key : formFieldNames.keySet()) {
				if (!inputMap.containsKey(key)) {
					inputMap.put(key, formFieldNames.get(key));
				}
			}
			return inputMap;
		}

		return formFieldNames;
	}
}
