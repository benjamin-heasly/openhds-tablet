package org.openhds.mobile.task.http;

import android.os.AsyncTask;
import android.util.Base64;

import org.apache.http.HttpStatus;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Carry out an HttpTaskRequest.
 *
 * Make an HTTP GET request with credentials, return response status and body.
 *
 * BSH
 */
public class HttpTask extends AsyncTask<HttpTaskRequest, Void, HttpTaskResponse> {
    public static final String MESSAGE_SUCCESS = "Request successful";
    public static final String MESSAGE_NO_REQUEST = "No request given";
    public static final String MESSAGE_CLIENT_ERROR = "Client error";
    public static final String MESSAGE_BAD_URL = "Bad URL";
    public static final String MESSAGE_SERVER_ERROR = "Server error";

    private HttpTaskResponseHandler httpTaskResponseHandler;

    // Require a handler to receive http results.
    public HttpTask(HttpTaskResponseHandler httpTaskResponseHandler) {
        this.httpTaskResponseHandler = httpTaskResponseHandler;
    }

    /*
        HTTP requests are now issued by HttpURLConnection, the recommended method for android > 2.3
        URLs with the 'https' scheme return the HttpsURLConnection subclass automatically.
     */
    @Override
    protected HttpTaskResponse doInBackground(HttpTaskRequest... httpTaskRequests) {
        if (null == httpTaskRequests || 0 == httpTaskRequests.length) {
            return new HttpTaskResponse(false, MESSAGE_NO_REQUEST, 0, null);
        }
        final HttpTaskRequest httpTaskRequest = httpTaskRequests[0];

        String rawCredentials = httpTaskRequest.getUserName()+":"+httpTaskRequest.getPassword();
        String basicAuthHeader = "Basic "+ Base64.encodeToString(rawCredentials.getBytes(), Base64.DEFAULT);

        HttpURLConnection urlConnection;
        InputStream responseStream;
        int statusCode;
        try {
            URL url = new URL(httpTaskRequest.getUrl());
            urlConnection = (HttpURLConnection) url.openConnection();
            if (httpTaskRequest.getAccept() != null) {
                urlConnection.setRequestProperty("Accept", httpTaskRequest.getAccept());
            }
            urlConnection.setRequestProperty("Authorization", basicAuthHeader);
            responseStream = urlConnection.getInputStream();
            statusCode = urlConnection.getResponseCode();
        } catch (Exception e) {
            return new HttpTaskResponse(false, e.getClass().getSimpleName() + ": " + e.getMessage(), 0, null);
        }

        if (HttpStatus.SC_OK == statusCode) {
            return new HttpTaskResponse(true, MESSAGE_SUCCESS, statusCode, responseStream);
        }

        if (statusCode < HttpStatus.SC_INTERNAL_SERVER_ERROR) {
            return new HttpTaskResponse(false, MESSAGE_CLIENT_ERROR, statusCode, responseStream);
        }

        return new HttpTaskResponse(false, MESSAGE_SERVER_ERROR, statusCode, responseStream);


    }

    // Forward the Http response to the handler.
    @Override
    protected void onPostExecute(HttpTaskResponse httpTaskResponse) {
        if (null != httpTaskResponseHandler) {
            httpTaskResponseHandler.handleHttpTaskResponse(httpTaskResponse);
        }
    }

    // A handler type to receive response status code and response body input stream.
    public interface HttpTaskResponseHandler {
        void handleHttpTaskResponse(HttpTaskResponse httpTaskResponse);
    }
}
