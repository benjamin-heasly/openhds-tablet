package org.openhds.mobile.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.R;
import org.openhds.mobile.adapter.FormInstanceAdapter;
import org.openhds.mobile.fragment.FieldWorkerLoginFragment;
import org.openhds.mobile.fragment.SearchFragment;
import org.openhds.mobile.model.FieldWorker;
import org.openhds.mobile.model.FormInstance;
import org.openhds.mobile.projectdata.ProjectActivityBuilder;
import org.openhds.mobile.repository.GatewayRegistry;
import org.openhds.mobile.repository.QueryResult;
import org.openhds.mobile.repository.search.SearchPluginModule;
import org.openhds.mobile.utilities.EncryptionHelper;
import org.openhds.mobile.utilities.OdkCollectHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.openhds.mobile.utilities.LayoutUtils.makeTextWithPayload;
import static org.openhds.mobile.utilities.MessageUtils.showLongToast;

public class PortalActivity extends Activity implements OnClickListener {

    private static final String SEARCH_FRAGMENT_TAG = "searchFragment";

    private FieldWorker currentFieldWorker;

    private SearchFragment searchFragment;
    private ListView formInstanceView;
    private List<FormInstance> formInstances;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // basic view setup
        setContentView(R.layout.portal_activity);
        setTitle(this.getResources().getString(R.string.field_worker_home_menu_text));

        // who logged in?
        currentFieldWorker = (FieldWorker) getIntent().getExtras().get(FieldWorkerLoginFragment.FIELD_WORKER_EXTRA);

        // put a sample search fragment in the left column for testing
        // TODO: remove this because it's just for testing
        if (null == savedInstanceState) {
            searchFragment = new SearchFragment();
            getFragmentManager().beginTransaction()
                    .add(R.id.portal_left_column, searchFragment, SEARCH_FRAGMENT_TAG)
                    .commit();
        } else {
            searchFragment = (SearchFragment) getFragmentManager().findFragmentByTag(SEARCH_FRAGMENT_TAG);
        }

