package org.openhds.mobile.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import org.openhds.mobile.R;
import org.openhds.mobile.model.form.FormHelper;
import org.openhds.mobile.model.form.FormInstance;
import org.openhds.mobile.projectdata.ProjectFormFields;
import org.openhds.mobile.projectdata.ProjectResources;
import org.openhds.mobile.utilities.EncryptionHelper;

import java.io.File;
import java.util.Map;

public class FormInstanceAdapter extends ArrayAdapter {

    private Object[] formInstances;
    private LayoutInflater inflater;


    public FormInstanceAdapter(Context context, int resource, Object[] formInstances) {
        super(context, resource, formInstances);
        this.formInstances = formInstances;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // get the data we want to display
        FormInstance instance = (FormInstance) formInstances[position];
        int formTypeLocalizedId= ProjectResources.FormType.getFormTypeStringId(instance.getFormName());
        String formTypeName = super.getContext().getResources().getString(formTypeLocalizedId);

        File formFile = new File(instance.getFilePath());
        EncryptionHelper.decryptFile(formFile, super.getContext());
        Map<String, String> instanceData = FormHelper.fetchFormInstanceData(instance.getFilePath());
        EncryptionHelper.encryptFile(formFile,super.getContext());

        String entityId = safeGetMapField(instanceData, ProjectFormFields.General.ENTITY_EXTID);
        String fieldWorker = safeGetMapField(instanceData, ProjectFormFields.General.FIELD_WORKER_EXTID);
        String date = safeGetMapField(instanceData, ProjectFormFields.General.COLLECTION_DATE_TIME);

        // stuff values into a view widget
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.form_instance_list_item_orange, null);
        }

        TextView formTypeView = (TextView) convertView.findViewById(R.id.form_instance_list_type);
        formTypeView.setText(formTypeName);

        TextView formIdView = (TextView) convertView.findViewById(R.id.form_instance_list_id);
        formIdView.setText(entityId);

        TextView fieldWorkerView = (TextView) convertView.findViewById(R.id.form_instance_list_fieldworker);
        fieldWorkerView.setText(fieldWorker);

        TextView formDateView = (TextView) convertView.findViewById(R.id.form_instance_list_date);
        formDateView.setText(date);

        return convertView;
    }

    private static String safeGetMapField(Map<String, String> map, String key) {
        return null == map || !map.containsKey(key) ? "" : map.get(key);
    }
}
