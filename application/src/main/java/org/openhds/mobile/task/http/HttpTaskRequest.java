package org.openhds.mobile.task.http;

/**
 * Url and credentials for an HTTP request.
 *
 * For now, just enough info to support GET requests.  This could
 * be extended, though, maybe with an optional output stream to
 * represent the request body.
 *
 * Pass an HttpTaskRequest to an HttpTask to make it go.
 *
 * BSH
 */
public class HttpTaskRequest {
    private final String title;
    private final String url;
    private final String userName;
    private final String password;

    public HttpTaskRequest(String title, String url, String userName, String password) {
        this.title = title;
        this.url = url;
        this.userName = userName;
        this.password = password;
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
}
