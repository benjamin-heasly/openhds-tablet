package org.openhds.mobile.model;

import org.openhds.mobile.projectdata.FormFilters.FormFilter;
import org.openhds.mobile.projectdata.FormPayloadBuilders.FormPayloadBuilder;
import org.openhds.mobile.projectdata.FormPayloadConsumers.FormPayloadConsumer;

public class FormBehaviour {

    private String formName;
    private int formLabelId;
    private FormFilter formFilter;
    private FormPayloadBuilder formPayloadBuilder;
    private FormPayloadConsumer formPayloadConsumer;

    public FormBehaviour(String formName, int formLabelId,
                         FormFilter formFilter, FormPayloadBuilder formMapper,
                         FormPayloadConsumer formPayloadConsumer) {

        this.formName = formName;
        this.formLabelId = formLabelId;
        this.formFilter = formFilter;
        this.formPayloadBuilder = formMapper;
        this.formPayloadConsumer = formPayloadConsumer;
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

}