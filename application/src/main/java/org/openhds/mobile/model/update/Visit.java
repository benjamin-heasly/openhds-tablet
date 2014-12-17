package org.openhds.mobile.model.update;

import java.io.Serializable;

public class Visit implements Serializable {

	private static final long serialVersionUID = 4419294254197721658L;

	private String visitExtId;
	private String locationUuid;
	private String visitDate;
	private String fieldWorkerUuid;
    private String uuid;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

	public String getVisitExtId() {
		return visitExtId;
	}

	public void setVisitExtId(String visitExtId) {
		this.visitExtId = visitExtId;
	}

	public String getLocationUuid() {
		return locationUuid;
	}

	public void setLocationUuid(String locationUuid) {
		this.locationUuid = locationUuid;
	}

	public String getVisitDate() {
		return visitDate;
	}

	public void setVisitDate(String visitDate) {
		this.visitDate = visitDate;
	}

	public String getFieldWorkerUuid() {
		return fieldWorkerUuid;
	}

	public void setFieldWorkerUuid(String fieldWorkerExtid) {
		this.fieldWorkerUuid = fieldWorkerExtid;
	}

}
