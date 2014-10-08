package org.openhds.mobile.task.parsing;

import android.app.ProgressDialog;
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

    private ProgressDialog progressDialog;
    private ContentResolver contentResolver;
    private ParseEntityTaskRequest parseEntityTaskRequest;

    private int entityCount;

    public ParseEntityTask(ProgressDialog progressDialog, ContentResolver contentResolver) {
        this.progressDialog = progressDialog;
        this.contentResolver = contentResolver;
    }

    @Override
    protected void onPreExecute () {
        updateDialog("Processing", "Stand By.");
        entityCount = 0;
    }

    @Override
    protected Integer doInBackground(ParseEntityTaskRequest... parseEntityTaskRequests) {
        parseEntityTaskRequest = parseEntityTaskRequests[0];

        // set up a page parser
        XmlPageParser xmlPageParser = new XmlPageParser();
        xmlPageParser.setPageHandler(new EntityPageHandler());
        xmlPageParser.setPageErrorHandler(new EntityErrorHandler());

        // pass input stream to page parser
        try {
            xmlPageParser.parsePages(parseEntityTaskRequest.getInputStream());
        } catch (Exception e) {
            Log.e(getClass().getName(), e.getMessage(), e);
            return -1;
        }

        persistBatch();
        return entityCount;
    }

    @Override
    protected void onProgressUpdate (Integer... values) {
        entityCount = values[0];
        updateDialog(parseEntityTaskRequest.getTitle(), Integer.toString(entityCount));
    }

    @Override
    protected void onPostExecute (Integer result) {
        if (result < 0) {
            updateDialog(parseEntityTaskRequest.getTitle(), Integer.toString(result));
            return;
        }
        updateDialog(parseEntityTaskRequest.getTitle(), Integer.toString(result));
    }

    private void persistBatch() {
        List<?> entities = parseEntityTaskRequest.getEntityParser().getEntities();
        parseEntityTaskRequest.getGateway().insertMany(contentResolver, entities);
        entities.clear();
    }

    private void updateDialog(String title, String message) {
        if (null == progressDialog) {
            return;
        }

        if (null != title) {
            progressDialog.setTitle(title);
        }

        if (null != message) {
            progressDialog.setMessage(message);
        }
    }

    private class EntityPageHandler implements XmlPageParser.PageHandler {
        @Override
        public boolean handlePage(DataPage dataPage) {
            // parse the new page into an entity
            parseEntityTaskRequest.getEntityParser().parsePage(dataPage);
            entityCount++;

            // persist entities in batches
            if (0 == entityCount % BATCH_SIZE) {
                persistBatch();
            }

            // stop parsing if the user cancelled the task
            return !isCancelled();
        }
    }

    private class EntityErrorHandler implements XmlPageParser.PageErrorHandler {
        @Override
        public boolean handlePageError(DataPage dataPage, Exception e) {
            Log.e(getClass().getName(), "Error parsing page: " + dataPage.getPageDescription(), e);

            // stop parsing if the user cancelled the task
            return !isCancelled();
        }
    }
}
