package org.openhds.mobile.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import org.openhds.mobile.R;
import org.openhds.mobile.fragment.FormInstanceReviewFragment;
import org.openhds.mobile.fragment.LoginPreferenceFragment;
import org.openhds.mobile.model.FormInstance;
import org.openhds.mobile.repository.GatewayRegistry;
import org.openhds.mobile.repository.gateway.Gateway;
import org.openhds.mobile.repository.search.FormSearchPluginModule;
import org.openhds.mobile.repository.search.SearchUtils;
import org.openhds.mobile.task.http.HttpTaskRequest;
import org.openhds.mobile.task.parsing.ParseEntityTaskRequest;
import org.openhds.mobile.task.parsing.entities.EntityParser;
import org.openhds.mobile.task.parsing.entities.FieldWorkerParser;
import org.openhds.mobile.task.parsing.entities.IndividualParser;
import org.openhds.mobile.task.parsing.entities.LocationHierarchyParser;
import org.openhds.mobile.task.parsing.entities.LocationParser;
import org.openhds.mobile.task.parsing.entities.MembershipParser;
import org.openhds.mobile.task.parsing.entities.RelationshipParser;
import org.openhds.mobile.task.parsing.entities.SocialGroupParser;
import org.openhds.mobile.utilities.EncryptionHelper;
import org.openhds.mobile.utilities.OdkCollectHelper;
import org.openhds.mobile.task.SyncDatabaseHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.openhds.mobile.utilities.ConfigUtils.getPreferenceString;
import static org.openhds.mobile.utilities.ConfigUtils.getResourceString;
import static org.openhds.mobile.utilities.LayoutUtils.makeButton;

public class SupervisorMainActivity extends Activity {

    private static final String REVIEW_FRAGMENT_TAG = "reviewFragment";
    private FrameLayout prefContainer;
    private LinearLayout supervisorButtonLayout;
    private FormInstanceReviewFragment reviewFragment;

