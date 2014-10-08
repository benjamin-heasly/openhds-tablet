package org.openhds.mobile.activity;

import android.app.Activity;
import android.app.ProgressDialog;
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
import org.openhds.mobile.model.FieldWorker;
import org.openhds.mobile.model.FormInstance;
import org.openhds.mobile.repository.GatewayRegistry;
import org.openhds.mobile.repository.search.FormSearchPluginModule;
import org.openhds.mobile.repository.search.SearchUtils;
import org.openhds.mobile.task.http.HttpTaskRequest;
import org.openhds.mobile.task.parsing.ParseEntityTaskRequest;
import org.openhds.mobile.task.parsing.entities.FieldWorkerParser;
import org.openhds.mobile.utilities.EncryptionHelper;
import org.openhds.mobile.utilities.OdkCollectHelper;
import org.openhds.mobile.utilities.SyncDatabaseHelper;

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

    private ProgressDialog progressDialog;

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

        progressDialog = new ProgressDialog(this);
        syncDatabaseHelper.setProgressDialog(progressDialog);

        encryptAllForms();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (null != progressDialog) {
            progressDialog.dismiss();
            progressDialog = null;
        }
        syncDatabaseHelper.setProgressDialog(null);
    }

    private void encryptAllForms() {
        List<FormInstance> allFormInstances = OdkCollectHelper.getAllFormInstances(getContentResolver());
        if (null != allFormInstances) {
            EncryptionHelper.encryptFiles(FormInstance.toListOfFiles(allFormInstances), this);
        }
    }

    private void syncAllEntities() {
        String username = (String) getIntent().getExtras().get(OpeningActivity.USERNAME_KEY);
        String password = (String) getIntent().getExtras().get(OpeningActivity.PASSWORD_KEY);
        String openHdsBaseUrl = getPreferenceString(this, R.string.openhds_server_url_key, "");
    }

    private void syncFieldWorkers() {
        String username = (String) getIntent().getExtras().get(OpeningActivity.USERNAME_KEY);
        String password = (String) getIntent().getExtras().get(OpeningActivity.PASSWORD_KEY);

        String baseUrl = getResourceString(this, R.string.default_openhds_server_url);
        String path = getResourceString(this, R.string.field_workers_sync_url);
        String url = baseUrl + path;

        HttpTaskRequest httpTaskRequest = new HttpTaskRequest("Field Workers", url, username, password);
        ParseEntityTaskRequest<FieldWorker> parseEntityTaskRequest = new ParseEntityTaskRequest<>(
                "Field Workers", null, new FieldWorkerParser(), GatewayRegistry.getFieldWorkerGateway());
        toBeSynced.put(httpTaskRequest, parseEntityTaskRequest);
        syncNext();
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
