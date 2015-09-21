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
import org.openhds.mobile.links.RelInterpretation;
import org.openhds.mobile.links.ResourceLinkRegistry;
import org.openhds.mobile.utilities.SyncDatabaseHelper;

import java.util.ArrayDeque;
import java.util.Queue;

import static org.openhds.mobile.utilities.ConfigUtils.getResourceString;
import static org.openhds.mobile.utilities.MessageUtils.showLongToast;

/**
 * Allow user to sync entities with the server.
 *
 * Shows a table with sync status and progress for each entity.
 * The user may sync one table at a time or queue up all tables at once.
 *
 * BSH
 */
public class SyncDatabaseFragment extends Fragment {

    // special case values for updating text widgets
    private static final int IGNORE = -1;
    private static final int UNKNOWN = -2;

    private Queue<SyncDatabaseHelper> queuedSyncHelpers;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        queuedSyncHelpers = new ArrayDeque<>();
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
        removeCurrentSync();
    }


    // Show an error by logging, and toasting.
    private void errorMessage(int entityId, String errorMessage) {
        String entityName = getResourceString(getActivity(), entityId);
        String message = "Error syncing " + entityName + ": " + errorMessage;
        Log.e(entityName, message);
        showLongToast(getActivity(), message);
    }

    // Show progress by toasting.
    private void progressMessage(int entityId, String progressMessage) {
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
                recordsText.setText(R.string.sync_database_unknown_value);
            } else {
                recordsText.setText(Integer.toString(records));
            }
        }

        if (IGNORE != errors) {
            if (UNKNOWN == errors) {
                errorsText.setText(R.string.sync_database_unknown_value);
            } else {
                errorsText.setText(Integer.toString(errors));
            }
        }

        if (IGNORE != actionId) {
            actionButton.setText(actionId);
            actionButton.setTag(interpretation);
        }
    }

    // Refresh a table with stored data and ready to sync.
    private void resetTableRow(RelInterpretation<?> interpretation) {
        SyncDatabaseHelper currentSync = getQueuedSync(interpretation);
        int errors = null == currentSync ?  UNKNOWN : currentSync.getErrorCount();
        int records = getRecordCount(interpretation);
        updateTableRow(interpretation, records, errors, R.string.sync_database_button_sync);
    }

    private void removeCurrentSync() {
        if (queuedSyncHelpers.isEmpty()) {
            return;
        }

        SyncDatabaseHelper currentHelper = queuedSyncHelpers.poll();
        currentHelper.cancel();
    }

    private void removeSync(RelInterpretation<?> interpretation) {
        SyncDatabaseHelper victim = null;
        for (SyncDatabaseHelper syncDatabaseHelper : queuedSyncHelpers) {
            if (syncDatabaseHelper.getRelInterpretation().equals(interpretation)) {
                victim = syncDatabaseHelper;
            }
        }

        if (null != victim) {
            queuedSyncHelpers.remove(victim);
        }
    }

    private SyncDatabaseHelper getQueuedSync(RelInterpretation<?> interpretation) {
        for (SyncDatabaseHelper syncDatabaseHelper : queuedSyncHelpers) {
            if (syncDatabaseHelper.getRelInterpretation().equals(interpretation)) {
                return syncDatabaseHelper;
            }
        }
        return null;
    }

    // Add an entity to the queue to be synced.
    private void enqueueSync(RelInterpretation<?> interpretation) {
        if (null != getQueuedSync(interpretation)) {
            return;
        }

        // mark the table row for this entity as "waiting"
        updateTableRow(interpretation, UNKNOWN, UNKNOWN, R.string.sync_database_button_waiting);

        // add this entity to the queue and run it if ready
        queuedSyncHelpers.add(createSyncHelper(interpretation));
        startNextSync();
    }

    private SyncDatabaseHelper createSyncHelper(RelInterpretation<?> interpretation) {
        String username = (String) getActivity().getIntent().getExtras().get(OpeningActivity.USERNAME_KEY);
        String password = (String) getActivity().getIntent().getExtras().get(OpeningActivity.PASSWORD_KEY);
        return new SyncDatabaseHelper(interpretation, new SyncListener(), username, password, getActivity().getContentResolver());
    }

    private int getRecordCount(RelInterpretation<?> interpretation) {
        return  interpretation.getGateway().countAll(getActivity().getContentResolver());
    }

    // Take the next entity off the queue and start the sync process.
    private void startNextSync() {
        if (queuedSyncHelpers.isEmpty()) {
            return;
        }

        // choose the next entity to sync
        SyncDatabaseHelper currentHelper = queuedSyncHelpers.peek();
        RelInterpretation<?> interpretation = currentHelper.getRelInterpretation();

        // reset the table row for this entity
        updateTableRow(interpretation, UNKNOWN, 0, R.string.sync_database_button_cancel);

        // start syncing this entity
        currentHelper.start();
    }

    // Respond to "sync all" button.
    private class SyncAllButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            for (String rel : ResourceLinkRegistry.activeRels()) {
                enqueueSync(ResourceLinkRegistry.getInterpretation(rel));
            }
        }
    }

    // Respond to individual entity "sync" buttons.
    private class ActionButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            // which button is this?
            RelInterpretation<?> interpretation = (RelInterpretation<?>) view.getTag();

            if (!queuedSyncHelpers.isEmpty() && interpretation.equals(queuedSyncHelpers.peek().getRelInterpretation())) {
                // button should change from "cancel" to "sync"
                removeCurrentSync();
                resetTableRow(interpretation);
                progressMessage(interpretation.getLabel(), getResourceString(getActivity(), R.string.sync_database_canceled));

            } else if (null != getQueuedSync(interpretation)) {
                // button should change "waiting" to "sync"
                removeSync(interpretation);
                resetTableRow(interpretation);

            } else {
                // button should change from "sync" to "waiting"
                enqueueSync(interpretation);
            }
        }
    }

    private class SyncListener implements SyncDatabaseHelper.SyncDatabaseListener {
        @Override
        public void onGotLinks(RelInterpretation<?> relInterpretation) {
            progressMessage(relInterpretation.getLabel(), "got links");
        }

        @Override
        public void onParsedLinks(RelInterpretation<?> relInterpretation) {
            progressMessage(relInterpretation.getLabel(), "parsed links");
        }

        @Override
        public void onGotData(RelInterpretation<?> relInterpretation) {
            progressMessage(relInterpretation.getLabel(), "got data");
        }

        @Override
        public void onParseDataProgress(RelInterpretation<?> relInterpretation, int progress) {
            updateTableRow(relInterpretation, progress, IGNORE, IGNORE);
        }

        @Override
        public void onParseDataComplete(RelInterpretation<?> relInterpretation, int progress) {
            resetTableRow(relInterpretation);
            progressMessage(relInterpretation.getLabel(), Integer.toString(getRecordCount(relInterpretation)));
            removeCurrentSync();
        }

        @Override
        public void onError(RelInterpretation<?> relInterpretation, String message, int errorCount) {
            updateTableRow(relInterpretation, IGNORE, errorCount, IGNORE);
            errorMessage(relInterpretation.getLabel(), message);
            removeCurrentSync();
        }
    }
}
