package org.openhds.mobile.model;

public class FormRecord {

	private String formName;
	private String formLabel;

	public FormRecord(String formName, String formLabel) {
		this.formName = formName;
		this.formLabel = formLabel;
	}

	public String getFormName() {
		return formName;
	}

	public String getFormLabel() {
		return formLabel;
	}

}