package org.openhds.mobile.forms;

import org.openhds.mobile.repository.search.FormSearchPluginModule;

import java.util.ArrayList;

public class FormBehaviour {

    private String formName;
    private int formLabelId;

    // ArrayList, not just List, because of user with Android Parcelable interface.
    private ArrayList<FormSearchPluginModule> formSearchPluginModules;

    public FormBehaviour(String formName, int formLabelId) {
        this(formName, formLabelId, null);
    }

    public FormBehaviour(String formName,
                         int formLabelId,
                         ArrayList<FormSearchPluginModule> formSearchPluginModules) {

        this.formName = formName;
        this.formLabelId = formLabelId;
        this.formSearchPluginModules = formSearchPluginModules;
    }

    public String getFormName() {
        return formName;
    }

    public int getFormLabelId() {
        return formLabelId;
    }

    public ArrayList<FormSearchPluginModule> getFormSearchPluginModules() {
        return formSearchPluginModules;
    }

    public boolean getNeedsFormFieldSearch() {
        return null != formSearchPluginModules && formSearchPluginModules.size() > 0;
    }
}