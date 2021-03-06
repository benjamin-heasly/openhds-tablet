package org.openhds.mobile.model.core;

import java.io.Serializable;

public class Membership implements Serializable {

    private static final long serialVersionUID = 6446118055284774938L;

    private String uuid;
    private String individualUuid;
    private String socialGroupUuid;
    private String lastModifiedServer;
    private String lastModifiedClient;

    public Membership() {}

    public Membership(Individual individual, SocialGroup socialGroup, String relationshipToHead, String uuid) {
        this.individualUuid = individual.getUuid();
        this.socialGroupUuid = socialGroup.getUuid();
        this.uuid = uuid;
    }

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

    public String getIndividualUuid() {
        return individualUuid;
    }

    public void setIndividualUuid(String individualUuid) {
        this.individualUuid = individualUuid;
    }

    public String getSocialGroupUuid() {
        return socialGroupUuid;
    }

    public void setSocialGroupUuid(String socialGroupUuid) {
        this.socialGroupUuid = socialGroupUuid;
    }
}
