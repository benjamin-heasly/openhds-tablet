package org.openhds.mobile.activity;

import static org.openhds.mobile.utilities.ConfigUtils.getPreferenceString;
import static org.openhds.mobile.utilities.ConfigUtils.getResourceString;
import static org.openhds.mobile.utilities.LayoutUtils.makeNewGenericButton;
import static org.openhds.mobile.utilities.UrlUtils.buildServerUrl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.widget.*;
import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.R;
import org.openhds.mobile.adapter.SupervisorFormInstanceAdapter;
import org.openhds.mobile.fragment.LoginPreferenceFragment;
import org.openhds.mobile.model.FormHelper;
import org.openhds.mobile.model.FormInstance;
import org.openhds.mobile.projectdata.ProjectFormFields;
import org.openhds.mobile.projectdata.ProjectResources;
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

	private FrameLayout prefContainer;
	private LinearLayout supervisorOptionsList;
    private ListView editFormListView;
    private SupervisorFormInstanceAdapter adapter;

	private SyncDatabaseHelper syncDatabaseHelper;
    private ArrayList<FormInstance> editedForms;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.supervisor_main);

		prefContainer = (FrameLayout) findViewById(R.id.login_pref_container);
		supervisorOptionsList = (LinearLayout) findViewById(R.id.supervisor_activity_options);
		syncDatabaseHelper = new SyncDatabaseHelper(this);
        editedForms = new ArrayList<FormInstance>();

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

        makeNewGenericButton(this, getResourceString(this, R.string.supervisor_approve_selected),
                getResourceString(this, R.string.supervisor_approve_selected),
                getResourceString(this, R.string.supervisor_approve_selected), this, supervisorOptionsList);

        makeNewGenericButton(this, getResourceString(this, R.string.supervisor_approve_all),
                getResourceString(this, R.string.supervisor_approve_all),
                getResourceString(this, R.string.supervisor_approve_all), this, supervisorOptionsList);

        editFormListView = (ListView) findViewById(R.id.supervisor_edit_form_list);
        TextView headerForms = (TextView) this.getLayoutInflater().inflate(R.layout.generic_header, null);
        headerForms.setText(R.string.supervisor_edit_form_list_header);
        editFormListView.addHeaderView(headerForms);

        populateEditFormListView();

        setupApprovalButtons();

		if (null != savedInstanceState) {
			return;
		}

		getFragmentManager().beginTransaction()
				.add(R.id.login_pref_container, new LoginPreferenceFragment())
				.commit();

	}

    private void setupApprovalButtons() {

        TextView approveAllButton = (TextView) findViewById(R.id.supervisor_approve_all_button);
        if (editedForms.size() > 0) {
            approveAllButton.setVisibility(View.VISIBLE);
        } else {
            approveAllButton.setVisibility(View.GONE);
        }
        TextView approveSelectedButton = (TextView) findViewById(R.id.supervisor_approve_selected_button);
        if (editedForms.size() > 0) {
            approveSelectedButton.setVisibility(View.VISIBLE);
        } else {
            approveAllButton.setVisibility(View.GONE);
        }

    }


    private void populateEditFormListView() {

        fillEditedFormsList();
        if (!editedForms.isEmpty()) {
            adapter = new SupervisorFormInstanceAdapter(this, R.id.form_instance_list_item,
                    editedForms);

            editFormListView.setAdapter(adapter);
        }
    }

    private void fillEditedFormsList() {
        List<FormInstance> allFormInstances = OdkCollectHelper.getAllUnsentFormInstances(getContentResolver());
        for (FormInstance instance : allFormInstances ) {
            File instanceFile = new File(instance.getFilePath());
            EncryptionHelper.decryptFile(instanceFile, getApplicationContext());
            String needsReview = FormHelper.getFormTagValue(ProjectFormFields.General.NEEDS_REVIEW, instance.getFilePath());

            if (needsReview.equalsIgnoreCase(ProjectResources.General.FORM_NEEDS_REVIEW)) {
                editedForms.add(instance);
            }
            EncryptionHelper.encryptFile(instanceFile, getApplicationContext());
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
            if (isShowingPreferences) {
                prefContainer.setVisibility(View.GONE);
            } else {
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
            sendApprovedForms();
		} else if (tag.equals((getResourceString(this, R.string.supervisor_approve_all)))) {
            approveListOfForms(adapter.registerApproveAllAction());
            editedForms = adapter.getListOfEditedForms();
        } else if (tag.equals((getResourceString(this, R.string.supervisor_approve_selected)))) {
            approveListOfForms(adapter.registerApproveSelectedAction());
            editedForms = adapter.getListOfEditedForms();
        }
	}

    private void sendApprovedForms() {

        List<FormInstance> allFormInstances = OdkCollectHelper.getAllUnsentFormInstances(getContentResolver());
        EncryptionHelper.decryptFiles(FormInstance.toListOfFiles(allFormInstances), this);
        for (FormInstance instance: allFormInstances) {
            File instanceFile = new File(instance.getFilePath());
            EncryptionHelper.decryptFile(instanceFile, this);
            if (!FormHelper.isFormReviewed(instance.getFilePath())) {
                OdkCollectHelper.setStatusIncomplete(getContentResolver(), instance.getUri());
                EncryptionHelper.encryptFile(instanceFile, this);
            }

        }

        startActivity(new Intent(Intent.ACTION_EDIT));
    }

    private void approveListOfForms(List<FormInstance> formInstances) {

        for (FormInstance instance: formInstances) {
            File instanceFile = new File(instance.getFilePath());
            EncryptionHelper.decryptFile(instanceFile, this);
            FormHelper.setFormTagValue(ProjectFormFields.General.NEEDS_REVIEW, ProjectResources.General.FORM_NO_REVIEW_NEEDED,
                    instance.getFilePath());
            EncryptionHelper.encryptFile(instanceFile, this);
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
