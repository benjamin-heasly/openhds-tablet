package org.openhds.mobile.task;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.os.AsyncTask;
import org.openhds.mobile.utilities.DataPage;
import org.openhds.mobile.utilities.XmlPageParser;

/**
 * Carry out a SyncEntityRequest.
 *
 * Request a stream of data from the OpenHDS server, parse the
 * stream into entities, and save entities to the database.
 *
 * BSH
 */
public class SyncEntityTask extends AsyncTask<SyncEntityRequest, Integer, Integer> {

    public static final int RESULT_OK = 0;
    public static final int RESULT_BAD_SERVER = 1;
    public static final int RESULT_BAD_AUTH = 2;
    public static final int RESULT_BAD_PARSE = 3;
    public static final int RESULT_CANCELED = 4;

    private static final int BATCH_SIZE = 100;

    private ProgressDialog progressDialog;
    private ContentResolver contentResolver;
    private SyncEntityRequest syncEntityRequest;

    private int entityCount;

    public SyncEntityTask(ProgressDialog progressDialog, ContentResolver contentResolver) {
        this.progressDialog = progressDialog;
        this.contentResolver = contentResolver;
    }

    @Override
    protected void onPreExecute () {
        progressDialog.setTitle("Contacting Server");
        progressDialog.setMessage("Stand By.");
        entityCount = 0;
    }

    @Override
    protected Integer doInBackground(SyncEntityRequest... syncEntityRequests) {
        syncEntityRequest = syncEntityRequests[0];

        // build request
        // make request
        // RESULT_BAD_SERVER or RESULT_BAD_AUTH ?

        // pass response to page parser
        // catch for RESULT_BAD_PARSE

        // persist last batch of entities

        return RESULT_OK;
    }

    @Override
    protected void onProgressUpdate (Integer... values) {
        entityCount = values[0];
        progressDialog.setTitle(syncEntityRequest.getTitle());
        progressDialog.setMessage("Synced: " + entityCount);
    }

    @Override
    protected void onPostExecute (Integer result) {
        switch (result) {
            case RESULT_OK:
                progressDialog.setMessage("Great work!");
                return;
            case RESULT_BAD_SERVER:
                progressDialog.setMessage("Could not connect to " + syncEntityRequest.getUrl());
                return;
            case RESULT_BAD_AUTH:
                progressDialog.setMessage("Could not authenticate as " + syncEntityRequest.getUserName());
                return;
            case RESULT_BAD_PARSE:
                progressDialog.setMessage("Something went very wrong parsing response.");
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
            // log and report error
            // check if canceled and return false
            return true;
        }
    }
}
