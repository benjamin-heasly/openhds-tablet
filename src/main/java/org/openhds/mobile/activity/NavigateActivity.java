package org.openhds.mobile.activity;

import static org.openhds.mobile.utilities.ConfigUtils.getResourceString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.openhds.mobile.R;
import org.openhds.mobile.database.queries.QueryResult;
import org.openhds.mobile.fragment.DetailToggleFragment;
import org.openhds.mobile.fragment.FieldWorkerLoginFragment;
import org.openhds.mobile.fragment.HierarchyFormFragment;
import org.openhds.mobile.fragment.HierarchySelectionFragment;
import org.openhds.mobile.fragment.HierarchyValueFragment;
import org.openhds.mobile.fragment.VisitFragment;
import org.openhds.mobile.fragment.detailfragments.DefaultDetailFragment;
import org.openhds.mobile.fragment.detailfragments.DetailFragment;
import org.openhds.mobile.model.FieldWorker;
import org.openhds.mobile.model.FormBehaviour;
import org.openhds.mobile.model.FormHelper;
import org.openhds.mobile.model.FormInstance;
import org.openhds.mobile.model.StateMachine;
import org.openhds.mobile.model.StateMachine.StateListener;
import org.openhds.mobile.model.Visit;
import org.openhds.mobile.projectdata.NavigatePluginModule;
import org.openhds.mobile.projectdata.ProjectActivityBuilder;
import org.openhds.mobile.projectdata.QueryHelpers.QueryHelper;
import org.openhds.mobile.utilities.EncryptionHelper;
import org.openhds.mobile.utilities.OdkCollectHelper;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;

public class NavigateActivity extends Activity implements HierarchyNavigator {

	private HierarchySelectionFragment selectionFragment;
	private HierarchyValueFragment valueFragment;
	private HierarchyFormFragment formFragment;
	private DetailToggleFragment detailToggleFragment;
	private DetailFragment defaultDetailFragment;
	private DetailFragment detailFragment;
	private VisitFragment visitFragment;

	private static final String SELECTION_FRAGMENT_TAG = "hierarchySelectionFragment";
	private static final String VALUE_FRAGMENT_TAG = "hierarchyValueFragment";
	private static final String FORM_FRAGMENT_TAG = "hierarchyFormFragment";
	private static final String TOGGLE_FRAGMENT_TAG = "hierarchyToggleFragment";
	private static final String DETAIL_FRAGMENT_TAG = "hierarchyDetailFragment";
	private static final String VISIT_FRAGMENT_TAG = "hierarchyVisitFragment";

	private static Map<String, List<FormBehaviour>> formsForStates;
	private static Map<String, Integer> stateLabels;
	private static List<String> stateSequence;

