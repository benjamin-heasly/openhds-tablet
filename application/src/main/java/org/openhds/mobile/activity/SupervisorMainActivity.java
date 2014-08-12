package org.openhds.mobile.activity;

import static org.openhds.mobile.utilities.ConfigUtils.getPreferenceString;
import static org.openhds.mobile.utilities.ConfigUtils.getResourceString;
import static org.openhds.mobile.utilities.LayoutUtils.makeNewGenericButton;
import static org.openhds.mobile.utilities.UrlUtils.buildServerUrl;
import android.content.res.Configuration;
import android.widget.*;
import org.openhds.mobile.R;
import org.openhds.mobile.fragment.FormInstanceReviewFragment;
import org.openhds.mobile.fragment.LoginPreferenceFragment;
import org.openhds.mobile.model.FormInstance;
import org.openhds.mobile.task.HttpTask.RequestContext;
import org.openhds.mobile.task.SyncEntitiesTask;
import org.openhds.mobile.task.SyncFieldworkersTask;
import org.openhds.mobile.utilities.EncryptionHelper;
import org.openhds.mobile.utilities.OdkCollectHelper;
import org.openhds.mobile.utilities.SyncDatabaseHelper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

public class SupervisorMainActivity extends Activity implements OnClickListener {

    private static final String REVIEW_FRAGMENT_TAG = "reviewFragment";
    private FrameLayout prefContainer;
	private LinearLayout supervisorOptionsList;
    private SyncDatabaseHelper syncDatabaseHelper;
    private FormInstanceReviewFragment reviewFragment;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.supervisor_main);

		prefContainer = (FrameLayout) findViewById(R.id.login_pref_container);
		supervisorOptionsList = (LinearLayout) findViewById(R.id.supervisor_activity_options);
		syncDatabaseHelper = new SyncDatabaseHelper(this);

		makeNewGenericButton(this,
				getResourceString(this, R.string.sync_database_description),
				getResourceString(this, R.string.sync_database_name),
				getResourceString(this, R.string.sync_database_name), this,
				supervisorOptionsList);

		makeNewGenericButton(
				this,
				getResourceString(this, R.string.sync_field_worker_description),
				getResourceString(this, R.string.sync_field_worker_name),
				getResourceString(this, R.string.sync_field_worker_name), this,
				supervisorOptionsList);

		makeNewGenericButton(
				this,
				getResourceString(this,
						R.string.send_finalized_forms_description),
				getResourceString(this, R.string.send_finalized_forms_name),
				getResourceString(this, R.string.send_finalized_forms_name),
				this, supervisorOptionsList);

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
            boolean isShowingPreferences = View.VISIBLE == prefContainer
                    .getVisibility();
            boolean isPortraitMode = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
            if (isShowingPreferences) {
                if (isPortraitMode) {
                    supervisorOptionsList.setVisibility(View.VISIBLE);
                }
                prefContainer.setVisibility(View.GONE);
            } else {
                if (isPortraitMode) {
                    supervisorOptionsList.setVisibility(View.GONE);
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
		String tag = (String) v.getTag();
		if (tag.equals(getResourceString(this, R.string.sync_database_name))) {
			syncDatabase();
		} else if (tag.equals(getResourceString(this,
				R.string.sync_field_worker_name))) {
			syncFieldWorkers();
		} else if (tag.equals((getResourceString(this,
				R.string.send_finalized_forms_name)))) {
            reviewFragment.sendApprovedForms();
		}
	}

    @Override
	protected void onResume() {
		super.onResume();
		encryptAllForms();
	}

	private void syncDatabase() {

		String username = (String) getIntent().getExtras().get(
				OpeningActivity.USERNAME_KEY);
		String password = (String) getIntent().getExtras().get(
				OpeningActivity.PASSWORD_KEY);

		String openHdsBaseUrl = getPreferenceString(this,
				R.string.openhds_server_url_key, "");
		SyncEntitiesTask currentTask = new SyncEntitiesTask(openHdsBaseUrl,
				username, password, syncDatabaseHelper.getProgressDialog(),
				this, syncDatabaseHelper);
		syncDatabaseHelper.setCurrentTask(currentTask);

		syncDatabaseHelper.startSync();
	}

	private void decryptAllForms() {

		EncryptionHelper.decryptFiles(FormInstance
				.toListOfFiles(OdkCollectHelper
						.getAllFormInstances(getContentResolver())), this);
	}

	private void encryptAllForms() {

		EncryptionHelper.encryptFiles(FormInstance
				.toListOfFiles(OdkCollectHelper
						.getAllFormInstances(getContentResolver())), this);

	}

	private void syncFieldWorkers() {

		String username = (String) getIntent().getExtras().get(
				OpeningActivity.USERNAME_KEY);
		String password = (String) getIntent().getExtras().get(
				OpeningActivity.PASSWORD_KEY);
		String path = getResourceString(this, R.string.field_workers_sync_url);

		RequestContext requestContext = new RequestContext().user(username)
				.password(password).url(buildServerUrl(this, path));
		SyncFieldworkersTask currentTask = new SyncFieldworkersTask(
				requestContext, getContentResolver(),
				syncDatabaseHelper.getProgressDialog(), syncDatabaseHelper);
		syncDatabaseHelper.setCurrentTask(currentTask);

		syncDatabaseHelper.startSync();
	}

}
