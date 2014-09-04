package org.openhds.mobile.model;

import java.io.Serializable;

public class Visit implements Serializable {

	private static final long serialVersionUID = 4419294254197721658L;

	private String visitExtId;
	private String locationExtId;
	private String visitDate;
	private String fieldWorkerExtId;

	public String getVisitExtId() {
		return visitExtId;
	}

	public void setVisitExtId(String visitExtId) {
		this.visitExtId = visitExtId;
	}

	public String getLocationExtId() {
		return locationExtId;
	}

	public void setLocationExtId(String locationExtId) {
		this.locationExtId = locationExtId;
	}

	public String getVisitDate() {
		return visitDate;
	}

	public void setVisitDate(String visitDate) {
		this.visitDate = visitDate;
	}

	public String getFieldWorkerExtId() {
		return fieldWorkerExtId;
	}

	public void setFieldWorkerExtId(String fieldWorkerExtid) {
		this.fieldWorkerExtId = fieldWorkerExtid;
	}

}
