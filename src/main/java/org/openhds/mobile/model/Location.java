package org.openhds.mobile.model;

import java.io.Serializable;

public class Location implements Serializable {

	private static final long serialVersionUID = 230186771721044764L;

	private String extId;
	private String name;
	private String latitude;
	private String longitude;
	private String hierarchyExtId;

	public String getExtId() {
		return extId;
	}

	public void setExtId(String extId) {
		this.extId = extId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public String getHierarchyExtId() {
		return hierarchyExtId;
	}

	public void setHierarchyExtId(String hierarchyExtId) {
		this.hierarchyExtId = hierarchyExtId;
	}
}
