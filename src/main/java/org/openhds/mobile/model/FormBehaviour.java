package org.openhds.mobile.model;

public class FormBehaviour {

	private String formName;
	private int formLabelId;
	private String editForState;
	
	public FormBehaviour(String formName, int formLabelId, String state) {
		this.formName = formName;
		this.formLabelId = formLabelId;
		this.editForState = state;
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

}