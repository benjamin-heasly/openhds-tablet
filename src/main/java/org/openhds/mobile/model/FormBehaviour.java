package org.openhds.mobile.model;

import org.openhds.mobile.projectdata.FormFilter;
import org.openhds.mobile.projectdata.FormMapper;

public class FormBehaviour {

	private String formName;
	private int formLabelId;
	private String editForState;
	private FormFilter formFilter;
	private FormMapper formMapper;

	public FormBehaviour(String formName, int formLabelId, String state,
			FormFilter formFilter, FormMapper formMapper) {
		this.formName = formName;
		this.formLabelId = formLabelId;
		this.editForState = state;
		this.formFilter = formFilter;
		this.formMapper = formMapper;
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
	
	public FormMapper getFormMapper() {
		return formMapper;
	}

}