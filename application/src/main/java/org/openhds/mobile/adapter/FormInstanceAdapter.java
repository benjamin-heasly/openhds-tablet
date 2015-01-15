package org.openhds.mobile.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import org.openhds.mobile.R;
import org.openhds.mobile.model.form.FormInstance;
import org.openhds.mobile.projectdata.ProjectResources;

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

        FormInstance instance = (FormInstance) formInstances[position];
        String formType = instance.getFormName();
        String fileName = instance.getFileName();

        //trim the file extension
        fileName = fileName.substring(0, fileName.length() - 4);

        int formTypeLocalizedId= ProjectResources.FormType.getFormTypeStringId(formType);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.form_instance_list_item_orange, null);
        }

        TextView formTypeView = (TextView) convertView.findViewById(R.id.form_instance_list_type);
        formTypeView.setText(super.getContext().getResources().getString(formTypeLocalizedId));

        TextView fileNameView = (TextView) convertView.findViewById(R.id.form_instance_list_filename);
        fileNameView.setText(fileName);

        return convertView;
    }
}
