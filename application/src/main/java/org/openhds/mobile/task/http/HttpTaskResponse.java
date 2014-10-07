package org.openhds.mobile.task.http;

import java.io.InputStream;

/**
 * Status and input stream from an HTTP request.
 *
 * Response status code and response body input stream
 * returned from an HttpTask.
 *
 * BSH
 */
public class HttpTaskResponse {
    private int statusCode;
    private InputStream inputStream;

    public HttpTaskResponse(int statusCode, InputStream inputStream) {
        this.statusCode = statusCode;
        this.inputStream = inputStream;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public InputStream getInputStream() {
        return inputStream;
    }
}
