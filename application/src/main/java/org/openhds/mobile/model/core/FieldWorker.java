package org.openhds.mobile.model.core;

import java.io.Serializable;

public class FieldWorker implements Serializable {

    private static final long serialVersionUID = -8973040054481039466L;

    private String uuid;
    private String fieldWorkerId;
    private String firstName;
    private String lastName;
    private String passwordHash;
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

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getFieldWorkerId() {
        return fieldWorkerId;
    }

    public void setFieldWorkerId(String fieldWorkerId) {
        this.fieldWorkerId = fieldWorkerId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
}
