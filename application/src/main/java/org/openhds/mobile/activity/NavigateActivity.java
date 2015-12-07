package org.openhds.mobile.activity;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import org.openhds.mobile.R;
import org.openhds.mobile.forms.FormBehaviour;
import org.openhds.mobile.forms.FormContent;
import org.openhds.mobile.forms.FormDefinition;
import org.openhds.mobile.forms.FormInstance;
import org.openhds.mobile.forms.odk.OdkInstanceGateway;
import org.openhds.mobile.fragment.DataSelectionFragment;
import org.openhds.mobile.fragment.FieldWorkerLoginFragment;
import org.openhds.mobile.fragment.FormSelectionFragment;
import org.openhds.mobile.fragment.navigate.HierarchyButtonFragment;
import org.openhds.mobile.fragment.navigate.VisitFragment;
import org.openhds.mobile.model.core.FieldWorker;
import org.openhds.mobile.model.update.Visit;
import org.openhds.mobile.modules.ModuleAppearance;
import org.openhds.mobile.modules.ModuleRegistry;
import org.openhds.mobile.modules.NavigationModule;
import org.openhds.mobile.repository.DataWrapper;
import org.openhds.mobile.repository.GatewayRegistry;
import org.openhds.mobile.repository.gateway.Gateway;
import org.openhds.mobile.repository.search.FormSearchPluginModule;
import org.openhds.mobile.task.odk.FormsFromOdkTask;
import org.openhds.mobile.utilities.ConfigUtils;
import org.openhds.mobile.utilities.DateUtils;
import org.openhds.mobile.utilities.EncryptionHelper;
import org.openhds.mobile.utilities.StateMachine;
import org.openhds.mobile.utilities.StateMachine.StateListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.openhds.mobile.utilities.MessageUtils.showShortToast;

public class NavigateActivity extends Activity implements HierarchyNavigator {

    private static final int ODK_ACTIVITY_REQUEST_CODE = 42;
    private static final int SEARCH_ACTIVITY_REQUEST_CODE = 43;

    private HierarchyButtonFragment hierarchyButtonFragment;
    private DataSelectionFragment valueFragment;
    private FormSelectionFragment formFragment;
    private VisitFragment visitFragment;

    private static final String HIERARCHY_BUTTON_FRAGMENT_TAG = HierarchyButtonFragment.class.getSimpleName();
    private static final String VALUE_FRAGMENT_TAG = DataSelectionFragment.class.getSimpleName();
    private static final String FORM_FRAGMENT_TAG = FormSelectionFragment.class.getSimpleName();
    private static final String VISIT_FRAGMENT_TAG = VisitFragment.class.getSimpleName();

    private static final String VISIT_KEY = "visitKey";
    private static final String HIERARCHY_PATH_KEYS = "hierarchyPathKeys";
    private static final String HIERARCHY_PATH_VALUES = "hierarchyPathValues";
    private static final String CURRENT_RESULTS_KEY = "currentResults";

    private NavigationModule navigationModule;
    private HashMap<MenuItem, String> menuItemTags;

