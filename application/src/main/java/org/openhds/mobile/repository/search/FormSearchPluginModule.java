package org.openhds.mobile.repository.search;

import org.openhds.mobile.repository.gateway.Gateway;

/**
 * Represents a "search" to fill in a field of a form.
 *
 * Extends SearchPluginModule to add the form field name that needs to
 * be filled in.
 *
 * TODO: might also add a consumer to take a QueryResult
 * (eg from FormSearchActivity) and return up a related value
 * that can be fill into the form field.
 */
public class FormSearchPluginModule extends SearchPluginModule {
    private String fieldName;

    public FormSearchPluginModule(Gateway gateway, int labelId, String fieldName) {
        super(gateway, labelId);
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
}
