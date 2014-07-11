package org.openhds.mobile.activity;

import static org.openhds.mobile.utilities.LayoutUtils.makeNewGenericLayout;

import java.util.List;

import android.widget.RelativeLayout;
import org.openhds.mobile.R;
import org.openhds.mobile.fragment.FieldWorkerLoginFragment;
import org.openhds.mobile.model.FieldWorker;
import org.openhds.mobile.projectdata.ProjectActivityBuilder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

public class PortalActivity extends Activity implements OnClickListener {

    private LinearLayout portals;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.portal_activity);

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

        if (null != savedInstanceState) {
            return;
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

}
