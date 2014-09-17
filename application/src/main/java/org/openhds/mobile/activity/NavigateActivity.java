package org.openhds.mobile.activity;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import org.openhds.mobile.R;
import org.openhds.mobile.fragment.FieldWorkerLoginFragment;
import org.openhds.mobile.fragment.FormSelectionFragment;
import org.openhds.mobile.fragment.DataSelectionFragment;
import org.openhds.mobile.fragment.navigate.DetailToggleFragment;
import org.openhds.mobile.fragment.navigate.HierarchyButtonFragment;
import org.openhds.mobile.fragment.navigate.VisitFragment;
import org.openhds.mobile.fragment.navigate.detail.DefaultDetailFragment;
import org.openhds.mobile.fragment.navigate.detail.DetailFragment;
import org.openhds.mobile.model.FieldWorker;
import org.openhds.mobile.model.FormBehaviour;
import org.openhds.mobile.model.FormHelper;
import org.openhds.mobile.model.FormInstance;
import org.openhds.mobile.model.StateMachine;
import org.openhds.mobile.model.StateMachine.StateListener;
import org.openhds.mobile.model.Visit;
import org.openhds.mobile.projectdata.FormPayloadConsumers.FormPayloadConsumer;
import org.openhds.mobile.projectdata.NavigatePluginModule;
import org.openhds.mobile.projectdata.ProjectActivityBuilder;
import org.openhds.mobile.projectdata.QueryHelpers.QueryHelper;
import org.openhds.mobile.repository.DataWrapper;
import org.openhds.mobile.repository.search.FormSearchPluginModule;
import org.openhds.mobile.utilities.EncryptionHelper;
import org.openhds.mobile.utilities.OdkCollectHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.openhds.mobile.utilities.ConfigUtils.getResourceString;

public class NavigateActivity extends Activity implements HierarchyNavigator {

    private static final int ODK_ACTIVITY_REQUEST_CODE = 0;
    private static final int SEARCH_ACTIVITY_REQUEST_CODE = 1;

    private HierarchyButtonFragment hierarchyButtonFragment;
    private DataSelectionFragment valueFragment;
    private FormSelectionFragment formFragment;
    private DetailToggleFragment detailToggleFragment;
    private DetailFragment defaultDetailFragment;
    private DetailFragment detailFragment;
    private VisitFragment visitFragment;

    private static final String HIERARCHY_BUTTON_FRAGMENT_TAG = "hierarchyButtonFragment";
    private static final String VALUE_FRAGMENT_TAG = "hierarchyValueFragment";
    private static final String FORM_FRAGMENT_TAG = "hierarchyFormFragment";
    private static final String TOGGLE_FRAGMENT_TAG = "hierarchyToggleFragment";
    private static final String DETAIL_FRAGMENT_TAG = "hierarchyDetailFragment";
    private static final String VISIT_FRAGMENT_TAG = "hierarchyVisitFragment";

    private static final String VISIT_KEY = "visitKey";

    private static Map<String, List<FormBehaviour>> formsForStates;
    private static Map<String, Integer> stateLabels;
    private static List<String> stateSequence;

    private FormHelper formHelper;
    private StateMachine stateMachine;
    private Map<String, DataWrapper> hierarchyPath;
    private Map<String, DetailFragment> detailFragsForStates;
    private List<DataWrapper> currentResults;
    private DataWrapper currentSelection;
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

        FieldWorker fieldWorker = (FieldWorker) getIntent().getExtras().get(FieldWorkerLoginFragment.FIELD_WORKER_EXTRA);
        setCurrentFieldWorker(fieldWorker);

        String builderName = (String) getIntent().getExtras().get(ProjectActivityBuilder.ACTIVITY_MODULE_EXTRA);
        NavigatePluginModule builder = ProjectActivityBuilder.getModuleByName(builderName);
        setTitle(builderName);

        stateLabels = builder.getStateLabels();
        stateSequence = builder.getStateSequence();
        queryHelper = builder.getQueryHelper();
        formsForStates = builder.getFormsForStates();
        detailFragsForStates = builder.getDetailFragsForStates();

        hierarchyPath = new HashMap<String, DataWrapper>();
        formHelper = new FormHelper(getContentResolver());
        stateMachine = new StateMachine(new HashSet<String>(stateSequence), stateSequence.get(0));

        for (String state : stateSequence) {
            stateMachine.registerListener(state, new HierarchyStateListener());
        }