    private SyncDatabaseHelper syncDatabaseHelper;
    private Map<HttpTaskRequest, ParseEntityTaskRequest> toBeSynced;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.supervisor_main);

        syncDatabaseHelper = new SyncDatabaseHelper(this);
        syncDatabaseHelper.setSyncCompleteListener(new DatabaseSyncListener());
        toBeSynced = new HashMap<>();

        prefContainer = (FrameLayout) findViewById(R.id.login_pref_container);
        supervisorButtonLayout = (LinearLayout) findViewById(R.id.supervisor_activity_options);

        ButtonClickListener buttonClickListener = new ButtonClickListener();
        makeButton(this,
                R.string.sync_database_description,
                R.string.sync_database_label,
                R.string.sync_database_label,
                buttonClickListener,
                supervisorButtonLayout);

        makeButton(this,
                R.string.sync_field_worker_description,
                R.string.sync_field_worker_label,
                R.string.sync_field_worker_label,
                buttonClickListener,
                supervisorButtonLayout);

        makeButton(this,
                R.string.search_database_description,
                R.string.search_database_label,
                R.string.search_database_label,
                buttonClickListener,
                supervisorButtonLayout);

        makeButton(this,
                R.string.send_finalized_forms_description,
                R.string.send_finalized_forms_label,
                R.string.send_finalized_forms_label,
                buttonClickListener,
                supervisorButtonLayout);

        if (null == savedInstanceState)  {
            reviewFragment = new FormInstanceReviewFragment();
            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.login_pref_container, new LoginPreferenceFragment())
                    .add(R.id.supervisor_edit_form_container, reviewFragment, REVIEW_FRAGMENT_TAG)
                    .commit();

        } else {
            reviewFragment = (FormInstanceReviewFragment) getFragmentManager().findFragmentByTag(REVIEW_FRAGMENT_TAG);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.supervisor_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Define what happens when a main menu item is selected.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.configure_server) {
            boolean isShowingPreferences = View.VISIBLE == prefContainer.getVisibility();
            boolean isPortraitMode = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
            if (isShowingPreferences) {
                if (isPortraitMode) {
                    supervisorButtonLayout.setVisibility(View.VISIBLE);
                }
                prefContainer.setVisibility(View.GONE);
            } else {
                if (isPortraitMode) {
                    supervisorButtonLayout.setVisibility(View.GONE);
                }
                prefContainer.setVisibility(View.VISIBLE);
            }
        } else if (item.getItemId() == R.id.logout_menu_button) {
            Intent intent = new Intent();
            intent.setClass(this, OpeningActivity.class);
            startActivity(intent);
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        encryptAllForms();
    }

    @Override
    protected void onPause() {
        super.onPause();
        syncDatabaseHelper.cancelSync();
    }

    private void encryptAllForms() {
        List<FormInstance> allFormInstances = OdkCollectHelper.getAllFormInstances(getContentResolver());
        if (null != allFormInstances) {
            EncryptionHelper.encryptFiles(FormInstance.toListOfFiles(allFormInstances), this);
        }
    }

    private void syncAllEntities() {
        setUpSyncTasks("Memberships", R.string.sync_memberships_path,
                new MembershipParser(), GatewayRegistry.getMembershipGateway());
        setUpSyncTasks("Relationships", R.string.sync_relationships_path,
                new RelationshipParser(), GatewayRegistry.getRelationshipGateway());
        setUpSyncTasks("Social Groups", R.string.sync_social_groups_path,
                new SocialGroupParser(), GatewayRegistry.getSocialGroupGateway());
        setUpSyncTasks("Visits", R.string.sync_social_groups_path,
                new SocialGroupParser(), GatewayRegistry.getSocialGroupGateway());
        setUpSyncTasks("Individuals", R.string.sync_individuals_path,
                new IndividualParser(), GatewayRegistry.getIndividualGateway());
        setUpSyncTasks("Location Hierarchies", R.string.sync_location_hierarchies_path,
                new LocationHierarchyParser(), GatewayRegistry.getLocationHierarchyGateway());
        setUpSyncTasks("Locations", R.string.sync_locations_path,
                new LocationParser(), GatewayRegistry.getLocationGateway());

        syncNext();
    }

    private void syncFieldWorkers() {
        setUpSyncTasks("Field Workers", R.string.sync_field_workers_path,
                new FieldWorkerParser(), GatewayRegistry.getFieldWorkerGateway());
        syncNext();
    }

    // Create tasks for syncing instances of entity type T.
    private <T> void setUpSyncTasks(String title, int resourcePathId, EntityParser<T> parser, Gateway<T> gateway) {
        String userName = (String) getIntent().getExtras().get(OpeningActivity.USERNAME_KEY);
        String password = (String) getIntent().getExtras().get(OpeningActivity.PASSWORD_KEY);
        String openHdsBaseUrl = getPreferenceString(this, R.string.openhds_server_url_key, "");

        String path = getResourceString(this, resourcePathId);
        String url = openHdsBaseUrl + path;
        HttpTaskRequest httpTaskRequest = new HttpTaskRequest(title, url, userName, password);

        ParseEntityTaskRequest<T> parseEntityTaskRequest = new ParseEntityTaskRequest<>(title, null, parser, gateway);

        toBeSynced.put(httpTaskRequest, parseEntityTaskRequest);
    }

    // Proceed to sync the next entity.
    private void syncNext() {
        if (toBeSynced.isEmpty()) {
            return;
        }

        HttpTaskRequest httpTaskRequest = null;
        ParseEntityTaskRequest<?> parseEntityTaskRequest = null;
        for (HttpTaskRequest key : toBeSynced.keySet()) {
            httpTaskRequest = key;
            parseEntityTaskRequest = toBeSynced.get(key);
        }
        toBeSynced.remove(httpTaskRequest);
        syncDatabaseHelper.startSync(httpTaskRequest, parseEntityTaskRequest);
    }

    private void searchDatabase() {
        ArrayList<FormSearchPluginModule> searchPluginModules = new ArrayList<>();
        searchPluginModules.add(SearchUtils.getFieldWorkerPlugin("fieldWorker"));
        searchPluginModules.add(SearchUtils.getIndividualPlugin("individual"));
        searchPluginModules.add(SearchUtils.getLocationPlugin("location"));
        searchPluginModules.add(SearchUtils.getSocialGroupPlugin("socialGroup"));

        Intent intent = new Intent(this, FormSearchActivity.class);
        intent.putParcelableArrayListExtra(FormSearchActivity.FORM_SEARCH_PLUGINS_KEY, searchPluginModules);
        startActivity(intent);
    }

    private class ButtonClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            Integer tag = (Integer) v.getTag();
            if (tag.equals(R.string.sync_database_label)) {
                syncAllEntities();
            } else if (tag.equals(R.string.sync_field_worker_label)) {
                syncFieldWorkers();
            } else if (tag.equals(R.string.search_database_label)) {
                searchDatabase();
            } else if (tag.equals(R.string.send_finalized_forms_label)) {
                reviewFragment.sendApprovedForms();
            }
        }
    }

    private class DatabaseSyncListener implements SyncDatabaseHelper.SyncCompleteListener {
        @Override
        public void onSyncComplete() {
            syncNext();
        }

        @Override
        public void onSyncError() {
            syncNext();
        }
    }
}
