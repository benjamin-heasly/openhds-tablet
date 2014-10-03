package org.openhds.mobile.activity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import org.openhds.mobile.R;
import org.openhds.mobile.fragment.FieldWorkerLoginFragment;
import org.openhds.mobile.model.FieldWorker;
import org.openhds.mobile.model.FormInstance;
import org.openhds.mobile.projectdata.ProjectActivityBuilder;
import org.openhds.mobile.repository.GatewayRegistry;
import org.openhds.mobile.repository.gateway.FieldWorkerGateway;
import org.openhds.mobile.utilities.EncryptionHelper;
import org.openhds.mobile.utilities.OdkCollectHelper;
import android.view.View.OnClickListener;

import java.util.List;

public class SuperSecretDevBackdoor extends Activity implements OnClickListener {


    @Override
    protected void onResume() {
        super.onResume();

        //encrypt the forms
        List<FormInstance> allFormInstances = OdkCollectHelper.getAllFormInstances(getContentResolver());
        if (null != allFormInstances) {
            EncryptionHelper.encryptFiles(FormInstance.toListOfFiles(allFormInstances), this);
        }

    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.super_secret_back_door);

        if (null != savedInstanceState) {
            return;
        }

    }



    @Override
    public void onClick(View view) {

        if(view.getTag().equals("supervisor")){
            Intent intent = new Intent(this, SupervisorMainActivity.class);
            setFieldWorker(intent);
            startActivity(intent);
        }
        if(view.getTag().equals("fieldworker")){
            Intent intent = new Intent(this, PortalActivity.class);
            setFieldWorker(intent);
            startActivity(intent);
        }
        if(view.getTag().equals("login")){
            Intent intent = new Intent(this, OpeningActivity.class);
            setFieldWorker(intent);
            startActivity(intent);
        }

        if(view.getTag().equals("census")){
            Intent intent = new Intent(this, NavigateActivity.class);
            intent.putExtra(ProjectActivityBuilder.ACTIVITY_MODULE_EXTRA, ProjectActivityBuilder.getActivityModuleNames().get(0));

            setFieldWorker(intent);
            startActivity(intent);
        }
    }

    private void setFieldWorker(Intent intent){
        FieldWorkerGateway fieldWorkerGateway = GatewayRegistry.getFieldWorkerGateway();
        ContentResolver contentResolver = getContentResolver();
        FieldWorker fieldWorker = fieldWorkerGateway.getFirst(contentResolver, fieldWorkerGateway.findById("UNK"));

        intent.putExtra(FieldWorkerLoginFragment.FIELD_WORKER_EXTRA, fieldWorker);
    }
}
