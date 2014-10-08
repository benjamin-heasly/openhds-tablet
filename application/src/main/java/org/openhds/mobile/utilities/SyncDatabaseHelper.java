package org.openhds.mobile.utilities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import org.openhds.mobile.task.http.HttpTask;
import org.openhds.mobile.task.http.HttpTaskRequest;
import org.openhds.mobile.task.http.HttpTaskResponse;
import org.openhds.mobile.task.parsing.ParseEntityTask;
import org.openhds.mobile.task.parsing.ParseEntityTaskRequest;

import java.io.InputStream;

import static org.openhds.mobile.utilities.MessageUtils.showLongToast;

/**
 * Request entity data from the server and parse it into entities.
 *
 * Combines an HttpTask, which gets data from the server, with a
 * ParseEntityTask, which parses the data stream and puts entities
 * into the OpenHDS database.
 *
 * Publishes updates to a progress dialog during parsing.  The user
 * may cancel the parsing by clicking away from the progress dialog
 * then confirming by clicking a "yes" button.
 *
 * To make it go, pass in an HttpTaskRequest and a ParseEntityTaskRequest
 * to startSync().  The InputStream from the HttpTaskRequest will be
 * assigned to the ParseEntityRequest automatically.
 *
 * Notifies a listener when syncing is complete.
 *
 * BSH
 */
public class SyncDatabaseHelper {

    private Context context;
    private ProgressDialog progressDialog;

    private HttpTask httpTask;
    private HttpTaskRequest httpTaskRequest;
    private ParseEntityTask parseEntityTask;
    private ParseEntityTaskRequest<?> parseEntityTaskRequest;
    private SyncCompleteListener syncCompleteListener;

    public SyncDatabaseHelper(Context context) {
        this.context = context;
    }

    public void setProgressDialog(ProgressDialog progressDialog) {
        this.progressDialog = progressDialog;

        if (null != progressDialog) {
            progressDialog.setCancelable(true);
            progressDialog.setOnCancelListener(new SyncCancelListener());
        }
    }

    public void setSyncCompleteListener(SyncCompleteListener syncCompleteListener) {
        this.syncCompleteListener = syncCompleteListener;
    }

    public void startSync(HttpTaskRequest httpTaskRequest, ParseEntityTaskRequest<?> parseEntityTaskRequest) {
        this.httpTaskRequest = httpTaskRequest;
        this.parseEntityTaskRequest = parseEntityTaskRequest;

        startHttpTask();
    }

    public interface SyncCompleteListener {
        public void onSyncComplete();
        public void onSyncError();
    }

    private void updateProgressDialog(String title, String message) {
        if (null == progressDialog) {
            return;
        }

        progressDialog.show();

        if (null != title) {
            progressDialog.setTitle(title);
        }

        if (null != message) {
            progressDialog.setMessage(message);
        }
    }

    private void hideProgressDialog() {
        if (null == progressDialog) {
            return;
        }
        progressDialog.show();
    }

    // Connect to server and get a data stream.
    private void startHttpTask(){
        updateProgressDialog(httpTaskRequest.getTitle(), "Connecting");

        httpTask = new HttpTask(new HttpResponseHandler());
        httpTask.execute(httpTaskRequest);
    }

    // Feed data stream to the parse task.
    private void httpResponseToParseTask(HttpTaskResponse httpTaskResponse) {
        updateProgressDialog(parseEntityTaskRequest.getTitle(), "Reading");

        if (!httpTaskResponse.isSuccess()) {
            String message = "Error " + httpTaskResponse.getHttpStatus() + ": " + httpTaskResponse.getMessage();
            showLongToast(context, message);
            updateProgressDialog(httpTaskRequest.getTitle(), message);

            if (null != syncCompleteListener) {
                syncCompleteListener.onSyncError();
            }
            hideProgressDialog();
            return;
        }

        // clear out the old database
        parseEntityTaskRequest.getGateway().deleteAll(context.getContentResolver());

        InputStream inputStream = httpTaskResponse.getInputStream();
        parseEntityTask = new ParseEntityTask(context.getContentResolver());
        parseEntityTask.setProgressListener(new ParseProgressListener());
        parseEntityTaskRequest.setInputStream(inputStream);
        parseEntityTask.execute(parseEntityTaskRequest);
    }

    // All done parsing, clean up.
    private void finishParseTask(int progress) {
        String message = "Complete" + ": " + Integer.toString(progress);
        showLongToast(context, message);
        updateProgressDialog(parseEntityTaskRequest.getTitle(), message);

        if (null != syncCompleteListener) {
            syncCompleteListener.onSyncComplete();
        }

        hideProgressDialog();
    }

    // User canceled the sync.
    private void cancelSync() {
        String message = "Canceled";
        showLongToast(context, message);
        updateProgressDialog(parseEntityTaskRequest.getTitle(), message);

        parseEntityTask.cancel(true);
        hideProgressDialog();
    }

    private class HttpResponseHandler implements HttpTask.HttpTaskResponseHandler {
        @Override
        public void handleHttpTaskResponse(HttpTaskResponse httpTaskResponse) {
            httpResponseToParseTask(httpTaskResponse);
        }
    }

    private class ParseProgressListener implements ParseEntityTask.ProgressListener {
        @Override
        public void onProgressReport(int progress) {
            updateProgressDialog(parseEntityTaskRequest.getTitle(), Integer.toString(progress));
        }

        @Override
        public void onComplete(int progress) {
            finishParseTask(progress);
        }
    }

    private class SyncCancelListener implements OnCancelListener {
        @Override
        public void onCancel(DialogInterface dialog) {
            SyncCancelConfirmListener syncCancelConfirmListener = new SyncCancelConfirmListener();
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage("Are you sure you want to stop sync?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", syncCancelConfirmListener)
                    .setNegativeButton("No", syncCancelConfirmListener);
            builder.create().show();
        }
    }

    private class SyncCancelConfirmListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialogInterface, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    cancelSync();
                    return;

                case DialogInterface.BUTTON_NEGATIVE:
                    if (httpTask.getStatus() == AsyncTask.Status.RUNNING
                            || parseEntityTask.getStatus() == AsyncTask.Status.RUNNING) {
                        updateProgressDialog(null, null);
                    }
                    return;
            }
        }
    }
}
