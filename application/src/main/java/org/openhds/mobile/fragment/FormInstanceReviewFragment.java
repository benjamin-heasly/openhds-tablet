package org.openhds.mobile.fragment;


import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import org.openhds.mobile.R;
import org.openhds.mobile.adapter.SupervisorFormInstanceAdapter;
import org.openhds.mobile.model.FormHelper;
import org.openhds.mobile.model.FormInstance;
import org.openhds.mobile.projectdata.ProjectFormFields;
import org.openhds.mobile.projectdata.ProjectResources;
import org.openhds.mobile.utilities.EncryptionHelper;
import org.openhds.mobile.utilities.OdkCollectHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FormInstanceReviewFragment extends Fragment {

    private ListView editFormListView;
    private TextView headerView;
    private RelativeLayout fragmentLayout;
    private TextView approveAllButton;
    private TextView approveSelectedButton;

    private SupervisorFormInstanceAdapter adapter;
    private ArrayList<Boolean> approveCheckList;
    private ArrayList<FormInstance> editedForms;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        editedForms = new ArrayList<FormInstance>();
        fragmentLayout = (RelativeLayout) inflater.inflate(R.layout.supervisor_edit_form_fragment_layout, container, false);
        editFormListView = (ListView) fragmentLayout.findViewById(R.id.supervisor_edit_form_list);

        headerView = (TextView) fragmentLayout.findViewById(R.id.supervisor_edit_form_list_header);

        approveAllButton = (TextView) fragmentLayout.findViewById(R.id.supervisor_approve_all_button);
        approveSelectedButton = (TextView) fragmentLayout.findViewById(R.id.supervisor_approve_selected_button);

        approveAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                approveAll();
            }
        });

        approveSelectedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                approveSelected();
            }
        });

        populateEditFormListView(savedInstanceState);
        setButtonVisibility();

        return fragmentLayout;
    }

    private void populateEditFormListView(Bundle savedInstanceState) {

        if (null != savedInstanceState) {
            approveCheckList = (ArrayList<Boolean>) savedInstanceState.get("approveCheckList");
            editedForms = (ArrayList<FormInstance>) savedInstanceState.get("editedForms");
        } else {
            fillEditedFormsList();
            approveCheckList = null;
        }

        if (null != editedForms && !editedForms.isEmpty()) {
            adapter = new SupervisorFormInstanceAdapter(getActivity(), R.id.form_instance_list_item,
                    editedForms, approveCheckList);
            headerView.setVisibility(View.VISIBLE);
            editFormListView.setAdapter(adapter);
        }
    }

    private void fillEditedFormsList() {
        List<FormInstance> allFormInstances = OdkCollectHelper.getAllUnsentFormInstances(getActivity().getContentResolver());
        for (FormInstance instance : allFormInstances ) {
            File instanceFile = new File(instance.getFilePath());
            EncryptionHelper.decryptFile(instanceFile, getActivity());
            String needsReview = FormHelper.getFormTagValue(ProjectFormFields.General.NEEDS_REVIEW, instance.getFilePath());

            if (ProjectResources.General.FORM_NEEDS_REVIEW.equalsIgnoreCase(needsReview)) {
                editedForms.add(instance);
            }
            EncryptionHelper.encryptFile(instanceFile, getActivity());
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (null != adapter) {
            outState.putSerializable("approveCheckList", adapter.getCheckList());
            outState.putSerializable("editedForms", adapter.getListOfEditedForms());
        }
    }


    private void setButtonVisibility() {
        if (null != editedForms && editedForms.size() > 0) {
            approveAllButton.setVisibility(View.VISIBLE);
            approveSelectedButton.setVisibility(View.VISIBLE);
        } else {
            approveAllButton.setVisibility(View.INVISIBLE);
            approveSelectedButton.setVisibility(View.INVISIBLE);
        }
    }

    private void setHeaderVisibility() {
        if (null != editedForms && editedForms.size() > 0) {
            headerView.setVisibility(View.VISIBLE);
        } else {
            headerView.setVisibility(View.GONE);
        }
    }

    private void approveSelected() {
        if (null != adapter) {
            approveListOfForms(adapter.registerApproveSelectedAction());
            editedForms = adapter.getListOfEditedForms();
            if (editedForms.size() == 0) {
                editFormListView = null;
            }
        }
        setButtonVisibility();
        setHeaderVisibility();
    }

    private void approveAll() {
        if (null != adapter) {
            List<FormInstance> approved = adapter.registerApproveAllAction();
            approveListOfForms(approved);
//            adapter.clearInstanceList();
            editedForms.clear();
            editFormListView = null;
        }
        setButtonVisibility();
        setHeaderVisibility();
    }

    public void sendApprovedForms() {

        List<FormInstance> allFormInstances = OdkCollectHelper.getAllUnsentFormInstances(getActivity().getContentResolver());
        EncryptionHelper.decryptFiles(FormInstance.toListOfFiles(allFormInstances), getActivity());
        for (FormInstance instance: allFormInstances) {
            File instanceFile = new File(instance.getFilePath());
            if (!FormHelper.isFormReviewed(instance.getFilePath())) {
                OdkCollectHelper.setStatusIncomplete(getActivity().getContentResolver(), Uri.parse(instance.getUriString()));
                EncryptionHelper.encryptFile(instanceFile, getActivity());
            }
        }
        startActivity(new Intent(Intent.ACTION_EDIT));
    }

    private void approveListOfForms(List<FormInstance> formInstances) {

        for (FormInstance instance: formInstances) {
            File instanceFile = new File(instance.getFilePath());
            EncryptionHelper.decryptFile(instanceFile, getActivity());
            FormHelper.setFormTagValue(ProjectFormFields.General.NEEDS_REVIEW, ProjectResources.General.FORM_NO_REVIEW_NEEDED,
                    instance.getFilePath());
            OdkCollectHelper.setStatusComplete(getActivity().getContentResolver(), Uri.parse(instance.getUriString()));
            EncryptionHelper.encryptFile(instanceFile, getActivity());
        }
    }

}
