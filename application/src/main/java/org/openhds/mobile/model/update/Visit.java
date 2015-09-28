package org.openhds.mobile.model.update;

import java.io.Serializable;
import java.util.Date;

public class Visit implements Serializable {

    private static final long serialVersionUID = 4419294254197721658L;

    private String extId;
    private String locationUuid;
    private String visitDate;
    private String fieldWorkerUuid;
    private String uuid;
    private String lastModifiedServer;
    private String lastModifiedClient;

    public String getLastModifiedServer() {
        return lastModifiedServer;
    }

    public void setLastModifiedServer(String lastModifiedServer) {
        this.lastModifiedServer = lastModifiedServer;
    }

    public String getLastModifiedClient() {
        return lastModifiedClient;
    }

    public void setLastModifiedClient(String lastModifiedClient) {
        this.lastModifiedClient = lastModifiedClient;
    }

    public String getExtId() {
        return extId;
    }

    public void setExtId(String extId) {
        this.extId = extId;
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

    public void setFieldWorkerUuid(String fieldWorkerUuid) {
        this.fieldWorkerUuid = fieldWorkerUuid;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
