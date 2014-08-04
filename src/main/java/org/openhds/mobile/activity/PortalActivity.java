package org.openhds.mobile.activity;

import static org.openhds.mobile.utilities.LayoutUtils.makeNewGenericLayout;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;

import android.net.Uri;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.*;
import org.openhds.mobile.R;
import org.openhds.mobile.adapter.FormInstanceArrayAdapter;
import org.openhds.mobile.fragment.FieldWorkerLoginFragment;
import org.openhds.mobile.model.FieldWorker;
import org.openhds.mobile.model.FormInstance;
import org.openhds.mobile.projectdata.ProjectActivityBuilder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import org.openhds.mobile.utilities.EncryptionHelper;
import org.openhds.mobile.utilities.OdkCollectHelper;

public class PortalActivity extends Activity implements OnClickListener {

    private LinearLayout portals;
    private ListView formInstanceLayout;
    private List<FormInstance> unsyncedFormInstances;
    private FieldWorker currentFieldWorker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.portal_activity);

        currentFieldWorker = (FieldWorker) getIntent().getExtras().get(FieldWorkerLoginFragment.FIELD_WORKER_EXTRA);

        portals = (LinearLayout) findViewById(R.id.portal_middle_column);
        List<String> activityModuleNames = ProjectActivityBuilder
                .getActivityModuleNames();

        for (String name : activityModuleNames) {

            RelativeLayout layout = makeNewGenericLayout(this,
                    getString(ProjectActivityBuilder.getModuleInfoByName(name).getModuleLabelStringId()),
                    getString(ProjectActivityBuilder.getModuleInfoByName(name).getModuleDescriptionStringId()),
                    name, this, portals,
                    ProjectActivityBuilder.getModuleInfoByName(name).getModuleColorId(), null, null);

            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) layout.getLayoutParams();
            params.setMargins(0, 0, 0, 20);
        }

        formInstanceLayout = (ListView) findViewById(R.id.portal_right_column);
        TextView header = (TextView) this.getLayoutInflater().inflate(R.layout.generic_header, null);
        header.setText(R.string.form_instance_list_header);
        formInstanceLayout.addHeaderView(header);

        populateFormInstanceListView();
        setTitle(this.getResources().getString(R.string.field_worker_home_menu_text));

        if (null != savedInstanceState) {
            return;
        }
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

    private void populateFormInstanceListView() {

        unsyncedFormInstances = OdkCollectHelper.getAllUnsentFormInstances(getContentResolver());

        if (!unsyncedFormInstances.isEmpty()) {
            FormInstanceArrayAdapter adapter = new FormInstanceArrayAdapter(this, R.id.form_instance_list_item,
                    unsyncedFormInstances.toArray());

            formInstanceLayout.setAdapter(adapter);
            formInstanceLayout.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    FormInstance selected = unsyncedFormInstances.get(position - 1);
                    Uri uri = selected.getUri();

                    File selectedFile = new File(selected.getFilePath());
                    EncryptionHelper.decryptFile(selectedFile, getApplicationContext());

                    Intent intent = new Intent(Intent.ACTION_EDIT, uri);
                    startActivityForResult(intent, 0);

                }
            });
        }

    }

    @Override
    public void onClick(View v) {
        String tag = (String) v.getTag();

        Intent intent = new Intent(this, NavigateActivity.class);

        FieldWorker fieldworker = (FieldWorker) getIntent().getExtras().get(
                FieldWorkerLoginFragment.FIELD_WORKER_EXTRA);

        intent.putExtra(FieldWorkerLoginFragment.FIELD_WORKER_EXTRA,
                fieldworker);
        intent.putExtra(ProjectActivityBuilder.ACTIVITY_MODULE_EXTRA, tag);

        startActivity(intent);

    }

    @Override
    protected void onResume() {
        super.onResume();
        populateFormInstanceListView();
    }
}
