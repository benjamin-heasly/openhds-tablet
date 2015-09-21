package org.openhds.mobile.utilities;

import android.content.ContentResolver;
import android.os.AsyncTask;

import org.openhds.mobile.links.Link;
import org.openhds.mobile.links.RelInterpretation;
import org.openhds.mobile.links.ResourceLinkRegistry;
import org.openhds.mobile.task.http.HttpTask;
import org.openhds.mobile.task.http.HttpTaskRequest;
import org.openhds.mobile.task.http.HttpTaskResponse;
import org.openhds.mobile.task.parsing.DataPage;
import org.openhds.mobile.task.parsing.ParseEntityTask;
import org.openhds.mobile.task.parsing.ParseEntityTaskRequest;
import org.openhds.mobile.task.parsing.entities.ParseLinksTask;

import java.io.InputStream;
import java.util.Map;

/**
 * Created by ben on 9/21/15.
 *
 * Coordinate several background tasks required for syncing a table against a server resource.
 *
 */
public class SyncDatabaseHelper {

    private final RelInterpretation<?> relInterpretation;

    private final SyncDatabaseListener listener;

    private final String username;

    private final String password;

    private final ContentResolver contentResolver;

    public String resourceRel = "bydatebulk";

    public String linkMediaType = "application/hal+json";

    public String dataMediaType = "application/json";

    private HttpTask getLinksTask = null;

    private ParseLinksTask parseLinksTask = null;

    private HttpTask getDataTask = null;

    private ParseEntityTask parseEntityTask = null;

    private int errorCount = 0;

    public SyncDatabaseHelper(RelInterpretation<?> relInterpretation, SyncDatabaseListener listener, String username, String password, ContentResolver contentResolver) {
        this.relInterpretation = relInterpretation;
        this.listener = listener;
        this.username = username;
        this.password = password;
        this.contentResolver = contentResolver;
    }

    public interface SyncDatabaseListener {
        void onGotLinks(RelInterpretation<?> relInterpretation);

        void onParsedLinks(RelInterpretation<?> relInterpretation);

        void onGotData(RelInterpretation<?> relInterpretation);

        void onParseDataProgress(RelInterpretation<?> relInterpretation, int progress);

        void onParseDataComplete(RelInterpretation<?> relInterpretation, int progress);

        void onError(RelInterpretation<?> relInterpretation, String message, int errorCount);
    }

    public String getResourceRel() {
        return resourceRel;
    }

    public void setResourceRel(String resourceRel) {
        this.resourceRel = resourceRel;
    }

    public String getLinkMediaType() {
        return linkMediaType;
    }

    public void setLinkMediaType(String linkMediaType) {
        this.linkMediaType = linkMediaType;
    }

    public String getDataMediaType() {
        return dataMediaType;
    }

    public void setDataMediaType(String dataMediaType) {
        this.dataMediaType = dataMediaType;
    }

    public RelInterpretation<?> getRelInterpretation() {
        return relInterpretation;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void cancel() {
        cancelTask(getLinksTask);
        getLinksTask = null;

        cancelTask(parseLinksTask);
        parseLinksTask = null;

        cancelTask(getDataTask);
        getDataTask = null;

        cancelTask(parseEntityTask);
        parseEntityTask = null;
    }

    private void cancelTask(AsyncTask asyncTask) {
        if (null == asyncTask) {
            return;
        }
        asyncTask.cancel(true);
    }

    public void start() {
        cancel();

        Link link = ResourceLinkRegistry.getLink(relInterpretation.getRel());
        HttpTaskRequest getLinkRequest = new HttpTaskRequest(relInterpretation.getLabel(),
                link.getUrl(),
                linkMediaType,
                username,
                password);

        getLinksTask = new HttpTask(new GetLinksResponseHandler());
        getLinksTask.execute(getLinkRequest);
    }

    private class GetLinksResponseHandler implements HttpTask.HttpTaskResponseHandler {
        @Override
        public void handleHttpTaskResponse(HttpTaskResponse httpTaskResponse) {
            if (httpTaskResponse.isSuccess()) {
                listener.onGotLinks(relInterpretation);
                parseLinks(httpTaskResponse.getInputStream());
                return;
            }

            errorCount++;
            listener.onError(relInterpretation, httpTaskResponse.getMessage(), errorCount);
        }
    }

    private void parseLinks(InputStream inputStream) {
        parseLinksTask = new ParseLinksTask(new ParseLinksHandler());
        parseLinksTask.execute(inputStream);
    }

    private class ParseLinksHandler implements ParseLinksTask.LinkHandler {
        @Override
        public void handleLinks(Map<String, Link> links) {
            if (null != links && links.containsKey(resourceRel)) {
                listener.onParsedLinks(relInterpretation);
                getData(links.get(resourceRel));
                return;
            }

            errorCount++;
            listener.onError(relInterpretation, "No such rel from server: " + resourceRel, errorCount);
        }
    }

    private void getData(Link link) {
        HttpTaskRequest getDataRequest = new HttpTaskRequest(relInterpretation.getLabel(),
                link.getUrl(),
                dataMediaType,
                username,
                password);

        getDataTask = new HttpTask(new GetDataResponseHandler());
        getDataTask.execute(getDataRequest);
    }

    private class GetDataResponseHandler implements HttpTask.HttpTaskResponseHandler {
        @Override
        public void handleHttpTaskResponse(HttpTaskResponse httpTaskResponse) {
            if (httpTaskResponse.isSuccess()) {
                listener.onGotData(relInterpretation);
                parseData(httpTaskResponse.getInputStream());
                return;
            }

            errorCount++;
            listener.onError(relInterpretation, httpTaskResponse.getMessage(), errorCount);
        }
    }

    private void parseData(InputStream inputStream) {
        // cast asserts to compiler that parser and gateway agree on <T>
        final RelInterpretation<Object> interpretation = (RelInterpretation<Object>) relInterpretation;
        ParseEntityTaskRequest<Object> parseRequest = new ParseEntityTaskRequest<>(
                interpretation.getLabel(),
                inputStream,
                interpretation.getParser(),
                interpretation.getGateway());

        parseEntityTask = new ParseEntityTask(contentResolver);
        parseEntityTask.setProgressListener(new ParseProgressListener());
        parseEntityTask.execute(parseRequest);
    }

    private class ParseProgressListener implements ParseEntityTask.ProgressListener {
        @Override
        public void onProgressReport(int progress) {
            listener.onParseDataProgress(relInterpretation, progress);
        }

        @Override
        public void onError(DataPage dataPage, Exception e) {
            errorCount++;
            listener.onError(relInterpretation, e.getMessage(), errorCount);
        }

        @Override
        public void onComplete(int progress) {
            listener.onParseDataComplete(relInterpretation, progress);
            cancel();
        }
    }

}
