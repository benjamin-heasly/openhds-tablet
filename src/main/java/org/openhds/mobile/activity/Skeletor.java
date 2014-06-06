package org.openhds.mobile.activity;

import static org.openhds.mobile.utilities.ConfigUtils.getResourceString;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.openhds.mobile.R;
import org.openhds.mobile.database.queries.QueryResult;
import org.openhds.mobile.fragment.HierarchyFormFragment;
import org.openhds.mobile.fragment.HierarchySelectionFragment;
import org.openhds.mobile.fragment.HierarchyValueFragment;
import org.openhds.mobile.model.FormBehaviour;
import org.openhds.mobile.model.StateMachine;
import org.openhds.mobile.model.StateMachine.StateListener;
import org.openhds.mobile.projectdata.CensusQueryHelper;
import org.openhds.mobile.projectdata.ProjectActivityBuilder;
import org.openhds.mobile.projectdata.ProjectActivityBuilder.ActivityBuilderModule;
import org.openhds.mobile.projectdata.QueryHelper;

import android.app.Activity;
import android.os.Bundle;

public class Skeletor extends Activity implements HierarchyNavigator {

	private HierarchySelectionFragment selectionFragment;
	private HierarchyValueFragment valueFragment;
	private HierarchyFormFragment formFragment;

	private static final String SELECTION_FRAGMENT_TAG = "hierarchySelectionFragment";
	private static final String VALUE_FRAGMENT_TAG = "hierarchyValueFragment";
	private static final String FORM_FRAGMENT_TAG = "hierarchyFormFragment";

	private static Map<String, Integer> stateLabels;
	private static List<String> stateSequence;
	private StateMachine stateMachine;
	private Map<String, QueryResult> hierarchyPath;
	private List<QueryResult> currentResults;
	private QueryHelper queryHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.navigate_activity);

		// /////////////////////////////////////////////////////////////////////////////////////////////
		// ACTIVITY BUILDING ZONE~ //
		// /////////////////////////////////////////////////////////////////////////////////////////////

		ProjectActivityBuilder.ActivityBuilderModule builder = (ActivityBuilderModule) getIntent()
				.getExtras().get(ProjectActivityBuilder.ACTIVITY_MODULE_EXTRA);

		stateLabels = builder.getStateLabels();
		stateSequence = builder.getStateSequence();
		queryHelper = builder.getQueryHelper();

		hierarchyPath = new HashMap<String, QueryResult>();
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

		}

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
			currentResults = queryHelper.getAll(getContentResolver(),
					stateSequence.get(0));
		} else {
			String previousState = stateSequence.get(stateIndex - 1);
			QueryResult previousSelection = hierarchyPath.get(previousState);
			currentResults = queryHelper.getChildren(getContentResolver(),
					previousSelection, state);
		}
		// make sure that listeners will fire for the current state
		stateMachine.transitionTo(stateSequence.get(0));
		stateMachine.transitionTo(state);

		valueFragment.populateValues(currentResults);
	}

	private void updateButtonLabel(String state) {

		QueryResult selected = hierarchyPath.get(state);
		if (null == selected) {
			String stateLabel = getResourceString(Skeletor.this,
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
	public void jumpUp(String state) {
		// TODO Auto-generated method stub

	}

	@Override
	public void stepDown(QueryResult qr) {
		// TODO Auto-generated method stub

	}

	@Override
	public void launchForm(FormBehaviour form) {
		// TODO Auto-generated method stub

	}

	private class HierarchyStateListener implements StateListener {

		@Override
		public void onEnterState() {

		}

		@Override
		public void onExitState() {
		}
	}
}
