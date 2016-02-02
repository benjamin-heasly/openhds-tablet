package org.openhds.mobile.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

import org.openhds.mobile.R;
import org.openhds.mobile.forms.FormDefinition;
import org.openhds.mobile.forms.FormInstance;
import org.openhds.mobile.forms.odk.InstanceProviderAPI;
import org.openhds.mobile.forms.odk.OdkInstanceGateway;
import org.openhds.mobile.fragment.LoginPreferenceFragment;
import org.openhds.mobile.fragment.SyncDatabaseFragment;
import org.openhds.mobile.repository.search.FormSearchPluginModule;
import org.openhds.mobile.repository.search.SearchUtils;
import org.openhds.mobile.task.odk.FormsToOdkTask;
import org.openhds.mobile.utilities.EncryptionHelper;

import java.util.ArrayList;
import java.util.List;

import static org.openhds.mobile.utilities.LayoutUtils.makeButton;
import static org.openhds.mobile.utilities.MessageUtils.showLongToast;
import static org.openhds.mobile.utilities.MessageUtils.showShortToast;

public class SupervisorMainActivity extends Activity {

    private static final String SYNC_FRAGMENT_TAG = "syncDatabaseFragment";
    private static final String PREFERENCE_FRAGMENT_TAG = "preferenceFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.supervisor_main);

        LinearLayout supervisorButtonLayout = (LinearLayout) findViewById(R.id.supervisor_activity_options);
        ButtonClickListener buttonClickListener = new ButtonClickListener();
        makeButton(this,
                R.string.search_database_description,
                R.string.search_database_label,
                R.string.search_database_label,
                buttonClickListener,
                supervisorButtonLayout);

        makeButton(this,
                R.string.register_odk_form_definitions_description,
                R.string.register_odk_form_definitions_label,
                R.string.register_odk_form_definitions_label,
                buttonClickListener,
                supervisorButtonLayout);

        makeButton(this,
                R.string.send_forms_to_openhds_description,
                R.string.send_forms_to_openhds_label,
                R.string.send_forms_to_openhds_label,
                buttonClickListener,
                supervisorButtonLayout);

        makeButton(this,
                R.string.send_finalized_forms_description,
                R.string.send_finalized_forms_label,
                R.string.send_finalized_forms_label,
                buttonClickListener,
                supervisorButtonLayout);

        SyncDatabaseFragment syncDatabaseFragment;
        PreferenceFragment preferenceFragment;
        if (null == savedInstanceState) {
            syncDatabaseFragment = new SyncDatabaseFragment();
            syncDatabaseFragment.setRetainInstance(true);
            preferenceFragment = new LoginPreferenceFragment();
            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.supervisor_auxiliary_container, syncDatabaseFragment, SYNC_FRAGMENT_TAG)
                    .add(R.id.supervisor_activity_options, preferenceFragment, PREFERENCE_FRAGMENT_TAG)
                    .commit();

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        encryptAllForms();
    }

    private void encryptAllForms() {
        List<FormInstance> allFormInstances = OdkInstanceGateway.findAllInstances(getContentResolver());
        if (null != allFormInstances) {
            EncryptionHelper.encryptFiles(FormInstance.toListOfFiles(allFormInstances), this);
        }
    }

    private void registerBundledForms() {
        final Context context = SupervisorMainActivity.this;
        final FormsToOdkTask.Listener listener = new FormsToOdkTask.Listener() {
            @Override
            public void onComplete(List<FormDefinition> formDefinitions) {
                showLongToast(context, formDefinitions.size() + " forms registered with Odk.");
            }
        };

        FormsToOdkTask formsToOdkTask = new FormsToOdkTask(listener);
        formsToOdkTask.execute(context);
    }

    private void searchDatabase() {
        ArrayList<FormSearchPluginModule> searchPluginModules = new ArrayList<>();
        searchPluginModules.add(SearchUtils.getFieldWorkerPlugin("fieldWorker", getString(R.string.search_field_worker_label)));
        searchPluginModules.add(SearchUtils.getIndividualPlugin("individual", getString(R.string.search_individual_label)));
        searchPluginModules.add(SearchUtils.getLocationPlugin("location", getString(R.string.search_location_label)));
        searchPluginModules.add(SearchUtils.getSocialGroupPlugin("socialGroup", getString(R.string.search_social_group_label)));

        Intent intent = new Intent(this, FormSearchActivity.class);
        intent.putParcelableArrayListExtra(FormSearchActivity.FORM_SEARCH_PLUGINS_KEY, searchPluginModules);
        startActivity(intent);
    }

    private void sendFormsToOpenHds() {
        Intent intent = new Intent(this, FormReviewActivity.class);
        // forward username and password to next activity
        intent.putExtras(getIntent().getExtras());
        startActivity(intent);
    }

    public void sendApprovedForms() {
        List<FormInstance> allFormInstances = OdkInstanceGateway.findInstancesByStatus(this.getContentResolver(), InstanceProviderAPI.STATUS_COMPLETE);
        EncryptionHelper.decryptFiles(FormInstance.toListOfFiles(allFormInstances), this);
        showShortToast(this, R.string.launching_odk_collect);
        startActivity(new Intent(Intent.ACTION_EDIT));
    }

    private class ButtonClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            Integer tag = (Integer) v.getTag();
            if (tag.equals(R.string.search_database_label)) {
                searchDatabase();
            } else if (tag.equals(R.string.register_odk_form_definitions_label)) {
                registerBundledForms();
            } else if (tag.equals(R.string.send_forms_to_openhds_label)) {
                sendFormsToOpenHds();
            } else if (tag.equals(R.string.send_finalized_forms_label)) {
                sendApprovedForms();
            }
        }
    }
}
