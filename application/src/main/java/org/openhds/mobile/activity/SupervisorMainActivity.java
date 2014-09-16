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
import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.R;
import org.openhds.mobile.fragment.FormInstanceReviewFragment;
import org.openhds.mobile.fragment.LoginPreferenceFragment;
import org.openhds.mobile.model.FormInstance;
import org.openhds.mobile.repository.GatewayRegistry;
import org.openhds.mobile.repository.search.FormSearchPluginModule;
import org.openhds.mobile.task.HttpTask.RequestContext;
import org.openhds.mobile.task.SyncEntitiesTask;
import org.openhds.mobile.task.SyncFieldworkersTask;
import org.openhds.mobile.utilities.EncryptionHelper;
import org.openhds.mobile.utilities.OdkCollectHelper;
import org.openhds.mobile.utilities.SyncDatabaseHelper;

import java.util.ArrayList;

import static org.openhds.mobile.utilities.ConfigUtils.getPreferenceString;
import static org.openhds.mobile.utilities.ConfigUtils.getResourceString;
import static org.openhds.mobile.utilities.LayoutUtils.makeButton;
import static org.openhds.mobile.utilities.UrlUtils.buildServerUrl;

public class SupervisorMainActivity extends Activity implements OnClickListener {

    private static final String REVIEW_FRAGMENT_TAG = "reviewFragment";
    private FrameLayout prefContainer;
    private LinearLayout supervisorButtonLayout;
    private SyncDatabaseHelper syncDatabaseHelper;
    private FormInstanceReviewFragment reviewFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.supervisor_main);

        prefContainer = (FrameLayout) findViewById(R.id.login_pref_container);
        supervisorButtonLayout = (LinearLayout) findViewById(R.id.supervisor_activity_options);
        syncDatabaseHelper = new SyncDatabaseHelper(this);

        makeButton(this,
                R.string.sync_database_description,
                R.string.sync_database_label,
                R.string.sync_database_label,
                this, supervisorButtonLayout);

        makeButton(this,
                R.string.sync_field_worker_description,
                R.string.sync_field_worker_label,
                R.string.sync_field_worker_label,
                this, supervisorButtonLayout);

        makeButton(this,
                R.string.search_database_description,
                R.string.search_database_label,
                R.string.search_database_label,
                this, supervisorButtonLayout);

        makeButton(this,
                R.string.send_finalized_forms_description,
                R.string.send_finalized_forms_label,
                R.string.send_finalized_forms_label,
                this, supervisorButtonLayout);

        if (null == savedInstanceState)  {
            reviewFragment = new FormInstanceReviewFragment();
            getFragmentManager().beginTransaction()
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
     * Defining what happens when a main menu item is selected
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

    public void onClick(View v) {
        Integer tag = (Integer) v.getTag();
        if (tag.equals(R.string.sync_database_label)) {
            syncDatabase();
        } else if (tag.equals(R.string.sync_field_worker_label)) {
            syncFieldWorkers();
        } else if (tag.equals(R.string.search_database_label)) {
            searchDatabase();
        } else if (tag.equals(R.string.send_finalized_forms_label)) {
            reviewFragment.sendApprovedForms();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        encryptAllForms();
    }

    private void encryptAllForms() {
        EncryptionHelper.encryptFiles(
                FormInstance .toListOfFiles(OdkCollectHelper.getAllFormInstances(getContentResolver())), this);
    }

    private void syncDatabase() {
        String username = (String) getIntent().getExtras().get(OpeningActivity.USERNAME_KEY);
        String password = (String) getIntent().getExtras().get(OpeningActivity.PASSWORD_KEY);
        String openHdsBaseUrl = getPreferenceString(this, R.string.openhds_server_url_key, "");

        SyncEntitiesTask currentTask = new SyncEntitiesTask(openHdsBaseUrl, username, password,
                syncDatabaseHelper.getProgressDialog(), this, syncDatabaseHelper);
        syncDatabaseHelper.setCurrentTask(currentTask);
        syncDatabaseHelper.startSync();
    }

    private void syncFieldWorkers() {
        String username = (String) getIntent().getExtras().get(OpeningActivity.USERNAME_KEY);
        String password = (String) getIntent().getExtras().get(OpeningActivity.PASSWORD_KEY);
        String path = getResourceString(this, R.string.field_workers_sync_url);

        RequestContext requestContext =
                new RequestContext().user(username).password(password).url(buildServerUrl(this, path));
        SyncFieldworkersTask currentTask = new SyncFieldworkersTask(requestContext, getContentResolver(),
                syncDatabaseHelper.getProgressDialog(), syncDatabaseHelper);
        syncDatabaseHelper.setCurrentTask(currentTask);
        syncDatabaseHelper.startSync();
    }

    private void searchDatabase() {
        FormSearchPluginModule fieldWorkerPlugin = new FormSearchPluginModule(
                GatewayRegistry.getFieldWorkerGateway(), R.string.search_field_worker_label, "fieldWorker");
        fieldWorkerPlugin.getColumnsAndLabels().put(
                OpenHDS.FieldWorkers.COLUMN_FIELD_WORKER_FIRST_NAME, R.string.field_worker_first_name_label);
        fieldWorkerPlugin.getColumnsAndLabels().put(
                OpenHDS.FieldWorkers.COLUMN_FIELD_WORKER_LAST_NAME, R.string.field_worker_last_name_label);
        fieldWorkerPlugin.getColumnsAndLabels().put(
                OpenHDS.FieldWorkers.COLUMN_FIELD_WORKER_EXTID, R.string.field_worker_id_label);

        FormSearchPluginModule individualPlugin = new FormSearchPluginModule(
                GatewayRegistry.getIndividualGateway(), R.string.search_individual_label, "individual");
        individualPlugin.getColumnsAndLabels().put(
                OpenHDS.Individuals.COLUMN_INDIVIDUAL_FIRST_NAME, R.string.individual_first_name_label);
        individualPlugin.getColumnsAndLabels().put(
                OpenHDS.Individuals.COLUMN_INDIVIDUAL_LAST_NAME, R.string.individual_last_name_label);
        individualPlugin.getColumnsAndLabels().put(
                OpenHDS.Individuals.COLUMN_INDIVIDUAL_PHONE_NUMBER, R.string.individual_personal_phone_number_label);

        ArrayList<FormSearchPluginModule> plugins = new ArrayList<>();
        plugins.add(fieldWorkerPlugin);
        plugins.add(individualPlugin);

        Intent intent = new Intent(this, FormSearchActivity.class);
        intent.putParcelableArrayListExtra(FormSearchActivity.FORM_SEARCH_PLUGINS_KEY, plugins);
        startActivity(intent);
    }
}
