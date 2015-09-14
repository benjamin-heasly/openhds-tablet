package org.openhds.mobile.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.openhds.mobile.R;
import org.openhds.mobile.activity.OpeningActivity;
import org.openhds.mobile.links.Link;
import org.openhds.mobile.links.RelInterpretation;
import org.openhds.mobile.links.ResourceLinkRegistry;
import org.openhds.mobile.repository.gateway.Gateway;
import org.openhds.mobile.task.http.HttpTask;
import org.openhds.mobile.task.http.HttpTaskRequest;
import org.openhds.mobile.task.http.HttpTaskResponse;
import org.openhds.mobile.task.parsing.DataPage;
import org.openhds.mobile.task.parsing.ParseEntityTask;
import org.openhds.mobile.task.parsing.ParseEntityTaskRequest;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import static org.openhds.mobile.utilities.ConfigUtils.getResourceString;
import static org.openhds.mobile.utilities.MessageUtils.showLongToast;

/**
 * Allow user to sync tables with the server.
 *
 * Shows a table with sync status and progress for each entity/table.
 * The user may sync one table at a time or queue up all tables at once.
 *
 * BSH
 */
public class SyncDatabaseFragment extends Fragment {

    // placeholder for integer value to ignore
    private static final int IGNORE = -1;
    private static final int UNKNOWN = -2;
    private static final String UNKNOWN_TEXT = "-";

