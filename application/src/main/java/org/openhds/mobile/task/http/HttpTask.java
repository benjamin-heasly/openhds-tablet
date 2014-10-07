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
import java.net.URL;

/**
 * Carry out an HttpTaskRequest.
 *
 * Make an HTTP GET request with credentials, return response status and body.
 *
 * BSH
 */
public class HttpTask extends AsyncTask<HttpTaskRequest, Void, HttpTaskResponse> {
    public static final int RESULT_OK = 0;
    public static final int RESULT_BAD_REQUEST = 1;
    public static final int RESULT_BAD_CREDENTIALS = 2;
    public static final int RESULT_BAD_RESPONSE = 3;

    private HttpTaskResponseHandler httpTaskResponseHandler;

    // Require a handler to receive http results.
    public HttpTask(HttpTaskResponseHandler httpTaskResponseHandler) {
        this.httpTaskResponseHandler = httpTaskResponseHandler;
    }

    // Http GET with the given url and credentials.
    @Override
    protected HttpTaskResponse doInBackground(HttpTaskRequest... httpTaskRequests) {
        if (null == httpTaskRequests || 0 == httpTaskRequests.length) {
            return new HttpTaskResponse(RESULT_BAD_REQUEST, null);
        }
        HttpTaskRequest httpTaskRequest = httpTaskRequests[0];

        // build an http client with url and credentials
        DefaultHttpClient httpClient = new DefaultHttpClient();
        setHttpClientCredentials(httpClient, httpTaskRequest.getUserName(), httpTaskRequest.getPassword());

        // execute a GET request from the client
        try {
            HttpResponse httpResponse = executeHttpGet(httpClient, httpTaskRequest.getUrl());
            InputStream responseStream = httpResponse.getEntity().getContent();

            // make up an appropriate task response
            final int statusCode = httpResponse.getStatusLine().getStatusCode();
            switch (statusCode) {
                case HttpStatus.SC_OK:
                    return new HttpTaskResponse(RESULT_OK, responseStream);

                case HttpStatus.SC_FORBIDDEN:
                    return new HttpTaskResponse(RESULT_BAD_CREDENTIALS, responseStream);

                default:
                    if (statusCode < HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                        return new HttpTaskResponse(RESULT_BAD_REQUEST, responseStream);
                    }
                    return new HttpTaskResponse(RESULT_BAD_RESPONSE, responseStream);
            }

        } catch (IOException e) {
            return new HttpTaskResponse(RESULT_BAD_REQUEST, null);

        } catch (AuthenticationException e) {
            return new HttpTaskResponse(RESULT_BAD_CREDENTIALS, null);
        }
    }

    // Add credentials to this http cleint.
    private void setHttpClientCredentials(DefaultHttpClient httpClient, String userName, String password) {
        AuthScope scope = new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, AuthScope.ANY_REALM);
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(userName, password);
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(scope, credentials);
        httpClient.setCredentialsProvider(credentialsProvider);
    }

    // Make a request with preemptive basic credentials.
    private HttpResponse executeHttpGet(DefaultHttpClient httpClient, String hostAddress)
            throws  IOException, AuthenticationException {

        URL getUrl = new URL(hostAddress);
        HttpGet httpGet = new HttpGet(getUrl.getPath());
        HttpHost httpHost = new HttpHost(getUrl.getHost(), getUrl.getDefaultPort());

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
