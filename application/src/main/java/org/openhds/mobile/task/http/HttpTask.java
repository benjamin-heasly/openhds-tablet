package org.openhds.mobile.task.http;

import android.os.AsyncTask;
import android.util.Base64;

import com.google.common.io.ByteStreams;

import org.apache.http.HttpStatus;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Carry out an HttpTaskRequest.
 * <p/>
 * Make an HTTP GET request with credentials, return response status and body.
 * <p/>
 * BSH
 */
public class HttpTask extends AsyncTask<HttpTaskRequest, HttpTaskResponse, Void> {
    public static final String MESSAGE_SUCCESS = "Request successful";
    public static final String MESSAGE_NO_REQUEST = "No request given";
    public static final String MESSAGE_CLIENT_ERROR = "Client error";
    public static final String MESSAGE_BAD_URL = "Bad URL";
    public static final String MESSAGE_SERVER_ERROR = "Server error";

    private HttpTaskResponseHandler httpTaskResponseHandler;

    // A handler type to receive response status code and response body input stream.
    public interface HttpTaskResponseHandler {
        void handleHttpTaskResponse(HttpTaskResponse httpTaskResponse);
    }

    // Require a handler to receive http results.
    public HttpTask(HttpTaskResponseHandler httpTaskResponseHandler) {
        this.httpTaskResponseHandler = httpTaskResponseHandler;
    }

    @Override
    protected Void doInBackground(HttpTaskRequest... httpTaskRequests) {
        if (null == httpTaskRequests) {
            return null;
        }

        for (HttpTaskRequest httpTaskRequest : httpTaskRequests) {
            publishProgress(performRequest(httpTaskRequest));
        }

        return null;
    }

    // Forward the Http response to the handler.
    @Override
    protected void onProgressUpdate(HttpTaskResponse... responses) {
        if (null == responses || null == httpTaskResponseHandler) {
            return;
        }

        for (HttpTaskResponse response : responses) {
            httpTaskResponseHandler.handleHttpTaskResponse(response);
        }
    }

    /**
     * HTTP requests are now issued by HttpURLConnection, the recommended method for android > 2.3
     * URLs with the 'https' scheme return the HttpsURLConnection subclass automatically.
     */
    private HttpTaskResponse performRequest(HttpTaskRequest httpTaskRequest) {
        String rawCredentials = httpTaskRequest.getUserName() + ":" + httpTaskRequest.getPassword();
        String basicAuthHeader = "Basic " + Base64.encodeToString(rawCredentials.getBytes(), Base64.DEFAULT);

        HttpURLConnection urlConnection = null;
        InputStream responseStream = null;
        int statusCode = 0;
        try {
            URL url = new URL(httpTaskRequest.getUrl());
            urlConnection = (HttpURLConnection) url.openConnection();

            if (httpTaskRequest.getMethod() != null) {
                urlConnection.setRequestMethod(httpTaskRequest.getMethod());
            }

            if (httpTaskRequest.getContentType() != null) {
                urlConnection.setRequestProperty("Content-Type", httpTaskRequest.getContentType());
            }

            if (httpTaskRequest.getAccept() != null) {
                urlConnection.setRequestProperty("Accept", httpTaskRequest.getAccept());
            }

            urlConnection.setRequestProperty("Authorization", basicAuthHeader);

            // copy request stream to connection
            if (httpTaskRequest.getBody() != null) {
                urlConnection.setDoOutput(true);
                urlConnection.setUseCaches(false);
                urlConnection.setChunkedStreamingMode(0);

                InputStream is = httpTaskRequest.getBody();
                OutputStream os = urlConnection.getOutputStream();
                ByteStreams.copy(is, os);
                os.close();
                is.close();
            }

            responseStream = urlConnection.getInputStream();
            statusCode = urlConnection.getResponseCode();

        } catch (Exception e) {
            return new HttpTaskResponse(false, e.getClass().getSimpleName() + ": " + e.getMessage(), 0, null, httpTaskRequest.getTag());
        }

        if (HttpStatus.SC_OK == statusCode
                || HttpStatus.SC_CREATED == statusCode
                || HttpStatus.SC_ACCEPTED == statusCode
                || HttpStatus.SC_NO_CONTENT == statusCode) {
            return new HttpTaskResponse(true, MESSAGE_SUCCESS, statusCode, responseStream, httpTaskRequest.getTag());
        }

        if (statusCode < HttpStatus.SC_INTERNAL_SERVER_ERROR) {
            return new HttpTaskResponse(false, MESSAGE_CLIENT_ERROR, statusCode, responseStream, httpTaskRequest.getTag());
        }

        return new HttpTaskResponse(false, MESSAGE_SERVER_ERROR, statusCode, responseStream, httpTaskRequest.getTag());
    }

}
