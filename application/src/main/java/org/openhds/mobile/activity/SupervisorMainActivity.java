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
import org.openhds.mobile.fragment.SyncDatabaseFragment;
import org.openhds.mobile.model.FormInstance;
import org.openhds.mobile.repository.search.FormSearchPluginModule;
import org.openhds.mobile.repository.search.SearchUtils;
import org.openhds.mobile.utilities.EncryptionHelper;
import org.openhds.mobile.utilities.OdkCollectHelper;

import java.util.ArrayList;
import java.util.List;

import static org.openhds.mobile.utilities.ConfigUtils.getPreferenceString;
import static org.openhds.mobile.utilities.LayoutUtils.makeButton;

public class SupervisorMainActivity extends Activity {

    private static final String REVIEW_FRAGMENT_TAG = "formInstanceReviewFragment";
    private static final String SYNC_FRAGMENT_TAG = "syncDatabaseFragment";

    private FrameLayout prefContainer;
    private LinearLayout supervisorButtonLayout;
    private FormInstanceReviewFragment formInstanceReviewFragment;
    private SyncDatabaseFragment syncDatabaseFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.supervisor_main);

        prefContainer = (FrameLayout) findViewById(R.id.login_pref_container);
        supervisorButtonLayout = (LinearLayout) findViewById(R.id.supervisor_activity_options);

        ButtonClickListener buttonClickListener = new ButtonClickListener();
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
            formInstanceReviewFragment = new FormInstanceReviewFragment();
            syncDatabaseFragment = new SyncDatabaseFragment();
            syncDatabaseFragment.setRetainInstance(true);
            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.login_pref_container, new LoginPreferenceFragment())
                    .add(R.id.supervisor_edit_form_container, formInstanceReviewFragment, REVIEW_FRAGMENT_TAG)
                    .add(R.id.supervisor_sync_database_container, syncDatabaseFragment, SYNC_FRAGMENT_TAG)
                    .commit();

        } else {
            formInstanceReviewFragment = (FormInstanceReviewFragment) getFragmentManager().findFragmentByTag(REVIEW_FRAGMENT_TAG);
            syncDatabaseFragment = (SyncDatabaseFragment) getFragmentManager().findFragmentByTag(SYNC_FRAGMENT_TAG);
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

    private void encryptAllForms() {
        List<FormInstance> allFormInstances = OdkCollectHelper.getAllFormInstances(getContentResolver());
        if (null != allFormInstances) {
            EncryptionHelper.encryptFiles(FormInstance.toListOfFiles(allFormInstances), this);
        }
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
            if (tag.equals(R.string.search_database_label)) {
                searchDatabase();
            } else if (tag.equals(R.string.send_finalized_forms_label)) {
                formInstanceReviewFragment.sendApprovedForms();
            }
        }
    }
}