    private StateMachine stateMachine;
    private Map<String, DataWrapper> hierarchyPath;
    private List<DataWrapper> currentResults;
    private DataWrapper currentSelection;
    private FieldWorker currentFieldWorker;
    private Visit currentVisit;
    private Map<String, List<FormBehaviour>> formBehaviors = new HashMap<>();
    private FormInstance currentFormInstance;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navigate_activity);

        FieldWorker fieldWorker = (FieldWorker) getIntent().getExtras().get(FieldWorkerLoginFragment.FIELD_WORKER_EXTRA);
        setCurrentFieldWorker(fieldWorker);

        String currentModuleName = (String) getIntent().getExtras().get(ModuleRegistry.MODULE_NAME_EXTRA_KEY);
        navigationModule = ModuleRegistry.getModuleByName(currentModuleName);
        if (null != navigationModule) {
            navigationModule.getModuleHierarchy().init(this);
        }

        hierarchyPath = new HashMap<>();
        stateMachine = new StateMachine(new HashSet<>(getLevelSequence()), getLevelSequence().get(0));

        for (String level : getLevelSequence()) {
            stateMachine.registerListener(level, new HierarchyStateListener());
        }

        if (null == savedInstanceState) {

            if (null != getIntent().getStringArrayListExtra(HIERARCHY_PATH_KEYS)) {
                ArrayList<String> hierarchyPathKeys = getIntent().getStringArrayListExtra(HIERARCHY_PATH_KEYS);
                for (String key : hierarchyPathKeys) {
                    hierarchyPath.put(key, (DataWrapper) getIntent().getParcelableExtra(key + HIERARCHY_PATH_VALUES));
                }
                currentResults = getIntent().getParcelableArrayListExtra(CURRENT_RESULTS_KEY);
            }

            //fresh activity
            hierarchyButtonFragment = new HierarchyButtonFragment();
            hierarchyButtonFragment.setNavigator(this);
            valueFragment = new DataSelectionFragment();
            valueFragment.setSelectionHandler(new ValueSelectionHandler());
            formFragment = new FormSelectionFragment();
            formFragment.setSelectionHandler(new FormSelectionHandler());
            visitFragment = new VisitFragment();
            visitFragment.setNavigateActivity(this);

            getFragmentManager().beginTransaction()
                    .add(R.id.left_column_top, hierarchyButtonFragment, HIERARCHY_BUTTON_FRAGMENT_TAG)
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
            visitFragment = (VisitFragment) fragmentManager.findFragmentByTag(VISIT_FRAGMENT_TAG);
            visitFragment.setNavigateActivity(this);
            valueFragment = (DataSelectionFragment) fragmentManager.findFragmentByTag(VALUE_FRAGMENT_TAG);

            // draw details if valuefrag is null, the drawing of valuefrag is
            // handled in onResume().
            if (null == valueFragment) {
                valueFragment = new DataSelectionFragment();
            }
            valueFragment.setSelectionHandler(new ValueSelectionHandler());

            ArrayList<String> hierarchyPathKeys = savedInstanceState.getStringArrayList(HIERARCHY_PATH_KEYS);
            for (String key : hierarchyPathKeys) {
                hierarchyPath.put(key, (DataWrapper) savedInstanceState.getParcelable(key + HIERARCHY_PATH_VALUES));
            }
            currentResults = savedInstanceState.getParcelableArrayList(CURRENT_RESULTS_KEY);
            setCurrentVisit((Visit) savedInstanceState.get(VISIT_KEY));
        }

        setActivityVisualTheme(navigationModule.getModuleAppearance());
        queryFormDefinitions();
    }

    // Takes in a NavigationModule's ModuleAppearance and sets all the fragment's drawables
    private void setActivityVisualTheme(ModuleAppearance moduleAppearance) {
        setTitle(getString(moduleAppearance.getTitleId()));
        hierarchyButtonFragment.setHiearchySelectionDrawableId(moduleAppearance.getHierarchySelectionDrawableId());
        valueFragment.setDataSelectionDrawableId(moduleAppearance.getDataSelectionDrawableId());
        formFragment.setFormSelectionDrawableId(moduleAppearance.getFormSelectionDrawableId());
        View middleColumn = findViewById(R.id.middle_column);
        middleColumn.setBackgroundResource(moduleAppearance.getMiddleColumnDrawableId());
    }

    // Ask Odk Collect what form definitions are installed.
    private void queryFormDefinitions() {
        final FormsFromOdkTask.Listener listener = new FormsFromOdkTask.Listener() {
            @Override
            public void onComplete(List<FormDefinition> formDefinitions) {
                parseFormBehaviors(formDefinitions);
            }
        };
        FormsFromOdkTask formsFromOdkTask = new FormsFromOdkTask(listener);
        formsFromOdkTask.execute(NavigateActivity.this);
    }

    // Parse form behaviors and cache for easy lookup.
    private void parseFormBehaviors(List<FormDefinition> formDefinitions) {
        formBehaviors.clear();
        for (FormDefinition formDefinition : formDefinitions) {

            // parse behavior out of form definition
            FormBehaviour formBehaviour = new FormBehaviour((formDefinition));
            if (!formBehaviour.parseMetadata()) {
                continue;
            }

            // cache behaviors, indexed by level (null == any level)
            List<String> levels = formBehaviour.getDisplayLevels();
            if (levels.isEmpty()) {
                // add to all levels
                if (!formBehaviors.containsKey(null)) {
                    formBehaviors.put(null, new ArrayList<FormBehaviour>());
                }
                formBehaviors.get(null).add(formBehaviour);

            } else {
                // add behavior to each named level
                for (String level : levels) {
                    if (!formBehaviors.containsKey(level)) {
                        formBehaviors.put(level, new ArrayList<FormBehaviour>());
                    }
                    formBehaviors.get(level).add(formBehaviour);
                }
            }
        }
    }

    // Which forms should be displayed at this level?
    private List<FormBehaviour> getActiveFormBehaviors(String level) {
        List<FormBehaviour> activeForms = new ArrayList<>();

        // the always-on default forms
        if (formBehaviors.containsKey(null)) {
            activeForms.addAll(formBehaviors.get(null));
        }

        // the forms for the current hierarchy level
        if (formBehaviors.containsKey(level)) {
            activeForms.addAll(formBehaviors.get(level));
        }

        return activeForms;
    }

    @Override
    protected void onResume() {
        super.onResume();
        hierarchySetup();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        ArrayList<String> hierarchyPathKeys = new ArrayList<String>(hierarchyPath.keySet());
        for (String key : hierarchyPathKeys) {
            savedInstanceState.putParcelable(key + HIERARCHY_PATH_VALUES, hierarchyPath.get(key));
        }
        savedInstanceState.putStringArrayList(HIERARCHY_PATH_KEYS, hierarchyPathKeys);

        savedInstanceState.putParcelableArrayList(CURRENT_RESULTS_KEY, (ArrayList<DataWrapper>) currentResults);
        savedInstanceState.putSerializable(VISIT_KEY, getCurrentVisit());
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar_menu, menu);

        // MenuItems do not have their own tags, so I am using a map as a substitute. This map uses the MenuItem itself
        // as a key and the moduleName.
        menuItemTags = new HashMap<>();

        // get the list of modules to iterate through
        List<String> modulesList = ModuleRegistry.getModuleNames();

        // reference to the current hierarchy's name
        String currentHierarchy = navigationModule.getModuleHierarchy().getName();

        // for each navigationModule
        for (String moduleName : modulesList) {

            // keep a reference to make the code readable
            NavigationModule module = ModuleRegistry.getModuleByName(moduleName);

            if (null == module) {
                continue;
            }

            // if that navigationModule has the same hierarchy as the current navigationModule
            if (module.getModuleHierarchy().getName().equals(currentHierarchy) && !moduleName.equals(navigationModule.getName())) {

                // keep a reference to make the code readable
                ModuleAppearance appearance = module.getModuleAppearance();

                // add a menuItem for the navigationModule, set its UI, give it a tag for OnClick
                MenuItem menuItem = menu.add(appearance.getTitleId());
                menuItem.setIcon(appearance.getPortalDrawableId());
                menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
                menuItemTags.put(menuItem, moduleName);

            }

        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Intent intent = new Intent();

        if (item.getItemId() == R.id.logout_menu_button) {
            intent.setClass(this, OpeningActivity.class);
            startActivity(intent);
            return true;
        }
        if (item.getItemId() == R.id.field_worker_home_menu_button) {
            intent.setClass(this, PortalActivity.class);
            intent.putExtra(FieldWorkerLoginFragment.FIELD_WORKER_EXTRA, getCurrentFieldWorker());
            startActivity(intent);
            return true;
        }

        if (null != menuItemTags.get(item)) {
            intent.setClass(this, NavigateActivity.class);
            intent.putExtra(FieldWorkerLoginFragment.FIELD_WORKER_EXTRA, getCurrentFieldWorker());
            intent.putExtra(ModuleRegistry.MODULE_NAME_EXTRA_KEY, menuItemTags.get(item));
            intent.putParcelableArrayListExtra(CURRENT_RESULTS_KEY, (ArrayList<DataWrapper>) currentResults);

            ArrayList<String> hierarchyPathKeys = new ArrayList<String>(hierarchyPath.keySet());
            for (String key : hierarchyPathKeys) {
                intent.putExtra(key + HIERARCHY_PATH_VALUES, hierarchyPath.get(key));
            }
            intent.putStringArrayListExtra(HIERARCHY_PATH_KEYS, hierarchyPathKeys);

            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);

    }

    private void hierarchySetup() {
        int levelIndex = 0;
        for (String level : getLevelSequence()) {
            if (hierarchyPath.containsKey(level)) {
                updateButtonLabel(level);
                hierarchyButtonFragment.setButtonAllowed(level, true);
                levelIndex++;
            } else {
                break;
            }
        }

        String level = getLevelSequence().get(levelIndex);
        if (0 == levelIndex) {
            hierarchyButtonFragment.setButtonAllowed(level, true);
            currentResults = navigationModule.getModuleHierarchy().getAll(getContentResolver(), getLevelSequence().get(0));

        } else {
            String previousLevel = getLevelSequence().get(levelIndex - 1);
            DataWrapper previousSelection = hierarchyPath.get(previousLevel);
            currentSelection = previousSelection;
            if (null == currentResults) {
                currentResults = navigationModule.getModuleHierarchy().getChildren(getContentResolver(), previousSelection);
            }
        }

        boolean isAdded = valueFragment.isAdded();

        // make sure that listeners will fire for the current level
        refreshHierarchy(level);
        showValueFragment();

        if (null != getCurrentVisit()) {
            visitFragment.setButtonEnabled(true);
        }
    }

    public Map<String, DataWrapper> getHierarchyPath() {
        return hierarchyPath;
    }

    public List<DataWrapper> getCurrentResults() {
        return currentResults;
    }

    public String getLevel() {
        return stateMachine.getState();
    }

    private void updateButtonLabel(String level) {

        DataWrapper selected = hierarchyPath.get(level);
        if (null == selected) {
            String label = getLevelLabels().get(level);
            hierarchyButtonFragment.setButtonLabel(level, label, null, true);
            hierarchyButtonFragment.setButtonHighlighted(level, true);
        } else {
            hierarchyButtonFragment.setButtonLabel(level, selected.getName(), selected.getExtId(), false);
            hierarchyButtonFragment.setButtonHighlighted(level, false);
        }
    }

    @Override
    public Map<String, String> getLevelLabels() {
        return navigationModule.getModuleHierarchy().getLevelLabels();
    }

    @Override
    public List<String> getLevelSequence() {
        return navigationModule.getModuleHierarchy().getLevelSequence();
    }

    @Override
    public void jumpUp(String targetLevel) {
        int targetIndex = getLevelSequence().indexOf(targetLevel);
        if (targetIndex < 0) {
            throw new IllegalStateException("Target level <" + targetLevel + "> is not a valid level");
        }

        String currentLevel = getLevel();
        int currentIndex = getLevelSequence().indexOf(currentLevel);
        if (targetIndex >= currentIndex) {
            // use stepDown() to go down the hierarchy
            return;
        }

        // un-traverse the hierarchy up to the target level
        for (int i = currentIndex; i >= targetIndex; i--) {
            String level = getLevelSequence().get(i);
            hierarchyButtonFragment.setButtonAllowed(level, false);
            hierarchyPath.remove(level);

        }

        // prepare to stepDown() from this target level
        if (0 == targetIndex) {
            // root of the hierarchy
            currentResults = navigationModule.getModuleHierarchy().getAll(getContentResolver(), getLevelSequence().get(0));
        } else {
            // middle of the hierarchy
            String previousLevel = getLevelSequence().get(targetIndex - 1);
            DataWrapper previousSelection = hierarchyPath.get(previousLevel);
            currentSelection = previousSelection;
            currentResults = navigationModule.getModuleHierarchy().getChildren(getContentResolver(), previousSelection);
        }
        stateMachine.transitionTo(targetLevel);
    }

    @Override
    public void stepDown(DataWrapper selected) {
        String currentLevel = getLevel();

        if (!currentLevel.equals(selected.getLevel())) {
            throw new IllegalStateException("Selected level <"
                    + selected.getLevel() + "> mismatch with current level <"
                    + currentLevel + ">");
        }

        int currentIndex = getLevelSequence().indexOf(currentLevel);
        if (currentIndex >= 0 && currentIndex < getLevelSequence().size() - 1) {
            String nextLevel = getLevelSequence().get(currentIndex + 1);

            currentSelection = selected;
            currentResults = navigationModule.getModuleHierarchy().getChildren(getContentResolver(), selected);

            hierarchyPath.put(currentLevel, selected);
            stateMachine.transitionTo(nextLevel);
        }
    }

    @Override
    public void launchForm(FormBehaviour formBehaviour) {

        FormInstance formInstance = OdkInstanceGateway.instantiateFormDefinition(formBehaviour.getFormDefinition());
        if (null == formInstance) {
            showShortToast(this, "Error instantiating form instance of type: " + formBehaviour.getFormDefinition().getId());
            return;
        }

        formInstance = OdkInstanceGateway.registerOrUpdateInstance(getContentResolver(), formInstance);
        if (null == formInstance) {
            showShortToast(this, "Error registering form instance of type: " + formBehaviour.getFormDefinition().getId());
            return;
        }

        formInstance.setFormBehaviour(formBehaviour);

        currentFormInstance = formInstance;

        // if needed, ask the user to search for required form field data
        List<FormSearchPluginModule> searchPlugins = formBehaviour.getSearchPlugins();
        if (!searchPlugins.isEmpty()) {
            launchFormSearchActivity(searchPlugins);
            return;
        }

        launchCurrentFormInODK(null);
    }

    private void launchCurrentFormInODK(FormContent previousContent) {
        if (null == currentFormInstance || null == currentFormInstance.getFormBehaviour()) {
            return;
        }

        // sanity check the form version
        String formVersion = currentFormInstance.getVersion();
        if (null == formVersion) {
            showShortToast(this, "Warning: Form has no defined version number.");
            return;
        }

        String appVersionNumber = Integer.toString(ConfigUtils.getAppVersionNumber(this));
        if (!formVersion.equals(appVersionNumber)) {
            showShortToast(this, "Warning: Version difference between OpenHDS ("
                    + appVersionNumber
                    + ") and ODK ("
                    + formVersion
                    + ").");
        }

        // auto-fill form fields based on field worker and hierarchy navigation
        FormContent navigationContent = buildNavigationContent();
        if (null != previousContent) {
            navigationContent.addAll(previousContent);
        }
        navigationContent.updateFormContent(new File(currentFormInstance.getFilePath()));

        // clear currentResults to get the most up-to-date currentResults after the form is consumed
        currentResults = null;

        // ask ODK Collect to edit this form instance
        Intent intent = OdkInstanceGateway.buildEditFormInstanceIntent(currentFormInstance.getUri());
        showShortToast(this, R.string.launching_odk_collect);
        startActivityForResult(intent, ODK_ACTIVITY_REQUEST_CODE);
    }

    private void launchFormSearchActivity(List<FormSearchPluginModule> searchPlugins) {
        // put form search plugins in the intent so we know what the user needs to search for
        Intent intent = new Intent(this, FormSearchActivity.class);
        ArrayList<FormSearchPluginModule> asArrayList = new ArrayList<>(searchPlugins.size());
        asArrayList.addAll(searchPlugins);
        intent.putParcelableArrayListExtra(FormSearchActivity.FORM_SEARCH_PLUGINS_KEY, asArrayList);
        startActivityForResult(intent, SEARCH_ACTIVITY_REQUEST_CODE);
    }

    private void showValueFragment() {
        if (!valueFragment.isAdded()) {
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.middle_column, valueFragment, VALUE_FRAGMENT_TAG).commit();
            getFragmentManager().executePendingTransactions();
        }
        valueFragment.populateData(currentResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (RESULT_OK == resultCode) {

            switch (requestCode) {
                case ODK_ACTIVITY_REQUEST_CODE: {
                    // consume form data that the user entered with ODK
                    FormContent formContent = FormContent.readFormContent(new File(currentFormInstance.getFilePath()));

                    // done reading, encrypt forms
                    List<FormInstance> allFormInstances = OdkInstanceGateway.findAllInstances(getContentResolver());
                    if (null != allFormInstances) {
                        EncryptionHelper.encryptFiles(FormInstance.toListOfFiles(allFormInstances), this);
                    }

                    // consume form records into database
                    List<String> consumers = currentFormInstance.getFormBehaviour().getConsumers();
                    if (!consumers.isEmpty()) {
                        FormContent entityContent = buildNavigationContent();
                        entityContent.addAll(formContent);

                        for (String consumer : consumers) {
                            Gateway<?> gateway = GatewayRegistry.getGatewayByEntityName(consumer);
                            if (null != gateway) {
                                showShortToast(this, getString(R.string.consuming_form_record) + ": " + consumer);
                                gateway.insertOrUpdate(getContentResolver(), entityContent);
                            }
                        }
                    }

                    // TODO: check whether we should launch a follow-up form

                    return;
                }

                case SEARCH_ACTIVITY_REQUEST_CODE: {
                    // put search results into form content
                    List<FormSearchPluginModule> formSearchPluginModules =
                            data.getParcelableArrayListExtra(FormSearchActivity.FORM_SEARCH_PLUGINS_KEY);

                    FormContent formContent = new FormContent();
                    for (FormSearchPluginModule plugin : formSearchPluginModules) {
                        DataWrapper dataWrapper = plugin.getDataWrapper();
                        addContentAliases(formContent, dataWrapper);
                    }

                    // now let the user finish filling in the form in ODK
                    launchCurrentFormInODK(formContent);
                    return;
                }
            }
        }
    }

    public DataWrapper getCurrentSelection() {
        return currentSelection;
    }

    private FormContent buildNavigationContent() {
        FormContent formContent = new FormContent();

        formContent.setContent(FormContent.TOP_LEVEL_ALIAS, "registrationDateTime", DateUtils.formatDateTimeIso(Calendar.getInstance()));

        if (null != currentFieldWorker) {
            formContent.setContent(FormContent.TOP_LEVEL_ALIAS, "collectedByUuid", currentFieldWorker.getUuid());
            formContent.setContent(FormContent.TOP_LEVEL_ALIAS, "collectedByExtId", currentFieldWorker.getFieldWorkerId());
        }

        if (null != currentVisit) {
            formContent.setContent(FormContent.TOP_LEVEL_ALIAS, "visitUuid", currentVisit.getUuid());
            formContent.setContent(FormContent.TOP_LEVEL_ALIAS, "visitExtId", currentVisit.getExtId());
        }

        // add each hierarchy path item under multiple aliases to aid form field matching
        // add in navigation order so that lower selections replace higher ones
        for (String level : navigationModule.getModuleHierarchy().getLevelSequence()) {
            if (hierarchyPath.containsKey(level)) {
                DataWrapper dataWrapper = hierarchyPath.get(level);
                addContentAliases(formContent, dataWrapper);
            }
        }

        return formContent;
    }

    private void addContentAliases(FormContent formContent, DataWrapper dataWrapper) {
        formContent.setContent(dataWrapper.getName(), dataWrapper.getContentValues());
        formContent.setContent(dataWrapper.getLevel(), dataWrapper.getContentValues());
        formContent.setContent(dataWrapper.getClassName(), dataWrapper.getContentValues());
        formContent.setContent("parent", dataWrapper.getContentValues());
    }

    @Override
    public void onBackPressed() {
        int currentLevelIndex;
        if (0 < (currentLevelIndex = getLevelSequence().indexOf(getLevel()))) {
            jumpUp(getLevelSequence().get(currentLevelIndex - 1));
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
        refreshHierarchy(getLevel());
    }

    private void refreshHierarchy(String level) {
        stateMachine.transitionTo(getLevelSequence().get(0));
        stateMachine.transitionTo(level);
    }

    // Respond when the navigation state machine changes state.
    private class HierarchyStateListener implements StateListener {

        @Override
        public void onEnterState() {
            String level = getLevel();
            updateButtonLabel(level);

            if (!level.equals(getLevelSequence().get(getLevelSequence().size() - 1))) {
                hierarchyButtonFragment.setButtonAllowed(level, true);
            }

            showValueFragment();
            formFragment.createFormButtons(getActiveFormBehaviors(level));
        }

        @Override
        public void onExitState() {
            String level = getLevel();
            updateButtonLabel(level);
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
