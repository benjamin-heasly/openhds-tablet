package org.openhds.mobile.model.core;

import java.io.Serializable;

public class LocationHierarchyLevel implements Serializable {

    private String uuid;
    private String name;
    private int keyIdentifier;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getKeyIdentifier() {
        return keyIdentifier;
    }

    public void setKeyIdentifier(int keyIdentifier) {
        this.keyIdentifier = keyIdentifier;
    }
}
