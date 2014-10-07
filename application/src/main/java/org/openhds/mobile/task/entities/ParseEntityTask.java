package org.openhds.mobile.task.entities;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.os.AsyncTask;
import org.openhds.mobile.utilities.DataPage;
import org.openhds.mobile.utilities.XmlPageParser;

/**
 * Carry out a ParseEntityTaskRequest.
 *
 * Read an input stream, parse it into entities, and save entities to the database.
 *
 * BSH
 */
public class ParseEntityTask extends AsyncTask<ParseEntityTaskRequest, Integer, Integer> {

    public static final int RESULT_OK = 0;
    public static final int RESULT_BAD_PARSE = 3;
    public static final int RESULT_CANCELED = 4;

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
        progressDialog.setTitle("Processing");
        progressDialog.setMessage("Stand By.");
        entityCount = 0;
    }

    @Override
    protected Integer doInBackground(ParseEntityTaskRequest... parseEntityTaskRequests) {
        parseEntityTaskRequest = parseEntityTaskRequests[0];

        // set up a page parser

        // pass input stream to page parser
        // catch for RESULT_BAD_PARSE

        // persist the trailing batch of entities

        return RESULT_OK;
    }

    @Override
    protected void onProgressUpdate (Integer... values) {
        entityCount = values[0];
        progressDialog.setTitle(parseEntityTaskRequest.getTitle());
        progressDialog.setMessage("Synced: " + entityCount);
    }

    @Override
    protected void onPostExecute (Integer result) {
        switch (result) {
            case RESULT_OK:
                progressDialog.setMessage("Great work!");
                return;
            case RESULT_BAD_PARSE:
                progressDialog.setMessage("Something went wrong parsing input stream.");
                return;
            case RESULT_CANCELED:
                progressDialog.setMessage("User canceled the sync.");
                return;
        }
    }

    private class EntityPageHandler implements XmlPageParser.PageHandler {
        @Override
        public boolean handlePage(DataPage dataPage) {
            // page to entity parser
            // check if canceled and return false
            // check if batch size and persist with gateway
            return true;
        }
    }

    private class EntityErrorHandler implements XmlPageParser.PageErrorHandler {
        @Override
        public boolean handlePageError(DataPage dataPage, Exception e) {
            // log and toast error
            // check if canceled and return false
            return true;
        }
    }
}
