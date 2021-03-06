package org.openhds.mobile.model.core;

import java.io.Serializable;

public class SocialGroup implements Serializable {

    private static final long serialVersionUID = 571090333555561853L;

    private String groupName;
    private String uuid;
    private String extId;
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

    public SocialGroup() {}

    public SocialGroup(String name, Individual head, String uuid, String extId) {
        this.groupName = name;
        this.uuid = uuid;
        this.extId = extId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getExtId() {
        return extId;
    }

    public void setExtId(String extId) {
        this.extId = extId;
    }
}
