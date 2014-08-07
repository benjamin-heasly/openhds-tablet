package org.openhds.mobile.adapter;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import org.openhds.mobile.R;
import org.openhds.mobile.model.FormInstance;
import org.openhds.mobile.utilities.EncryptionHelper;

import java.io.File;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;


public class SupervisorFormInstanceAdapter extends ArrayAdapter {

    private ArrayList<FormInstance> formInstanceList;
    private ArrayList<CheckBox> checkBoxList;
    private Context context;
    private LayoutInflater inflater;

    public SupervisorFormInstanceAdapter(Context context, int resource, Object[] formInstances) {
        super(context, resource, formInstances);
        this.context = context;
        formInstanceList = new ArrayList<FormInstance>();
        checkBoxList = new ArrayList<CheckBox>();

        FormInstance tempInstance;
        CheckBox tempCheckBox;
        for (int i = 0; i < formInstances.length; i++) {
            tempInstance = (FormInstance) formInstances[i];
            formInstanceList.add(tempInstance);
            tempCheckBox = new CheckBox(context);
            checkBoxList.add(tempCheckBox);
        }

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    private static class ViewHolder {
        CheckBox checkBox;
        TextView formType;
        TextView fileName;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.form_instance_with_checkbox, null);
            holder = new ViewHolder();
            holder.formType = (TextView) convertView.findViewById(R.id.form_instance_list_type);
            holder.fileName = (TextView) convertView.findViewById(R.id.form_instance_list_filename);
            holder.fileName.setTag(formInstanceList.get(position));
            holder.checkBox = (CheckBox) convertView.findViewById(R.id.form_instance_check_box);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        FormInstance instance = (FormInstance) formInstanceList.get(position);
        holder.formType.setText(instance.getFileName());
        holder.fileName.setText(instance.getFormName());
        holder.fileName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FormInstance selected = (FormInstance) v.getTag();
                Uri uri = selected.getUri();

                File selectedFile = new File(selected.getFilePath());
                EncryptionHelper.decryptFile(selectedFile, context);

                Intent intent = new Intent(Intent.ACTION_EDIT, uri);
                ((Activity) context).startActivityForResult(intent, 0);
            }
        });

        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (holder.checkBox.isChecked()) {
                    checkBoxList.get(position).setChecked(true);

                } else {
                    checkBoxList.get(position).setChecked(false);
                }
            }
        });

        return convertView;

    }

    public List<FormInstance> registerApproveSelectedAction() {
        ArrayList<CheckBox> unselectedCheckBoxes = checkBoxList;
        ArrayList<FormInstance> approvedFormInstanceList = new ArrayList<>();
        for (int i = 0; i < checkBoxList.size(); i++) {
            if (checkBoxList.get(i).isChecked()) {
                unselectedCheckBoxes.remove(checkBoxList.get(i));
                formInstanceList.remove(i);
            }
        }
        checkBoxList = unselectedCheckBoxes;
        //notifyChange();
        return formInstanceList;
    }

    public List<FormInstance> registerApproveAllAction() {

        ArrayList<FormInstance> allInstances = formInstanceList;
        formInstanceList.clear();
        checkBoxList.clear();
        //notifyChange();
        return allInstances;
    }
}
