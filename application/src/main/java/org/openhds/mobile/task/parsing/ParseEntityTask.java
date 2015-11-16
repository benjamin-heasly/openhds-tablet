package org.openhds.mobile.task.parsing;

import android.content.ContentResolver;
import android.os.AsyncTask;
import android.util.Log;

import java.util.List;

/**
 * Carry out a ParseEntityTaskRequest.
 *
 * Read an input stream, parse it into entities, and
 * save entities to the database in batches.
 *
 * BSH
 */
public class ParseEntityTask extends AsyncTask<ParseEntityTaskRequest, Integer, Integer> {

    private static final int BATCH_SIZE = 100;

    private final ContentResolver contentResolver;
    private final AbstractPageParser pageParser;

    private ParseEntityTaskRequest parseEntityTaskRequest;
    private ProgressListener progressListener;

    private int entityCount;

    private DataPage errorPage;
    private Exception lastError;

    public ParseEntityTask(ContentResolver contentResolver, AbstractPageParser pageParser) {
        this.contentResolver = contentResolver;
        this.pageParser = pageParser;

        this.pageParser.setPageHandler(new EntityPageHandler());
        this.pageParser.setPageErrorHandler(new EntityErrorHandler());
    }

    public void setProgressListener(ProgressListener progressListener) {
        this.progressListener = progressListener;
    }

    public void unhookInputStream() {
        if (null == parseEntityTaskRequest) {
            return;
        }
        parseEntityTaskRequest.setInputStream(null);
    }

    @Override
    protected void onPreExecute () {
        entityCount = 0;
    }

    @Override
    protected Integer doInBackground(ParseEntityTaskRequest... parseEntityTaskRequests) {
        parseEntityTaskRequest = parseEntityTaskRequests[0];

        // pass input stream to page parser
        try {
            pageParser.parsePages(parseEntityTaskRequest.getInputStream());
        } catch (Exception e) {
            Log.e(getClass().getName(), e.getMessage(), e);
            return -1;
        }

        persistBatch();
        return entityCount;
    }

    @Override
    protected void onProgressUpdate (Integer... values) {
        int progress = values[0];
        if (null != progressListener) {
            progressListener.onProgressReport(progress);
        }
    }

    @Override
    protected void onPostExecute (Integer result) {
        if (null != lastError) {
            progressListener.onError(errorPage, lastError);
            lastError = null;
            errorPage = null;
            return;
        }

        if (null != progressListener) {
            progressListener.onComplete(entityCount);
        }
    }

    public interface ProgressListener {
        void onProgressReport(int progress);
        void onError(DataPage dataPage, Exception e);
        void onComplete(int progress);
    }

    private void persistBatch() {
        List<?> entities = parseEntityTaskRequest.getEntityParser().getEntities();
        parseEntityTaskRequest.getGateway().insertMany(contentResolver, entities);
        entities.clear();
    }

    private class EntityPageHandler implements AbstractPageParser.PageHandler {
        @Override
        public boolean handlePage(DataPage dataPage) {
            // parse the new page into an entity
            parseEntityTaskRequest.getEntityParser().parsePage(dataPage);
            entityCount++;

            // persist entities in batches
            if (0 == entityCount % BATCH_SIZE) {
                persistBatch();
                publishProgress(entityCount);
            }

            // stop parsing if the user cancelled the task
            return !isCancelled();
        }
    }

    private class EntityErrorHandler implements AbstractPageParser.PageErrorHandler {
        @Override
        public boolean handlePageError(DataPage dataPage, Exception e) {
            // report the error on the UI thread
            errorPage = dataPage;
            lastError = e;
            publishProgress(entityCount);

            // stop parsing if the user cancelled the task
            return !isCancelled();
        }
    }
}
