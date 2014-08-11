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
import android.widget.CompoundButton;
import android.widget.TextView;
import org.openhds.mobile.R;
import org.openhds.mobile.model.FormInstance;
import org.openhds.mobile.projectdata.ProjectResources;
import org.openhds.mobile.utilities.EncryptionHelper;

import java.io.File;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;


public class SupervisorFormInstanceAdapter extends ArrayAdapter {

    private ArrayList<FormInstance> formInstanceList;
    private ArrayList<Boolean> checkList;
    private Context context;
    private LayoutInflater inflater;

    public SupervisorFormInstanceAdapter(Context context, int resource, ArrayList<FormInstance> formInstances, ArrayList<Boolean> checkList) {
        super(context, resource, formInstances);
        this.context = context;
        this.formInstanceList = formInstances;
        if (null != checkList) {
            this.checkList = checkList;
        } else {
            this.checkList = clearCheckListState();
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

        convertView = inflater.inflate(R.layout.form_instance_with_checkbox, null);
        holder = new ViewHolder();
        holder.formType = (TextView) convertView.findViewById(R.id.form_instance_list_type);
        holder.fileName = (TextView) convertView.findViewById(R.id.form_instance_list_filename);
        holder.fileName.setTag(formInstanceList.get(position));
        holder.checkBox = (CheckBox) convertView.findViewById(R.id.form_instance_check_box);
        holder.checkBox.setChecked(checkList.get(position));

        FormInstance instance = (FormInstance) formInstanceList.get(position);
        String formType = instance.getFormName();
        int resourceId = ProjectResources.FormType.getFormTypeStringId(formType);
        holder.formType.setText(context.getResources().getString(resourceId));

        holder.fileName.setText(instance.getFileName());
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

        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                checkList.set(position, isChecked);

            }
        });

        convertView.setTag(holder);
        return convertView;

    }

    public List<FormInstance> registerApproveSelectedAction() {
        ArrayList<Integer> indexesToRemove = new ArrayList<>();
        ArrayList<FormInstance> approvedFormInstanceList = new ArrayList<>();

        //collect list of instances that were checked for approval
        for (int i = 0; i < checkList.size(); i++) {
            if (checkList.get(i)) {
                approvedFormInstanceList.add(formInstanceList.get(i));
                indexesToRemove.add(i);
            }
        }

        //remove the approved instances from the adapter's list
        for (Integer index: indexesToRemove) {
            FormInstance removed = formInstanceList.get(index);
            formInstanceList.remove(index);
            this.remove(removed);
        }

        //reset all checkboxes to unchecked
        checkList = clearCheckListState();

        notifyDataSetChanged();
        return approvedFormInstanceList;
    }

    private ArrayList<Boolean> clearCheckListState() {
        ArrayList<Boolean> newCheckList = new ArrayList<>();
        for (int i = 0; i < formInstanceList.size(); i++) {
            newCheckList.add(false);
        }
        return newCheckList;
    }

    public List<FormInstance> registerApproveAllAction() {
        ArrayList<FormInstance> allInstances = formInstanceList;
        return allInstances;
    }

    public void clearInstanceList() {
        formInstanceList.clear();
        checkList.clear();
        this.clear();
        notifyDataSetChanged();
    }

    public ArrayList<FormInstance> getListOfEditedForms() {
        return formInstanceList;
    }

    public ArrayList<Boolean> getCheckList() {
        return checkList;
    }
}
