package org.openhds.mobile.task.odk;

import android.content.Context;
import android.os.AsyncTask;

import org.openhds.mobile.forms.FormDefinition;
import org.openhds.mobile.forms.odk.OdkFormGateway;

import java.util.List;

/**
 * Created by ben on 11/16/15.
 *
 * Register forms bundled with the app with the installed ODK Collect app.  See FormsProviderApi.
 *
 */
public class FormsToOdkTask extends AsyncTask<Context, Void, List<FormDefinition>> {

    public interface Listener {
        void onComplete(List<FormDefinition> formDefinitions);
    }

    private final Listener listener;

    public FormsToOdkTask(Listener listener) {
        this.listener = listener;
    }

    @Override
    protected List<FormDefinition> doInBackground(Context... contexts) {
        if (null == contexts || 0 == contexts.length){
            return null;
        }

        Context context = contexts[0];
        List<FormDefinition> formDefinitions = OdkFormGateway.expandBundledForms(context);
        return OdkFormGateway.registerForms(context.getContentResolver(), formDefinitions);
    }

    @Override
    protected void onPostExecute(List<FormDefinition> formDefinitions) {
        listener.onComplete(formDefinitions);
    }
}
