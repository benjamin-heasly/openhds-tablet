package org.openhds.mobile.model.core;

import java.io.Serializable;

public class LocationHierarchyLevel implements Serializable {

    private String uuid;
    private String name;
    private int keyIdentifier;

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