        // /////////////////////////////////////////////////////////////////////////////////////////////
        // ACTIVITY BUILDING ZONE~ //
        // /////////////////////////////////////////////////////////////////////////////////////////////

        if (null == savedInstanceState) {
            // fromForm fresh activity
            hierarchyButtonFragment = new HierarchyButtonFragment();
            hierarchyButtonFragment.setNavigator(this);
            valueFragment = new DataSelectionFragment();
            valueFragment.setSelectionHandler(new ValueSelectionHandler());
            formFragment = new FormSelectionFragment();
            formFragment.setSelectionHandler(new FormSelectionHandler());
            detailToggleFragment = new DetailToggleFragment();
            detailToggleFragment.setNavigateActivity(this);
            defaultDetailFragment = new DefaultDetailFragment();
            visitFragment = new VisitFragment();
            visitFragment.setNavigateActivity(this);

            getFragmentManager().beginTransaction()
                    .add(R.id.left_column_top, hierarchyButtonFragment, HIERARCHY_BUTTON_FRAGMENT_TAG)
                    .add(R.id.left_column_bottom, detailToggleFragment, TOGGLE_FRAGMENT_TAG)
                    .add(R.id.middle_column, valueFragment, VALUE_FRAGMENT_TAG)
                    .add(R.id.right_column_top, formFragment, FORM_FRAGMENT_TAG)
                    .add(R.id.right_column_bottom, visitFragment, VISIT_FRAGMENT_TAG)
                    .commit();

        } else {

            FragmentManager fragmentManager = getFragmentManager();
            // restore saved activity state
            hierarchyButtonFragment = (HierarchyButtonFragment) fragmentManager.findFragmentByTag(HIERARCHY_BUTTON_FRAGMENT_TAG);
            hierarchyButtonFragment.setNavigator(this);

            formFragment = (FormSelectionFragment) fragmentManager.findFragmentByTag(FORM_FRAGMENT_TAG);
            formFragment.setSelectionHandler(new FormSelectionHandler());

            detailToggleFragment = (DetailToggleFragment) fragmentManager.findFragmentByTag(TOGGLE_FRAGMENT_TAG);
            detailToggleFragment.setNavigateActivity(this);

            visitFragment = (VisitFragment) fragmentManager.findFragmentByTag(VISIT_FRAGMENT_TAG);
            visitFragment.setNavigateActivity(this);

            defaultDetailFragment = new DefaultDetailFragment();

            valueFragment = (DataSelectionFragment) fragmentManager.findFragmentByTag(VALUE_FRAGMENT_TAG);

            // draw details if valuefrag is null, the drawing of valuefrag is
            // handled in onResume().
            if (null == valueFragment) {
                valueFragment = new DataSelectionFragment();

                detailFragment = (DetailFragment) fragmentManager.findFragmentByTag(DETAIL_FRAGMENT_TAG);
                detailFragment.setNavigateActivity(this);
            }
            valueFragment.setSelectionHandler(new ValueSelectionHandler());

            for (String state : stateSequence) {
                if (savedInstanceState.containsKey(state)) {
                    String extId = savedInstanceState.getString(state);

                    if (null == extId) {
                        break;
                    }
                    DataWrapper qr = queryHelper.getIfExists(getContentResolver(), state, extId);
                    if (null == qr) {
                        break;
                    } else {
                        hierarchyPath.put(state, qr);
                    }
                } else {
                    break;
                }
            }

            setCurrentVisit((Visit)savedInstanceState.get(VISIT_KEY));
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
                DataWrapper selected = hierarchyPath.get(state);
                savedInstanceState.putString(state, selected.getExtId());
            }
        }

        savedInstanceState.putSerializable(VISIT_KEY, getCurrentVisit());
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Intent intent = new Intent();
        switch (item.getItemId()) {
            case R.id.logout_menu_button:
                intent.setClass(this, OpeningActivity.class);
                startActivity(intent);
                return true;
            case R.id.field_worker_home_menu_button:
                intent.setClass(this, PortalActivity.class);
                intent.putExtra(FieldWorkerLoginFragment.FIELD_WORKER_EXTRA, getCurrentFieldWorker());
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void hierarchySetup() {
        int stateIndex = 0;
        for (String state : stateSequence) {
            if (hierarchyPath.containsKey(state)) {
                updateButtonLabel(state);
                hierarchyButtonFragment.setButtonAllowed(state, true);
                stateIndex++;
            } else {
                break;
            }
        }

        String state = stateSequence.get(stateIndex);
        if (0 == stateIndex) {
            hierarchyButtonFragment.setButtonAllowed(state, true);
            currentResults = queryHelper.getAll(getContentResolver(), stateSequence.get(0));
            updateToggleButton();

        } else {
            String previousState = stateSequence.get(stateIndex - 1);
            DataWrapper previousSelection = hierarchyPath.get(previousState);
            currentSelection = previousSelection;
            currentResults = queryHelper.getChildren(getContentResolver(), previousSelection, state);
        }

        boolean isAdded = valueFragment.isAdded();

        // make sure that listeners will fire for the current state
        refreshHierarchy(state);

        if (isAdded || !currentResults.isEmpty()) {
            showValueFragment();
            valueFragment.populateData(currentResults);
        } else {
            showDetailFragment();
            detailToggleFragment.setButtonHighlighted(true);
        }

        if(null != getCurrentVisit()){
            visitFragment.setButtonEnabled(true);
        }
    }

    public Map<String, DataWrapper> getHierarchyPath() {
        return hierarchyPath;
    }

    public List<DataWrapper> getCurrentResults() {
        return currentResults;
    }

    public String getState() {
        return stateMachine.getState();
    }

    private void updateButtonLabel(String state) {

        DataWrapper selected = hierarchyPath.get(state);
        if (null == selected) {
            String stateLabel = getResourceString(NavigateActivity.this, stateLabels.get(state));
            hierarchyButtonFragment.setButtonLabel(state, stateLabel, null);
            hierarchyButtonFragment.setButtonHighlighted(state, true);
        } else {
            hierarchyButtonFragment.setButtonLabel(state, selected.getName(), selected.getExtId());
            hierarchyButtonFragment.setButtonHighlighted(state, false);
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

    @Override
    public void jumpUp(String targetState) {
        int targetIndex = stateSequence.indexOf(targetState);
        if (targetIndex < 0) {
            throw new IllegalStateException("Target state <" + targetState + "> is not a valid state");
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
            hierarchyButtonFragment.setButtonAllowed(state, false);
            hierarchyPath.remove(state);
        }

        // prepare to stepDown() from this target state
        if (0 == targetIndex) {
            // root of the hierarchy
            currentResults = queryHelper.getAll(getContentResolver(), stateSequence.get(0));
        } else {
            // middle of the hierarchy
            String previousState = stateSequence.get(targetIndex - 1);
            DataWrapper previousSelection = hierarchyPath.get(previousState);
            currentSelection = previousSelection;
            currentResults = queryHelper.getChildren(getContentResolver(), previousSelection, targetState);
        }
        stateMachine.transitionTo(targetState);
    }

    @Override
    public void stepDown(DataWrapper selected) {
        String currentState = getState();

        if (!currentState.equals(selected.getCategory())) {
            throw new IllegalStateException("Selected state <"
                    + selected.getCategory() + "> mismatch with current state <"
                    + currentState + ">");
        }

        int currentIndex = stateSequence.indexOf(currentState);
        if (currentIndex >= 0 && currentIndex < stateSequence.size() - 1) {
            String nextState = stateSequence.get(currentIndex + 1);

            currentSelection = selected;
            currentResults = queryHelper.getChildren(getContentResolver(), selected, nextState);

            hierarchyPath.put(currentState, selected);
            stateMachine.transitionTo(nextState);
        }
    }

    @Override
    public void launchForm(FormBehaviour formBehaviour) {

        // use the given form as the current form
        formHelper.setFormBehaviour(formBehaviour);

        // fill a payload of form fields for the current form
        Map<String, String> formFieldData = new HashMap<String, String>();
        formBehaviour.getFormPayloadBuilder().buildFormPayload(formFieldData, this);
        formHelper.setFormFieldData(formFieldData);

        // if needed, ask the user to search for required form field data
        if (formBehaviour.getNeedsFormFieldSearch()) {
            launchCurrentFormInSearchActivity();
            return;
        }

        // otherwise, launch the form in ODK immediately
        launchCurrentFormInODK();
    }

    private void launchCurrentFormInODK() {
        FormBehaviour formBehaviour = formHelper.getFormBehaviour();
        if (null != formBehaviour && null != formBehaviour.getFormName()) {
            formHelper.newFormInstance();
            Intent intent = formHelper.buildEditFormInstanceIntent();
            startActivityForResult(intent, ODK_ACTIVITY_REQUEST_CODE);
        }
    }

    private void launchCurrentFormInSearchActivity() {
        FormBehaviour formBehaviour = formHelper.getFormBehaviour();
        if (null != formBehaviour) {
            // put all the form search plugins in an intent so we know what the user needs to search for
            Intent intent = new Intent(this, FormSearchActivity.class);
            intent.putExtra(FormSearchActivity.FORM_SEARCH_PLUGINS_KEY, formBehaviour.getFormSearchPluginModules().toArray());
            startActivityForResult(intent, SEARCH_ACTIVITY_REQUEST_CODE);
        }
    }

    private void showValueFragment() {
        // there is only 1 value fragment that can be added
        if (!valueFragment.isAdded()) {
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.middle_column, valueFragment, VALUE_FRAGMENT_TAG).commit();
            getFragmentManager().executePendingTransactions();

            valueFragment.populateData(currentResults);
        }
    }

    private void showDetailFragment() {
        // we don't check if it is added here because there are detail fragments for each state
        detailFragment = getDetailFragmentForCurrentState();
        detailFragment.setNavigateActivity(this);

        getFragmentManager()
                .beginTransaction()
                .replace(R.id.middle_column, detailFragment, DETAIL_FRAGMENT_TAG)
                .commit();
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
        if (null != detailFragsForStates.get(getState()) && !shouldShowDetailFragment()) {

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

        if (RESULT_OK == resultCode) {

            switch (requestCode) {
                case ODK_ACTIVITY_REQUEST_CODE:
                    // consume form data that the user entered with ODK
                    FormBehaviour formBehaviour = formHelper.getFormBehaviour();
                    formHelper.checkFormInstanceStatus();
                    if (null != formHelper.getFinalizedFormFilePath()) {
                        FormPayloadConsumer consumer = formBehaviour.getFormPayloadConsumer();
                        if (null != consumer) {
                            boolean needUpdate = consumer.consumeFormPayload(formHelper.getFormInstanceData(), this);
                            if (needUpdate) {
                                formHelper.updateExistingFormInstance();
                            }
                        }
                    }
                    break;

                case SEARCH_ACTIVITY_REQUEST_CODE:
                    // result intent contains the form fields and values that the user just search for
                    List<FormSearchPluginModule> formSearchPluginModules =
                            data.getParcelableArrayListExtra(FormSearchActivity.FORM_SEARCH_PLUGINS_KEY);

                    // merge searched fields with the existing form payload
                    for (FormSearchPluginModule plugin : formSearchPluginModules) {
                        formHelper.getFormFieldData().put(plugin.getFieldName(), plugin.getFieldValue());
                    }

                    // now let the user finish filling in the form in ODK
                    launchCurrentFormInODK();
                    break;
            }
        }

        // restore the appropriate UI in the middle column
        if(!shouldShowDetailFragment()) {
            showValueFragment();
        }

        // encrypt files regardless
        EncryptionHelper.encryptFiles(
                FormInstance.toListOfFiles(OdkCollectHelper.getAllFormInstances(getContentResolver())), this);
    }

    public DataWrapper getCurrentSelection() {
        return currentSelection;
    }

    @Override
    public void onBackPressed() {
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

    // TODO: not working??
    private void refreshHierarchy(String state){
        stateMachine.transitionTo(stateSequence.get(0));
        stateMachine.transitionTo(state);
    }

    // Respond when the navigation state machine changes state.
    private class HierarchyStateListener implements StateListener {

        @Override
        public void onEnterState() {
            String state = getState();
            updateButtonLabel(state);

            if (!state.equals(stateSequence.get(stateSequence.size() - 1))) {
                hierarchyButtonFragment.setButtonAllowed(state, true);
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
                valueFragment.populateData(currentResults);
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

    // Receive a value that the user clicked in ValueSelectionFragment.
    private class ValueSelectionHandler implements DataSelectionFragment.SelectionHandler {
        @Override
        public void handleSelectedData(DataWrapper dataWrapper) {
            NavigateActivity.this.stepDown(dataWrapper);
        }
    }

    // Receive a form that the user clicked in FormSelectionFragment.
    private class FormSelectionHandler implements FormSelectionFragment.SelectionHandler {
        @Override
        public void handleSelectedForm(FormBehaviour formBehaviour) {
            NavigateActivity.this.launchForm(formBehaviour);
        }
    }
}
