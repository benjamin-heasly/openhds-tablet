package org.openhds.mobile.links;

import org.openhds.mobile.repository.gateway.Gateway;
import org.openhds.mobile.task.parsing.entities.EntityParser;

/**
 * Tablet-side interpretation of a HATEOAS "resourceRel" from the OpenHDS server.
 */
public class RelInterpretation<T> {

    private final String resourceRel;

    private final Integer label;

    private final EntityParser<T> parser;

    private final Gateway<T> gateway;

    private String syncRel = "bydatebulk";

    public RelInterpretation(String resourceRel, Integer label, EntityParser<T> parser, Gateway<T> gateway) {
        this.resourceRel = resourceRel;
        this.label = label;
        this.parser = parser;
        this.gateway = gateway;
    }

    public String getResourceRel() {
        return resourceRel;
    }

    public Integer getLabel() {
        return label;
    }

    public EntityParser<T> getParser() {
        return parser;
    }

    public Gateway<T> getGateway() {
        return gateway;
    }

    public String getSyncRel() {
        return syncRel;
    }

    public void setSyncRel(String syncRel) {
        this.syncRel = syncRel;
    }
}
