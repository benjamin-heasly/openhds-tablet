package org.openhds.mobile.model.core;

import java.io.Serializable;

public class Relationship implements Serializable {

    private static final long serialVersionUID = -7405831168618814047L;

    private String individualA;
    private String individualB;
    private String startDate;
    private String type;
    private String uuid;

    public Relationship() {}

    public Relationship(Individual individualAUuid, Individual individualBUuid, String type, String startDate, String uuid) {
        this.individualA = individualAUuid.getUuid();
        this.individualB = individualBUuid.getUuid();
        this.type = type;
        this.startDate = startDate;
        this.uuid = uuid;
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

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
