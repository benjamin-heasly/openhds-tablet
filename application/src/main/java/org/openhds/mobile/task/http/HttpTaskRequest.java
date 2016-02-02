package org.openhds.mobile.task.http;

import java.io.InputStream;

/**
 * Url and credentials for an HTTP request.
 *
 * Pass an HttpTaskRequest to an HttpTask to make it go.
 *
 * BSH
 */
public class HttpTaskRequest {
    private final String url;
    private final String userName;
    private final String password;
    private final String accept;

    private final String method;
    private final String contentType;
    private final InputStream body;

    // Just enough for at GET request
    public HttpTaskRequest(String url, String accept, String userName, String password) {
        this.url = url;
        this.accept = accept;
        this.userName = userName;
        this.password = password;
        this.method = null;
        this.contentType = null;
        this.body = null;
    }

    // General request
    public HttpTaskRequest(String url, String accept, String userName, String password, String method, String contentType, InputStream body) {
        this.url = url;
        this.userName = userName;
        this.password = password;
        this.accept = accept;
        this.method = method;
        this.contentType = contentType;
        this.body = body;
    }

    public String getUrl() {
        return url;
    }

    public String getAccept() {
        return accept;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public String getMethod() {
        return method;
    }

    public String getContentType() {
        return contentType;
    }

    public InputStream getBody() {
        return body;
    }
}
