package org.openhds.mobile.model;

import org.openhds.mobile.projectdata.FormFilters.FormFilter;
import org.openhds.mobile.projectdata.FormPayloadBuilders.FormPayloadBuilder;
import org.openhds.mobile.projectdata.FormPayloadConsumers.FormPayloadConsumer;
import org.openhds.mobile.repository.search.FormSearchPluginModule;

import java.util.List;

public class FormBehaviour {

    private String formName;
    private int formLabelId;
    private FormFilter formFilter;
    private FormPayloadBuilder formPayloadBuilder;
    private FormPayloadConsumer formPayloadConsumer;
    private List<FormSearchPluginModule> formSearchPluginModules;

    public FormBehaviour(String formName,
                         int formLabelId,
                         FormFilter formFilter,
                         FormPayloadBuilder formMapper,
                         FormPayloadConsumer formPayloadConsumer) {
        this(formName, formLabelId, formFilter, formMapper, formPayloadConsumer, null);
    }

    public FormBehaviour(String formName,
                         int formLabelId,
                         FormFilter formFilter,
                         FormPayloadBuilder formMapper,
                         FormPayloadConsumer formPayloadConsumer,
                         List<FormSearchPluginModule> formSearchPluginModules) {

        this.formName = formName;
        this.formLabelId = formLabelId;
        this.formFilter = formFilter;
        this.formPayloadBuilder = formMapper;
        this.formPayloadConsumer = formPayloadConsumer;
        this.formSearchPluginModules = formSearchPluginModules;
    }

    public String getFormName() {
        return formName;
    }

    public int getFormLabelId() {
        return formLabelId;
    }

    public FormFilter getFormFilter() {
        return formFilter;
    }

    public FormPayloadBuilder getFormPayloadBuilder() {
        return formPayloadBuilder;
    }

    public FormPayloadConsumer getFormPayloadConsumer() {
        return formPayloadConsumer;
    }

    public List<FormSearchPluginModule> getFormSearchPluginModules() {
        return formSearchPluginModules;
    }

    public boolean getNeedsFormFieldSearch() {
        return null != formSearchPluginModules && formSearchPluginModules.size() > 0;
    }
}