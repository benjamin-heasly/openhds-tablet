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
import org.openhds.mobile.model.FormHelper;
import org.openhds.mobile.model.FormRecord;
import org.openhds.mobile.model.Individual;
import org.openhds.mobile.model.Location;
import org.openhds.mobile.model.Membership;
import org.openhds.mobile.model.Relationship;
import org.openhds.mobile.model.SocialGroup;
import org.openhds.mobile.model.StateMachine;
import org.openhds.mobile.model.StateMachine.StateListener;
import org.openhds.mobile.projectdata.ProjectFormFields;
import org.openhds.mobile.projectdata.ProjectQueryHelper;
import org.openhds.mobile.utilities.LuhnValidator;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
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
	private static final Map<String, List<FormRecord>> formsForStates = new HashMap<String, List<FormRecord>>();

	private static final String CREATE_HEAD_OF_HOUSEHOLD_LABEL = "Create Head of Household";
	private static final String ADD_MEMBER_OF_HOUSEHOLD_LABEL = "Add Member of Household";
	private static final String EDIT_INDIVIDUAL_LABEL = "Edit Individual";

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

		ArrayList<FormRecord> regionFormList = new ArrayList<FormRecord>();
		ArrayList<FormRecord> provinceFormList = new ArrayList<FormRecord>();
		ArrayList<FormRecord> districtFormList = new ArrayList<FormRecord>();
		ArrayList<FormRecord> mapAreaFormList = new ArrayList<FormRecord>();
		ArrayList<FormRecord> sectorFormList = new ArrayList<FormRecord>();
		ArrayList<FormRecord> householdFormList = new ArrayList<FormRecord>();
		ArrayList<FormRecord> individualFormList = new ArrayList<FormRecord>();
		ArrayList<FormRecord> bottomFormList = new ArrayList<FormRecord>();

		individualFormList.add(new FormRecord("Individual",
				CREATE_HEAD_OF_HOUSEHOLD_LABEL, null));
		individualFormList.add(new FormRecord("Individual",
				ADD_MEMBER_OF_HOUSEHOLD_LABEL, null));

		bottomFormList.add(new FormRecord("Individual", EDIT_INDIVIDUAL_LABEL,
				INDIVIDUAL_STATE));

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
	public static final String RELATIONSHIP_TO_HEAD_KEY = "relationship to head";

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
				String socialGroupExtId = locationExtId.substring(3);
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
									qr.getPayLoad().put(
											RELATIONSHIP_TO_HEAD_KEY,
											relationship.getType());
								}
								relationshipCursor.close();
							}
						}
						individualCursor.close();
					}
				}
				socialGroupCursor.close();

				List<FormRecord> individualForms = formsForStates.get(state);
				if (headOfHouseholdDefined) {
					formFragment.createFormButtons(individualForms
							.subList(1, 2));
				} else {
					formFragment.createFormButtons(individualForms
							.subList(0, 1));
				}

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

	@Override
	public void launchForm(FormRecord form) {
		Map<String, String> formFieldMap = getFormFieldNameMap();
		String editState;

		if (null != (editState = form.getEditForState())) {
			String extId = hierarchyPath.get(form.getEditForState()).getExtId();

			switch (editState) {
			case INDIVIDUAL_STATE:
				Cursor cursor = Queries.getIndividualByExtId(getContentResolver(), extId);
				cursor.moveToFirst();
				Individual individualToEdit = Converter.toIndividual(cursor, true);		
				formFieldMap = IndividualAdapter.individualToFormFields(individualToEdit);
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
				if (formHelper.checkFormInstanceStatus()) {

					QueryResult selectedLocation = hierarchyPath
							.get(HOUSEHOLD_STATE);

					Map<String, String> formInstanceData = formHelper
							.getFormInstanceData();
					Individual individual = IndividualAdapter
							.create(formInstanceData);
					Uri result = IndividualAdapter.insert(getContentResolver(),
							individual);

					FormRecord formRecord = formHelper.getForm();
					String locationExtId = hierarchyPath.get(HOUSEHOLD_STATE)
							.getExtId();
					String socialGroupExtId = locationExtId.substring(3);
					String relationshipType = formInstanceData
							.get(ProjectFormFields.Individuals.RELATIONSHIP_TO_HEAD);
					String membershipStatus = formInstanceData
							.get(ProjectFormFields.Individuals.MEMBER_STATUS);
					String locationName = "House of "
							+ individual.getLastName();
					if (formRecord.getFormLabel().equals(
							CREATE_HEAD_OF_HOUSEHOLD_LABEL)) {
						// update name of location
						Cursor locationCursor = Queries.getLocationByExtId(
								getContentResolver(), locationExtId);
						locationCursor.moveToNext();
						Location location = Converter.toLocation(
								locationCursor, true);
						location.setName(locationName);
						selectedLocation.setName(locationName);
						int rowsAffected = LocationAdapter.update(
								getContentResolver(), location);

						// get the social group associated with current location
						Cursor socialGroupCursor = Queries
								.getSocialGroupByExtId(getContentResolver(),
										socialGroupExtId);
						SocialGroup socialGroup;
						if (socialGroupCursor.moveToFirst()) {
							socialGroup = Converter.toSocialGroup(
									socialGroupCursor, true);
							socialGroup.setGroupHead(individual.getExtId());
							socialGroup.setGroupName(locationName);
							rowsAffected = SocialGroupAdapter.update(
									getContentResolver(), socialGroup);
						} else {
							socialGroup = SocialGroupAdapter.create(
									socialGroupExtId, individual);
							result = SocialGroupAdapter.insert(
									getContentResolver(), socialGroup);
						}
						socialGroupCursor.close();

						// create membership of hoh in own household
						Membership membership = MembershipAdapter.create(
								individual, socialGroup, relationshipType,
								membershipStatus);
						result = MembershipAdapter.insert(getContentResolver(),
								membership);

					} else {
						// make relationship to head of household
						String startDate = formInstanceData
								.get(ProjectFormFields.General.COLLECTED_DATE_TIME);
						Relationship relationship = RelationshipAdapter.create(
								currentHeadOfHousehold, individual,
								relationshipType, startDate);
						RelationshipAdapter.insert(getContentResolver(),
								relationship);

						// make membership in household
						Cursor socialGroupCursor = Queries
								.getSocialGroupByExtId(getContentResolver(),
										socialGroupExtId);
						if (socialGroupCursor.moveToFirst()) {
							SocialGroup socialGroup = Converter.toSocialGroup(
									socialGroupCursor, true);
							Membership membership = MembershipAdapter.create(
									individual, socialGroup, relationshipType,
									membershipStatus);
							result = MembershipAdapter.insert(
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

	public Map<String, String> getFormFieldNameMap() {

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

		return formFieldNames;
	}
}
