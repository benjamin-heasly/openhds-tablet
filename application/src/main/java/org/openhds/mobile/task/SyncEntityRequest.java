package org.openhds.mobile.task;

import org.openhds.mobile.repository.gateway.Gateway;

/**
 * Url, credentials, EntityParser, and Gateway for syncing entities.
 *
 * Declare everything needed to sync entities from the OpenHDS server:
 *  - which server Url to GET from
 *  - username and password for server authentication
 *  - an entity parser to parse incoming data into entity objects
 *  - a gateway to persist entities in the database
 *
 *  Pass a SyncEntityRequest to a SyncEntityTask to make it go.
 *
 *  BSH
 */
public class SyncEntityRequest<T> {
    private final String title;
    private final String url;
    private final String userName;
    private final String password;
    private final EntityParser<T> entityParser;
    private final Gateway<T> gateway;

    public SyncEntityRequest(String title, String url, String userName, String password,
                             EntityParser<T> entityParser, Gateway<T> gateway) {
        this.title = title;
        this.url = url;
        this.userName = userName;
        this.password = password;
        this.entityParser = entityParser;
        this.gateway = gateway;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public EntityParser<T> getEntityParser() {
        return entityParser;
    }

    public Gateway<T> getGateway() {
        return gateway;
    }
}