	private FormHelper formHelper;
	private StateMachine stateMachine;
	private Map<String, QueryResult> hierarchyPath;
	private Map<String, DetailFragment> detailFragsForStates;
	private List<QueryResult> currentResults;
	private QueryResult currentSelection;
	private QueryHelper queryHelper;
	private FieldWorker currentFieldWorker;
	private Visit currentVisit;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.navigate_activity);

		// /////////////////////////////////////////////////////////////////////////////////////////////
		// ACTIVITY BUILDING ZONE~ //
		// /////////////////////////////////////////////////////////////////////////////////////////////

		FieldWorker fieldWorker = (FieldWorker) getIntent().getExtras().get(
				FieldWorkerLoginFragment.FIELD_WORKER_EXTRA);
		setCurrentFieldWorker(fieldWorker);

		String builderName = (String) getIntent().getExtras().get(
				ProjectActivityBuilder.ACTIVITY_MODULE_EXTRA);

		NavigatePluginModule builder = ProjectActivityBuilder
				.getModuleByName(builderName);

		setTitle(builderName);

		stateLabels = builder.getStateLabels();
		stateSequence = builder.getStateSequence();
		queryHelper = builder.getQueryHelper();
		formsForStates = builder.getFormsforstates();
		detailFragsForStates = builder.getDetailFragsForStates();

		hierarchyPath = new HashMap<String, QueryResult>();
		formHelper = new FormHelper(getContentResolver());
		stateMachine = new StateMachine(new HashSet<String>(stateSequence),
				stateSequence.get(0));

		for (String state : stateSequence) {
			stateMachine.registerListener(state, new HierarchyStateListener());
		}

		// /////////////////////////////////////////////////////////////////////////////////////////////
		// ACTIVITY BUILDING ZONE~ //
		// /////////////////////////////////////////////////////////////////////////////////////////////

		if (null == savedInstanceState) {
			// create fresh activity
			selectionFragment = new HierarchySelectionFragment();
			selectionFragment.setNavigator(this);
			valueFragment = new HierarchyValueFragment();
			valueFragment.setNavigator(this);
			formFragment = new HierarchyFormFragment();
			formFragment.setNavigator(this);
			detailToggleFragment = new DetailToggleFragment();
			detailToggleFragment.setNavigateActivity(this);
			defaultDetailFragment = new DefaultDetailFragment();
			visitFragment = new VisitFragment();
			visitFragment.setNavigateActivity(this);
	
			getFragmentManager()
					.beginTransaction()
					.add(R.id.left_column_top, selectionFragment,
							SELECTION_FRAGMENT_TAG)
					.add(R.id.left_column_bottom, detailToggleFragment,
							TOGGLE_FRAGMENT_TAG)
					.add(R.id.middle_column, valueFragment, VALUE_FRAGMENT_TAG)
					.add(R.id.right_column_top, formFragment, FORM_FRAGMENT_TAG)
					.add(R.id.right_column_bottom, visitFragment,
							VISIT_FRAGMENT_TAG).commit();

		} else {

			FragmentManager fragmentManager = getFragmentManager();
			// restore saved activity state
			selectionFragment = (HierarchySelectionFragment) fragmentManager
					.findFragmentByTag(SELECTION_FRAGMENT_TAG);
			selectionFragment.setNavigator(this);

			formFragment = (HierarchyFormFragment) fragmentManager
					.findFragmentByTag(FORM_FRAGMENT_TAG);
			formFragment.setNavigator(this);

			detailToggleFragment = (DetailToggleFragment) fragmentManager
					.findFragmentByTag(TOGGLE_FRAGMENT_TAG);
			detailToggleFragment.setNavigateActivity(this);

			visitFragment = (VisitFragment) fragmentManager
					.findFragmentByTag(VISIT_FRAGMENT_TAG);
			visitFragment.setNavigateActivity(this);

			defaultDetailFragment = new DefaultDetailFragment();

			valueFragment = (HierarchyValueFragment) fragmentManager
					.findFragmentByTag(VALUE_FRAGMENT_TAG);

			// draw details if valuefrag is null, the drawing of valuefrag is
			// handled in onResume().
			if (null == valueFragment) {
				valueFragment = new HierarchyValueFragment();

				detailFragment = (DetailFragment) fragmentManager
						.findFragmentByTag(DETAIL_FRAGMENT_TAG);
				detailFragment.setNavigateActivity(this);

			}
			valueFragment.setNavigator(this);

			for (String state : stateSequence) {
				if (savedInstanceState.containsKey(state)) {
					String extId = savedInstanceState.getString(state);

					if (null == extId) {
						break;
					}
					QueryResult qr = queryHelper.getIfExists(
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
	protected void onResume() {
		super.onResume();
		hierarchySetup();

	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		for (String state : stateSequence) {
			if (hierarchyPath.containsKey(state)) {
				QueryResult selected = hierarchyPath.get(state);
				savedInstanceState.putString(state, selected.getExtId());
			}
		}

		super.onSaveInstanceState(savedInstanceState);
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
			currentResults = queryHelper.getAll(getContentResolver(),
					stateSequence.get(0));
			updateToggleButton();

		} else {
			String previousState = stateSequence.get(stateIndex - 1);
			QueryResult previousSelection = hierarchyPath.get(previousState);
			currentSelection = previousSelection;
			currentResults = queryHelper.getChildren(getContentResolver(),
					previousSelection, state);
		}

		boolean isAdded = valueFragment.isAdded();

		// make sure that listeners will fire for the current state
		refreshHierarchy(getState());

		if (isAdded) {
			showValueFragment();
			valueFragment.populateValues(currentResults);
		} else {
			showDetailFragment();
			detailToggleFragment.setButtonHighlighted(true);
		}
	}

	public Map<String, QueryResult> getHierarchyPath() {
		return hierarchyPath;
	}

	public List<QueryResult> getCurrentResults() {
		return currentResults;
	}

	public String getState() {
		return stateMachine.getState();
	}

	private void updateButtonLabel(String state) {

		QueryResult selected = hierarchyPath.get(state);
		if (null == selected) {
			String stateLabel = getResourceString(NavigateActivity.this,
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
		// TODO Auto-generated method stub
		return stateLabels;
	}

	@Override
	public List<String> getStateSequence() {
		return stateSequence;
	}

	@Override
	public void jumpUp(String targetState) {
		int targetIndex = stateSequence.indexOf(targetState);
		if (targetIndex < 0) {
			throw new IllegalStateException("Target state <" + targetState
					+ "> is not a valid state");
		}

		String currentState = getState();
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
			currentResults = queryHelper.getAll(getContentResolver(),
					stateSequence.get(0));
		} else {
			// middle of the hierarchy
			String previousState = stateSequence.get(targetIndex - 1);
			QueryResult previousSelection = hierarchyPath.get(previousState);
			currentSelection = previousSelection;
			currentResults = queryHelper.getChildren(getContentResolver(),
					previousSelection, targetState);
		}
		stateMachine.transitionTo(targetState);

	}

	@Override
	public void stepDown(QueryResult selected) {
		String currentState = getState();

		if (!currentState.equals(selected.getState())) {
			throw new IllegalStateException("Selected state <"
					+ selected.getState() + "> mismatch with current state <"
					+ currentState + ">");
		}

		int currentIndex = stateSequence.indexOf(currentState);
		if (currentIndex >= 0 && currentIndex < stateSequence.size() - 1) {
			String nextState = stateSequence.get(currentIndex + 1);

			currentSelection = selected;
			currentResults = queryHelper.getChildren(getContentResolver(),
					selected, nextState);

			hierarchyPath.put(currentState, selected);
			stateMachine.transitionTo(nextState);
		}

	}

	@Override
	public void launchForm(FormBehaviour form) {

		Map<String, String> formFieldMap = new HashMap<String, String>();

		form.getFormPayloadBuilder().buildFormPayload(formFieldMap, this);

		if (null != form.getFormName()) {
			formHelper.newFormInstance(form, formFieldMap);
			Intent intent = formHelper.buildEditFormInstanceIntent();
			startActivityForResult(intent, 0);
		}

	}

	private void showValueFragment() {

		// there is only 1 value fragment that can be added
		if (!valueFragment.isAdded()) {
			getFragmentManager()
					.beginTransaction()
					.replace(R.id.middle_column, valueFragment,
							VALUE_FRAGMENT_TAG).commit();
			getFragmentManager().executePendingTransactions();

			valueFragment.populateValues(currentResults);
		}
	}

	private void showDetailFragment() {

		// we don't check if it is added here because there are detail fragments
		// for each state

		detailFragment = getDetailFragmentForCurrentState();
		detailFragment.setNavigateActivity(this);

		getFragmentManager()
				.beginTransaction()
				.replace(R.id.middle_column, detailFragment,
						DETAIL_FRAGMENT_TAG).commit();
		getFragmentManager().executePendingTransactions();

		detailFragment.setUpDetails();

	}

	private DetailFragment getDetailFragmentForCurrentState() {

		if (null != (detailFragment = detailFragsForStates.get(getState()))) {
			return detailFragment;
		}
		return defaultDetailFragment;
	}

	private boolean shouldShowDetailFragment() {

		if (null == currentResults || currentResults.isEmpty()) {
			return true;
		}
		return false;

	}

	private void updateToggleButton() {

		if (null != detailFragsForStates.get(getState())
				&& !shouldShowDetailFragment()) {

			detailToggleFragment.setButtonEnabled(true);
			if (!valueFragment.isAdded()) {
				detailToggleFragment.setButtonHighlighted(true);
			}
		} else {
			detailToggleFragment.setButtonEnabled(false);
		}

	}

	// for ONCLICK of the toggleFrag
	public void toggleMiddleFragment() {

		if (valueFragment.isAdded()) {
			showDetailFragment();
			detailToggleFragment.setButtonHighlighted(true);
		} else if (detailFragment.isAdded()) {
			showValueFragment();
			detailToggleFragment.setButtonHighlighted(false);
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (resultCode == RESULT_OK) {
			FormBehaviour form = formHelper.getForm();
			formHelper.checkFormInstanceStatus();
			if (null != formHelper.getFinalizedFormFilePath()) {

				if (form.getFormPayloadConsumer().consumeFormPayload(
						formHelper.getFormInstanceData(), this)) {

					formHelper.updateExistingFormInstance();

				}
			}
			showValueFragment();
		}
		if (resultCode == RESULT_CANCELED) {
			// Write your code if there's no result
		}

		// encrypt files regardless.
		EncryptionHelper.encryptFiles(FormInstance
				.toListOfFiles(OdkCollectHelper
						.getAllFormInstances(getContentResolver())), this);
	}

	public QueryResult getCurrentSelection() {
		return currentSelection;
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		int currentStateIndex;
		if (0 < (currentStateIndex = stateSequence.indexOf(getState()))) {

			jumpUp(stateSequence.get(currentStateIndex - 1));

		} else {
			super.onBackPressed();
		}
	}

	public FieldWorker getCurrentFieldWorker() {
		return currentFieldWorker;
	}

	public void setCurrentFieldWorker(FieldWorker currentFieldWorker) {
		this.currentFieldWorker = currentFieldWorker;
	}

	public Visit getCurrentVisit() {
		return currentVisit;
	}

	public void setCurrentVisit(Visit currentVisit) {
		this.currentVisit = currentVisit;
	}

	public void startVisit(Visit visit) {
		setCurrentVisit(visit);
		visitFragment.setButtonEnabled(true);
	}

	public void finishVisit() {
		setCurrentVisit(null);
		visitFragment.setButtonEnabled(false);
        refreshHierarchy(getState());
		
	}

	// not working??
	private void refreshHierarchy(String state){
		stateMachine.transitionTo(stateSequence.get(0));
		stateMachine.transitionTo(state);
	}
	
	private class HierarchyStateListener implements StateListener {

		@Override
		public void onEnterState() {
			String state = getState();
			updateButtonLabel(state);

			if (!state.equals(stateSequence.get(stateSequence.size() - 1))) {
				selectionFragment.setButtonAllowed(state, true);
			}

			List<FormBehaviour> filteredForms = formsForStates.get(state);
			List<FormBehaviour> validForms = new ArrayList<FormBehaviour>();

			for (FormBehaviour form : filteredForms) {
				if (form.getFormFilter().amIValid(NavigateActivity.this)) {
					validForms.add(form);
				}
			}

			if (shouldShowDetailFragment()) {
				showDetailFragment();
			} else {
				showValueFragment();
				valueFragment.populateValues(currentResults);
			}
			updateToggleButton();

			formFragment.createFormButtons(validForms);

		}

		@Override
		public void onExitState() {
			String state = getState();
			updateButtonLabel(state);
		}
	}

}
