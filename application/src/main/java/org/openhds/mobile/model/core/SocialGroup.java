package org.openhds.mobile.model.core;

import java.io.Serializable;

public class SocialGroup implements Serializable {

    private static final long serialVersionUID = 571090333555561853L;

    private String groupName;
    private String groupHeadUuid;
    private String uuid;
    private String extId;

    public SocialGroup() {}

    public SocialGroup(String name, Individual head, String uuid, String extId) {
        this.groupName = name;
        this.groupHeadUuid = head.getUuid();
        this.uuid = uuid;
        this.extId = extId;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupHeadUuid() {
        return groupHeadUuid;
    }

    public void setGroupHeadUuid(String groupHeadUUid) {
        this.groupHeadUuid = groupHeadUUid;
    }

    public String getExtId() {
        return extId;
    }

    public void setExtId(String extId) {
        this.extId = extId;
    }
}