    private HttpTask httpTask;
    private ParseEntityTask parseEntityTask;
    private Queue<RelInterpretation<?>> queuedEntities;
    private RelInterpretation<?> currentEntity;
    private Map<RelInterpretation<?>, Integer> allErrorCounts;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        queuedEntities = new ArrayDeque<>();
        allErrorCounts = new HashMap<>();
        currentEntity = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sync_database_fragment, container, false);

        TableLayout tableLayout = (TableLayout) view.findViewById(R.id.sync_summary_table);
        View.OnClickListener actionButtonListener = new ActionButtonListener();
        for (String rel : ResourceLinkRegistry.activeRels()) {
            RelInterpretation<?> interpretation = ResourceLinkRegistry.getInterpretation(rel);

            TableRow tableRow = (TableRow) inflater.inflate(R.layout.sync_database_row, container, false);
            tableRow.setTag(interpretation);
            tableLayout.addView(tableRow);

            Button actionButton = (Button) tableRow.findViewById(R.id.action_column);
            actionButton.setOnClickListener(actionButtonListener);
            actionButton.setTag(interpretation);
        }

        Button syncAllButton = (Button) view.findViewById(R.id.sync_all_button);
        syncAllButton.setOnClickListener(new SyncAllButtonListener());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        // update entity record counts directly from the database
        for (String rel : ResourceLinkRegistry.activeRels()) {
            RelInterpretation<?> interpretation = ResourceLinkRegistry.getInterpretation(rel);
            resetTableRow(interpretation);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        terminateSync(true);
    }

    // Refresh a table with stored data and ready to sync.
    private void resetTableRow(RelInterpretation<?> interpretation) {
        int records = queryRecordCount(interpretation);
        int errors = allErrorCounts.containsKey(interpretation) ? allErrorCounts.get(interpretation) : UNKNOWN;
        updateTableRow(interpretation, records, errors, R.string.sync_database_button_sync);
    }

    // Query the database for entity record counts.
    private int queryRecordCount(RelInterpretation<?> interpretation) {
        Gateway gateway = interpretation.getGateway();
        return gateway.countAll(getActivity().getContentResolver());
    }

    // Add an entity to the queue to be synced.
    private void enqueueEntity(RelInterpretation<?> interpretation) {
        if (currentEntity == interpretation || queuedEntities.contains(interpretation)) {
            return;
        }

        // mark the table row for this entity as "waiting"
        updateTableRow(interpretation, UNKNOWN, UNKNOWN, R.string.sync_database_button_waiting);

        // add this entity to the queue and run it if ready
        queuedEntities.add(interpretation);
        startNextEntity();
    }

    // Take the next entity off the queue and start the sync process.
    private void startNextEntity() {
        if (null != currentEntity || queuedEntities.isEmpty()) {
            return;
        }

        // choose the next entity to sync
        currentEntity = queuedEntities.remove();

        // reset the table row for this entity
        allErrorCounts.put(currentEntity, 0);
        updateTableRow(currentEntity, UNKNOWN, 0, R.string.sync_database_button_cancel);

        // start an http task for this entity
        httpTask = new HttpTask(new HttpResponseHandler());
        HttpTaskRequest httpTaskRequest = buildHttpTaskRequest(currentEntity);
        httpTask.execute(httpTaskRequest);
    }

    // Pass http data stream to the entity parser.
    private void httpResultToParser(HttpTaskResponse httpTaskResponse) {
        parseEntityTask = new ParseEntityTask(getActivity().getContentResolver());
        parseEntityTask.setProgressListener(new ParseProgressListener());

        ParseEntityTaskRequest parseEntityTaskRequest = buildParseRequest(currentEntity);
        parseEntityTaskRequest.setInputStream(httpTaskResponse.getInputStream());

        parseEntityTaskRequest.getGateway().deleteAll(getActivity().getContentResolver());
        parseEntityTask.execute(parseEntityTaskRequest);
    }

    // Request to parse entities re.
    private static ParseEntityTaskRequest<?> buildParseRequest(RelInterpretation<?> interpretation) {
        // cast asserts to compiler that parser and gateway agree on <T>
        final RelInterpretation<Object> objectInterpretation = (RelInterpretation<Object>) interpretation;
        return new ParseEntityTaskRequest<>(
                objectInterpretation.getLabel(),
                null,
                objectInterpretation.getParser(),
                objectInterpretation.getGateway());
    }


    // Clean up after the entity parser is all done.
    private void finishEntity() {
        int records = queryRecordCount(currentEntity);
        updateTableRow(currentEntity, records, allErrorCounts.get(currentEntity), R.string.sync_database_button_sync);
        showProgressMessage(currentEntity.getLabel(), Integer.toString(records));
        terminateSync(false);
    }

    // Clean up tasks.  If a isError is true, counts as an error for the running task.
    private void terminateSync(boolean isError) {
        if (null != currentEntity) {
            // a task is currently running

            // refresh the error count
            int errorCount = allErrorCounts.get(currentEntity);
            if (isError) {
                errorCount++;
                allErrorCounts.put(currentEntity, errorCount);
            }
            updateTableRow(currentEntity, IGNORE, errorCount, R.string.sync_database_button_sync);

            // unhook the parse entity task request from the http input stream
            parseEntityTask.unhookUnputStream();
        }
        currentEntity = null;

        if (null != httpTask) {
            httpTask.cancel(true);
        }
        httpTask = null;

        if (null != parseEntityTask) {
            parseEntityTask.cancel(true);
        }
        parseEntityTask = null;

        // proceed to the next entity if any
        startNextEntity();
    }

    // Show an error by logging, and toasting.
    private void showError(int entityId, int errorCode, String errorMessage) {
        String entityName = getResourceString(getActivity(), entityId);
        String message = "Error syncing " + entityName + " (" + Integer.toString(errorCode) + "):" + errorMessage;
        Log.e(entityName, message);
        showLongToast(getActivity(), message);
    }

    // Show progress by toasting.
    private void showProgressMessage(int entityId, String progressMessage) {
        String entityName = getResourceString(getActivity(), entityId);
        String message = entityName + ": " + progressMessage;
        showLongToast(getActivity(), message);
    }

    // Update column values and button status.
    private void updateTableRow(RelInterpretation<?> interpretation, int records, int errors, int actionId) {
        View view = getView();
        if (null == view) {
            return;
        }

        TableRow tableRow = (TableRow) view.findViewWithTag(interpretation);
        TextView entityText = (TextView) tableRow.findViewById(R.id.entity_column);
        TextView recordsText = (TextView) tableRow.findViewById(R.id.records_column);
        TextView errorsText = (TextView) tableRow.findViewById(R.id.errors_column);
        Button actionButton = (Button) tableRow.findViewById(R.id.action_column);

        entityText.setText(interpretation.getLabel());

        if (IGNORE != records) {
            if (UNKNOWN == records) {
                recordsText.setText(UNKNOWN_TEXT);
            } else {
                recordsText.setText(Integer.toString(records));
            }
        }

        if (IGNORE != errors) {
            if (UNKNOWN == errors) {
                errorsText.setText(UNKNOWN_TEXT);
            } else {
                errorsText.setText(Integer.toString(errors));
            }
        }

        if (IGNORE != actionId) {
            actionButton.setText(actionId);
            actionButton.setTag(interpretation);
        }
    }

    // Create an http task request for fetching data from the server.
    private HttpTaskRequest buildHttpTaskRequest(RelInterpretation<?> interpretation) {
        String userName = (String) getActivity().getIntent().getExtras().get(OpeningActivity.USERNAME_KEY);
        String password = (String) getActivity().getIntent().getExtras().get(OpeningActivity.PASSWORD_KEY);

        Link link = ResourceLinkRegistry.getLink(interpretation.getRel());
        return new HttpTaskRequest(interpretation.getLabel(), link.getUrl(), "application/json", userName, password);
    }

    // Respond to "sync all" button.
    private class SyncAllButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            for (String rel : ResourceLinkRegistry.activeRels()) {
                enqueueEntity(ResourceLinkRegistry.getInterpretation(rel));
            }
        }
    }

    // Respond to individual entity "sync" buttons.
    private class ActionButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            // which button is this?
            RelInterpretation<?> interpretation = (RelInterpretation<?>) view.getTag();

            if (interpretation == currentEntity) {
                // button should change from "cancel" to "sync"
                terminateSync(true);
                showProgressMessage(interpretation.getLabel(), getResourceString(getActivity(), R.string.sync_database_canceled));

            } else if (queuedEntities.contains(interpretation)) {
                // button should change "waiting" to "sync"
                queuedEntities.remove(interpretation);
                resetTableRow(interpretation);

            } else {
                // button should change from "sync" to "waiting"
                enqueueEntity(interpretation);
            }
        }
    }

    // Receive http response from server, or error data.
    private class HttpResponseHandler implements HttpTask.HttpTaskResponseHandler {
        @Override
        public void handleHttpTaskResponse(HttpTaskResponse httpTaskResponse) {
            if (!httpTaskResponse.isSuccess()) {
                showError(currentEntity.getLabel(), httpTaskResponse.getHttpStatus(), httpTaskResponse.getMessage());
                terminateSync(true);
                return;
            }
            httpResultToParser(httpTaskResponse);
        }
    }

    // Receive progress reports from parser, or error data.
    private class ParseProgressListener implements ParseEntityTask.ProgressListener {
        @Override
        public void onProgressReport(int progress) {
            updateTableRow(currentEntity, progress, IGNORE, IGNORE);
        }

        @Override
        public void onError(DataPage dataPage, Exception e) {
            int errorCount = allErrorCounts.get(currentEntity);
            errorCount++;
            allErrorCounts.put(currentEntity, errorCount);
            updateTableRow(currentEntity, IGNORE, errorCount, IGNORE);
            showError(currentEntity.getLabel(), 0, e.getMessage());
        }

        @Override
        public void onComplete(int progress) {
            finishEntity();
        }
    }
}
