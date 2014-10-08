package org.openhds.mobile.task.parsing;

import org.openhds.mobile.repository.gateway.Gateway;
import org.openhds.mobile.task.parsing.entities.EntityParser;

import java.io.InputStream;

/**
 * Input stream, EntityParser, and Gateway for parsing entities.
 *
 * Declare everything needed to parse an input stream into OpenHDS entities:
 *  - the input stream to read
 *  - an entity parser to parse incoming data into entity objects
 *  - a gateway to persist entities in the database
 *
 *  Pass a ParseEntityTaskRequest to a ParseEntityTask to make it go.
 *
 *  BSH
 */
public class ParseEntityTaskRequest<T> {
    private final String title;
    private InputStream inputStream;
    private final EntityParser<T> entityParser;
    private final Gateway<T> gateway;

    public ParseEntityTaskRequest(String title, InputStream inputStream, EntityParser<T> entityParser, Gateway<T> gateway) {
        this.title = title;
        this.inputStream = inputStream;
        this.entityParser = entityParser;
        this.gateway = gateway;
    }

    public String getTitle() {
        return title;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public EntityParser<T> getEntityParser() {
        return entityParser;
    }

    public Gateway<T> getGateway() {
        return gateway;
    }
}
