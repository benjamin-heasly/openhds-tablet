package org.openhds.mobile.model;

import java.io.Serializable;

public class Relationship implements Serializable {

	private static final long serialVersionUID = -7405831168618814047L;

	private String individualA;
	private String individualB;
	private String startDate;
	private String type;

    public Relationship() {}

    public Relationship(Individual individualA, Individual individualB, String type, String startDate) {
        this.individualA = individualA.getExtId();
        this.individualB = individualB.getExtId();
        this.type = type;
        this.startDate = startDate;
    }

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getIndividualA() {
		return individualA;
	}

	public void setIndividualA(String individualA) {
		this.individualA = individualA;
	}

	public String getIndividualB() {
		return individualB;
	}

	public void setIndividualB(String individualB) {
		this.individualB = individualB;
	}

	// dates come in from the web service in dd-MM-yyyy format
	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}
}
