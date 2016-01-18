package org.openhds.mobile.task.odk;

import android.content.Context;
import android.os.AsyncTask;

import org.openhds.mobile.forms.FormDefinition;
import org.openhds.mobile.forms.odk.OdkFormGateway;

import java.util.ArrayList;
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

    private final String formId;

    public FormsFromOdkTask(Listener listener) {
        this(listener, null);
    }

    public FormsFromOdkTask(Listener listener, String formId) {
        this.listener = listener;
        this.formId = formId;
    }

    @Override
    protected List<FormDefinition> doInBackground(Context... contexts) {
        if (null == contexts || 0 == contexts.length){
            return null;
        }

        Context context = contexts[0];
        if (null == formId) {
            return OdkFormGateway.findRegisteredForms(context.getContentResolver());
        } else {
            List<FormDefinition> formDefinitions = new ArrayList<>();
            FormDefinition formDefinition = OdkFormGateway.findRegisteredFormById(context.getContentResolver(), formId);
            if (null != formDefinition) {
                formDefinitions.add(formDefinition);
            }
            return formDefinitions;
        }
    }

    @Override
    protected void onPostExecute(List<FormDefinition> formDefinitions) {
        listener.onComplete(formDefinitions);
    }
}
