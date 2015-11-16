package org.openhds.mobile.task.odk;

import android.content.Context;
import android.os.AsyncTask;

import org.openhds.mobile.forms.FormDefinition;
import org.openhds.mobile.forms.odk.OdkFormGateway;

import java.util.List;

/**
 * Created by ben on 11/16/15.
 *
 * Query for forms from an installed ODK Collect app.
 *
 */
public class FormsFromOdkTask extends AsyncTask<Context, Void, List<FormDefinition>> {

    public interface Listener {
        void onComplete(List<FormDefinition> formDefinitions);
    }

    private final Listener listener;

    public FormsFromOdkTask(Listener listener) {
        this.listener = listener;
    }

    @Override
    protected List<FormDefinition> doInBackground(Context... contexts) {
        if (null == contexts || 0 == contexts.length){
            return null;
        }

        Context context = contexts[0];
        return OdkFormGateway.findRegisteredForms(context.getContentResolver());
    }

    @Override
    protected void onPostExecute(List<FormDefinition> formDefinitions) {
        listener.onComplete(formDefinitions);
    }
}
