package org.openhds.mobile.links;

import org.openhds.mobile.repository.gateway.Gateway;
import org.openhds.mobile.task.parsing.entities.EntityParser;

/**
 * Tablet-side interpretation of a HATEOAS "rel" from the OpenHDS server.
 */
public class RelInterpretation<T> {

    private final String rel;

    private final Integer label;

    private final EntityParser<T> parser;

    private final Gateway<T> gateway;

    public RelInterpretation(String rel, Integer label, EntityParser<T> parser, Gateway<T> gateway) {
        this.rel = rel;
        this.label = label;
        this.parser = parser;
        this.gateway = gateway;
    }

    public String getRel() {
        return rel;
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
}
