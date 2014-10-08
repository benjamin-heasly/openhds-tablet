package org.openhds.mobile.task.http;

import android.os.AsyncTask;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
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
    public static final String MESSAGE_SERVER_ERROR = "Server error";

    private HttpTaskResponseHandler httpTaskResponseHandler;

    // Require a handler to receive http results.
    public HttpTask(HttpTaskResponseHandler httpTaskResponseHandler) {
        this.httpTaskResponseHandler = httpTaskResponseHandler;
    }

    // Http GET with the given url and credentials.
    @Override
    protected HttpTaskResponse doInBackground(HttpTaskRequest... httpTaskRequests) {
        if (null == httpTaskRequests || 0 == httpTaskRequests.length) {
            return new HttpTaskResponse(false, MESSAGE_NO_REQUEST, 0, null);
        }
        HttpTaskRequest httpTaskRequest = httpTaskRequests[0];

        // build an http client with url and credentials
        DefaultHttpClient httpClient = new DefaultHttpClient();
        setHttpClientCredentials(httpClient, httpTaskRequest.getUserName(), httpTaskRequest.getPassword());

        // execute a GET request from the client
        try {
            HttpResponse httpResponse = executeHttpGet(httpClient, httpTaskRequest.getUrl());
            InputStream responseStream = httpResponse.getEntity().getContent();
            int statusCode = httpResponse.getStatusLine().getStatusCode();

            if (HttpStatus.SC_OK == statusCode) {
                return new HttpTaskResponse(true, MESSAGE_SUCCESS, statusCode, responseStream);
            }

            if (statusCode < HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                return new HttpTaskResponse(false, MESSAGE_CLIENT_ERROR, statusCode, responseStream);
            }

            return new HttpTaskResponse(false, MESSAGE_SERVER_ERROR, statusCode, responseStream);

        } catch (Exception e) {
            return new HttpTaskResponse(false, e.getClass().getName() +": " + e.getMessage(), 0, null);
        }
    }

    // Add credentials to this http client.
    private void setHttpClientCredentials(DefaultHttpClient httpClient, String userName, String password) {
        AuthScope scope = new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, AuthScope.ANY_REALM);
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(userName, password);
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(scope, credentials);
        httpClient.setCredentialsProvider(credentialsProvider);
    }

    // Make a request with preemptive basic credentials.
    private HttpResponse executeHttpGet(DefaultHttpClient httpClient, String hostAddress)
            throws  IOException, AuthenticationException, URISyntaxException {

        URL getUrl = new URL(hostAddress);
        HttpGet httpGet = new HttpGet(getUrl.toURI());
        HttpHost httpHost = new HttpHost(getUrl.getHost(), getUrl.getPort());

        // preemptive basic credentials
        Credentials credentials = httpClient.getCredentialsProvider().getCredentials(AuthScope.ANY);
        httpGet.addHeader(new BasicScheme().authenticate(credentials, httpGet));

        return httpClient.execute(httpHost, httpGet);
    }

    // Forward the Http response to the handler.
    @Override
    protected void onPostExecute(HttpTaskResponse httpTaskResponse) {
        if (null != httpTaskResponseHandler) {
            httpTaskResponseHandler.handleHttpTaskResponse(httpTaskResponse);
        }
    }

    // A handler type to receive reponse status code and response body input stream.
    public interface HttpTaskResponseHandler {
        public void handleHttpTaskResponse(HttpTaskResponse httpTaskResponse);
    }
}