        // fill the middle column with a button for each available activity
        LinearLayout activitiesLayout = (LinearLayout) findViewById(R.id.portal_middle_column);
        List<String> activityModuleNames = ProjectActivityBuilder.getActivityModuleNames();
        for (String name : activityModuleNames) {
            RelativeLayout layout = makeTextWithPayload(this,
                    getString(ProjectActivityBuilder.getModuleInfoByName(name).getModuleLabelStringId()),
                    getString(ProjectActivityBuilder.getModuleInfoByName(name).getModuleDescriptionStringId()),
                    name, this, activitiesLayout,
                    ProjectActivityBuilder.getModuleInfoByName(name).getModuleColorId(), null, null);

            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) layout.getLayoutParams();
            params.setMargins(0, 0, 0, 20);
        }

        // fill the right column with a list of recent form instances
        formInstanceView = (ListView) findViewById(R.id.portal_right_column);
        TextView header = (TextView) this.getLayoutInflater().inflate(R.layout.generic_header, null);
        header.setText(R.string.form_instance_list_header);
        header.setVisibility(View.VISIBLE);
        formInstanceView.addHeaderView(header);
        populateFormInstanceListView();
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this, NavigateActivity.class);

        intent.putExtra(FieldWorkerLoginFragment.FIELD_WORKER_EXTRA, currentFieldWorker);

        String activityName = (String) v.getTag();
        intent.putExtra(ProjectActivityBuilder.ACTIVITY_MODULE_EXTRA, activityName);

        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        populateSearchFragment();
        populateFormInstanceListView();
    }

    // Allow a few searches for testing
    // TODO: remove this because it's just for testing
    private void populateSearchFragment() {
        List<SearchPluginModule> searchPluginModules = new ArrayList<>();

        SearchPluginModule individualPluginModule = new SearchPluginModule(GatewayRegistry.getIndividualGateway(), R.string.individual_label);
        individualPluginModule.getColumnsAndLabels().put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_FIRST_NAME, R.string.individual_name_label);
        individualPluginModule.getColumnsAndLabels().put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_PHONE_NUMBER, R.string.individual_personal_phone_number_label);
        individualPluginModule.getColumnsAndLabels().put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_AGE, R.string.individual_age_label);
        searchPluginModules.add(individualPluginModule);

        SearchPluginModule fieldWorkerPluginModule = new SearchPluginModule(GatewayRegistry.getFieldWorkerGateway(), R.string.fieldworker_login);
        fieldWorkerPluginModule.getColumnsAndLabels().put(OpenHDS.FieldWorkers.COLUMN_FIELD_WORKER_FIRST_NAME, R.string.individual_name_label);
        fieldWorkerPluginModule.getColumnsAndLabels().put(OpenHDS.FieldWorkers.COLUMN_FIELD_WORKER_EXTID, R.string.individual_dip_label);
        searchPluginModules.add(fieldWorkerPluginModule);

        SearchPluginModule locationPluginModule = new SearchPluginModule(GatewayRegistry.getLocationGateway(), R.string.location_lbl);
        locationPluginModule.getColumnsAndLabels().put(OpenHDS.Locations.COLUMN_LOCATION_EXTID, R.string.household_head_extid);
        locationPluginModule.getColumnsAndLabels().put(OpenHDS.Locations.COLUMN_LOCATION_NAME, R.string.individual_name_label);
        locationPluginModule.getColumnsAndLabels().put(OpenHDS.Locations.COLUMN_LOCATION_COMMUNITY_NAME, R.string.locality_label);
        locationPluginModule.getColumnsAndLabels().put(OpenHDS.Locations.COLUMN_LOCATION_FLOOR_NUMBER, R.string.individual_personal_phone_number_label);
        locationPluginModule.getColumnsAndLabels().put(OpenHDS.Locations.COLUMN_LOCATION_BUILDING_NUMBER, R.string.individual_other_phone_number_label);
        locationPluginModule.getColumnsAndLabels().put(OpenHDS.Locations.COLUMN_LOCATION_MAP_AREA_NAME, R.string.map_area_label);
        locationPluginModule.getColumnsAndLabels().put(OpenHDS.Locations.COLUMN_LOCATION_SECTOR_NAME, R.string.sector_label);
        searchPluginModules.add(locationPluginModule);

        searchFragment.setSearchPluginModules(searchPluginModules);
        searchFragment.setTitle(R.string.search_lbl);
        searchFragment.setResultsHandler(new SearchResultsHandler());
    }

    // Display a list of recent form instances not yet sent to the ODK server
    private void populateFormInstanceListView() {
        formInstances = OdkCollectHelper.getAllUnsentFormInstances(getContentResolver());
        if (!formInstances.isEmpty()) {
            FormInstanceAdapter adapter = new FormInstanceAdapter(
                    this, R.id.form_instance_list_item, formInstances.toArray());
            formInstanceView.setAdapter(adapter);
            formInstanceView.setOnItemClickListener(new FormInstanceClickListener());
        }
    }

    // Launch an intent for ODK Collect when user clicks on a form instance.
    private class FormInstanceClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            FormInstance selected = formInstances.get(position - 1);
            Uri uri = Uri.parse(selected.getUriString());

            File selectedFile = new File(selected.getFilePath());
            EncryptionHelper.decryptFile(selectedFile, getApplicationContext());

            Intent intent = new Intent(Intent.ACTION_EDIT, uri);
            startActivityForResult(intent, 0);
        }
    }

    // Receive search results from the search fragment.
    private class SearchResultsHandler implements SearchFragment.ResultsHandler {
        @Override
        public void handleSearchResults(List<QueryResult> queryResults) {
            showLongToast(PortalActivity.this, "Found " + queryResults.size() + " results.");
        }
    }
}
