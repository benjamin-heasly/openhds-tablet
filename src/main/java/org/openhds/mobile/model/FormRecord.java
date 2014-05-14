package org.openhds.mobile.model;

public class FormRecord {

	private String formName;
	private String formLabel;
	private String editForState;
	
	public FormRecord(String formName, String formLabel, String state) {
		this.formName = formName;
		this.formLabel = formLabel;
		this.editForState = state;
	}

	public String getFormName() {
		return formName;
	}

	public String getFormLabel() {
		return formLabel;
	}
	
	public String getEditForState() {
		return editForState;
	}

}