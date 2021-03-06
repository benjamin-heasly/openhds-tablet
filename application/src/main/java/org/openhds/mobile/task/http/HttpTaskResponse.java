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
    private final boolean isSuccess;
    private final String message;
    private final int httpStatus;
    private final InputStream inputStream;
    private final String response;
    private final Object requestTag;

    public HttpTaskResponse(boolean isSuccess, String message, int httpStatus, InputStream inputStream, Object requestTag) {
        this.isSuccess = isSuccess;
        this.message = message;
        this.httpStatus = httpStatus;
        this.inputStream = inputStream;
        this.response = null;
        this.requestTag = requestTag;
    }

    public HttpTaskResponse(boolean isSuccess, String message, int httpStatus, String response, Object requestTag) {
        this.isSuccess = isSuccess;
        this.message = message;
        this.httpStatus = httpStatus;
        this.inputStream = null;
        this.response = response;
        this.requestTag = requestTag;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public String getMessage() {
        return message;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public String getResponse() {
        return response;
    }

    public Object getRequestTag() {
        return requestTag;
    }
}
