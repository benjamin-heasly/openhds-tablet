package org.openhds.mobile.tests;

import android.test.AndroidTestCase;
import org.openhds.mobile.task.http.HttpTask;
import org.openhds.mobile.task.http.HttpTaskRequest;
import org.openhds.mobile.task.http.HttpTaskResponse;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/**
 * Try to get some data from google.com.
 *
 * BSH
 */
public class HttpTaskTest  extends AndroidTestCase {

    private static final String GOOGLE_URL =  "http://www.google.com";
    private static final long TASK_TIMEOUT = 10;

    HttpTaskResponse httpTaskResponse;

    public void testGetGoogle() throws Exception {
        HttpTaskRequest httpTaskRequest = new HttpTaskRequest("test", GOOGLE_URL, "", "");
        HttpTask httpTask = new HttpTask(new ResponseHandler());
        httpTask.execute(httpTaskRequest);

        // wait for the task to complete
        httpTask.get(TASK_TIMEOUT, TimeUnit.SECONDS);

        assertNotNull(httpTaskResponse);
        assertEquals(HttpTask.RESULT_OK, httpTaskResponse.getStatusCode());

        InputStream inputStream = httpTaskResponse.getInputStream();
        assertNotNull(inputStream);
        inputStream.close();
    }

    private class ResponseHandler implements HttpTask.HttpTaskResponseHandler {
        @Override
        public void handleHttpTaskResponse(HttpTaskResponse httpTaskResponse) {
            HttpTaskTest.this.httpTaskResponse = httpTaskResponse;
        }
    }
}
