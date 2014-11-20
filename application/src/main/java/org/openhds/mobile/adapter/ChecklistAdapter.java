package org.openhds.mobile.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import org.openhds.mobile.R;
import org.openhds.mobile.model.FormInstance;
import org.openhds.mobile.projectdata.ProjectResources;
import org.openhds.mobile.utilities.EncryptionHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.openhds.mobile.utilities.MessageUtils.showShortToast;


public class ChecklistAdapter extends ArrayAdapter {

    private List<FormInstance> formInstanceList;
    private List<Boolean> checkList;
    private LayoutInflater inflater;


    public ChecklistAdapter(Context context, int checklistItemId, List<FormInstance> formInstances) {
        super(context, checklistItemId, formInstances);

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.formInstanceList = formInstances;
        this.checkList = initializeCheckBoxes();

    }

    private ArrayList<Boolean> initializeCheckBoxes() {
        ArrayList<Boolean> newCheckList = new ArrayList<>();
        for (int i = 0; i < formInstanceList.size(); i++) {
            newCheckList.add(false);
        }
        return newCheckList;
    }

    private static class ViewHolder {
        CheckBox checkBox;
        TextView formType;
        TextView fileName;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final ViewHolder holder;

        convertView = inflater.inflate(R.layout.form_instance_with_checkbox_orange, null);
        holder = new ViewHolder();
        holder.formType = (TextView) convertView.findViewById(R.id.form_instance_list_type);
        holder.fileName = (TextView) convertView.findViewById(R.id.form_instance_list_filename);
        holder.fileName.setTag(formInstanceList.get(position));
        holder.checkBox = (CheckBox) convertView.findViewById(R.id.form_instance_check_box);
        holder.checkBox.setChecked(checkList.get(position));

        FormInstance instance = formInstanceList.get(position);
        String formType = instance.getFormName();
        int resourceId = ProjectResources.FormType.getFormTypeStringId(formType);
        holder.formType.setText(getContext().getResources().getString(resourceId));

        String verboseFileName= instance.getFileName();
        //trim the file extension
        holder.fileName.setText(verboseFileName.substring(0, verboseFileName.length() - 4));

        holder.fileName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FormInstance selected = (FormInstance) v.getTag();
                Uri uri = Uri.parse(selected.getUriString());

                File selectedFile = new File(selected.getFilePath());
                EncryptionHelper.decryptFile(selectedFile, getContext());

                Intent intent = new Intent(Intent.ACTION_EDIT, uri);
                showShortToast(getContext(), R.string.launching_odk_collect);
                ((Activity) getContext()).startActivityForResult(intent, 0);
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

    public List<FormInstance> getCheckedForms() {

        List<FormInstance> checkedForms = new ArrayList<>();

        for (int i = 0; i < checkList.size(); i++) {
            if (checkList.get(i)) {
                FormInstance formInstance = formInstanceList.get(i);
                checkedForms.add(formInstance);
            }
        }

        return checkedForms;
    }

    public List<FormInstance> getFormInstanceList() {
        return formInstanceList;
    }

    public void resetFormInstanceList(List<FormInstance> formInstanceList) {
        this.formInstanceList = formInstanceList;
        checkList = initializeCheckBoxes();
        notifyDataSetChanged();
    }

    public List<Boolean> getCheckList() {
        return checkList;
    }

    public void setCheckList(List<Boolean> checkList) {
        this.checkList = checkList;
    }
}
