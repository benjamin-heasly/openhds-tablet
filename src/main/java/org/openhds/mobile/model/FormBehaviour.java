package org.openhds.mobile.model;

import org.openhds.mobile.projectdata.FormFilter;

public class FormBehaviour {

	private String formName;
	private int formLabelId;
	private String editForState;
	private FormFilter formFilter;

	public FormBehaviour(String formName, int formLabelId, String state,
			FormFilter formFilter) {
		this.formName = formName;
		this.formLabelId = formLabelId;
		this.editForState = state;
		this.formFilter = formFilter;
	}

	public String getFormName() {
		return formName;
	}

	public int getFormLabelId() {
		return formLabelId;
	}

	public String getEditForState() {
		return editForState;
	}

	public FormFilter getFormFilter() {
		return formFilter;
	}

}