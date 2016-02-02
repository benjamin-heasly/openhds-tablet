package org.openhds.mobile.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.openhds.mobile.R;
import org.openhds.mobile.forms.FormInstance;
import org.openhds.mobile.forms.odk.InstanceProviderAPI;
import org.openhds.mobile.forms.odk.OdkInstanceGateway;
import org.openhds.mobile.repository.RepositoryUtils;
import org.openhds.mobile.utilities.EncryptionHelper;

import java.io.File;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.openhds.mobile.utilities.MessageUtils.showShortToast;

/**
 * Allow supervisor to review forms.
 * <p/>
 * Shows a table with form instances: type, date, status, and selection.
 * Clicking the date launches the form for editing in ODK.
 * Checking a check box chooses to perform an action on that form instance.
 * <p/>
 * Various buttons allow selecting lots of forms at once, for example by status.
 * A checkbox allows hiding or showing forms that have already been submitted.
 * <p/>
 * A few more buttons allow performing actions on selected forms: send to OpenHDS, mark as
 * incomplete, mark as complete, or skip (aka mark as submitted without submitting).
 * <p/>
 * When a form submission fails, the user can click on the Failed status to view the error message
 * from the server.
 * <p/>
 * Table data come from ODKInstanceGateway.  Data that are not stored in ODK are transient, so they
 * won't survive when the user changes Activities.  These include form selections, error messages
 * from the server, and whether or not to hide submitted forms.
 * <p/>
 * BSH
 */
public class FormReviewActivity extends Activity {

    private CheckBox showSubmittedCheckBox;
    private AtomicBoolean showSubmitted = new AtomicBoolean(false);
    private List<FormInstance> formInstances;

    private Set<String> formSelections = new HashSet<>();
    private Map<String, String> formErrors = new HashMap<>();

    private static final Map<String, Integer> formStatusLabels = new HashMap<>();

    static {
        formStatusLabels.put(InstanceProviderAPI.STATUS_COMPLETE, R.string.form_instance_status_completed);
        formStatusLabels.put(InstanceProviderAPI.STATUS_INCOMPLETE, R.string.form_instance_status_incomplete);
        formStatusLabels.put(InstanceProviderAPI.STATUS_SUBMITTED, R.string.form_instance_status_submitted);
        formStatusLabels.put(InstanceProviderAPI.STATUS_SUBMISSION_FAILED, R.string.form_instance_status_submission_failed);
    }

    private static final String[] formInstanceOrderByColumns = {
            InstanceProviderAPI.InstanceColumns.STATUS,
            InstanceProviderAPI.InstanceColumns.LAST_STATUS_CHANGE_DATE,
            InstanceProviderAPI.InstanceColumns.JR_FORM_ID
    };
    private final static String formInstanceOrderBy = RepositoryUtils.buildOrderByStatement(formInstanceOrderByColumns, true);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.form_review_activity);

        // toggle whether to show submitted instances
        showSubmittedCheckBox = (CheckBox) findViewById(R.id.show_submitted_check);
        showSubmittedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                showSubmitted.set(b);
                refreshFormSummary();
            }
        });

        // select all forms
        Button selectAllButton = (Button) findViewById(R.id.select_all_button);
        selectAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectFormsByStatus(null);
            }
        });

        // select no forms
        Button selectNoneButton = (Button) findViewById(R.id.select_none_button);
        selectNoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                formSelections.clear();
                refreshFormSummary();
            }
        });

        // select all completed forms
        Button selectCompletedButton = (Button) findViewById(R.id.select_complete_button);
        selectCompletedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectFormsByStatus(InstanceProviderAPI.STATUS_COMPLETE);
            }
        });

        // select all incomplete forms
        Button selectIncompleteButton = (Button) findViewById(R.id.select_incomplete_button);
        selectIncompleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectFormsByStatus(InstanceProviderAPI.STATUS_INCOMPLETE);
            }
        });

        // select all submitted forms
        Button selectSubmittedButton = (Button) findViewById(R.id.select_submitted_button);
        selectSubmittedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectFormsByStatus(InstanceProviderAPI.STATUS_SUBMITTED);
            }
        });

        // select all errored forms
        Button selectFailedButton = (Button) findViewById(R.id.select_submission_failed_button);
        selectFailedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectFormsByStatus(InstanceProviderAPI.STATUS_SUBMISSION_FAILED);
            }
        });

        // mark selected forms as incomplete
        Button markAsIncompleteButton = (Button) findViewById(R.id.mark_as_incomplete_button);
        markAsIncompleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateSelectedFormsStatus(InstanceProviderAPI.STATUS_INCOMPLETE);
            }
        });

        // mark selected forms as completed
        Button markAsCompleteButton = (Button) findViewById(R.id.mark_as_complete_button);
        markAsCompleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateSelectedFormsStatus(InstanceProviderAPI.STATUS_COMPLETE);
            }
        });

        // mark selected forms as submitted
        Button markAsSubmittedButton = (Button) findViewById(R.id.mark_as_submitted_button);
        markAsSubmittedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateSelectedFormsStatus(InstanceProviderAPI.STATUS_SUBMITTED);
            }
        });
    }

    // *add* forms with the given status to the current selection
    private void selectFormsByStatus(String status) {
        if (null == formInstances) {
            return;
        }

        for (FormInstance formInstance : formInstances) {
            // don't select forms when they're hidden
            if (!showSubmitted.get() && InstanceProviderAPI.STATUS_SUBMITTED.equals(formInstance.getStatus())) {
                continue;
            }

            if (null == status || status.equals(formInstance.getStatus())) {
                formSelections.add(formInstance.getUri());
            }
        }

        refreshFormSummary();
    }

    // update status for each selected form
    private void updateSelectedFormsStatus(String status) {
        if (null == formSelections || null == status) {
            return;
        }

        ContentResolver contentResolver = getContentResolver();
        for (String uri : formSelections) {
            OdkInstanceGateway.updateInstanceStatus(contentResolver, uri, status);
        }

        // re-query to make sure the update was persisted
        refreshFormList();
        refreshFormSummary();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshFormList();
        refreshFormSummary();
        refreshControls();
    }

    // query for fresh data, discard volatile selections etc.
    private void refreshFormList() {
        // move to async task?
        formInstances = OdkInstanceGateway.findAllInstances(getContentResolver(), formInstanceOrderBy);
    }

    // query for fresh data, discard volatile selections etc.
    private void refreshFormSummary() {
        TableLayout tableLayout = (TableLayout) findViewById(R.id.form_instance_summary_table);
        LayoutInflater layoutInflater = getLayoutInflater();
        tableLayout.removeAllViews();

        if (null == formInstances) {
            return;
        }

        for (FormInstance formInstance : formInstances) {
            TableRow tableRow = (TableRow) layoutInflater.inflate(R.layout.form_review_row, tableLayout, false);
            if (!showSubmitted.get() && InstanceProviderAPI.STATUS_SUBMITTED.equals(formInstance.getStatus())) {
                continue;
            }
            setUpTableRow(tableRow, formInstance);
            tableLayout.addView(tableRow);
        }
    }

    private void refreshControls() {
        showSubmittedCheckBox.setChecked(showSubmitted.get());
    }

    // add a table row and bind it to a formInstance
    private void setUpTableRow(TableRow tableRow, final FormInstance formInstance) {
        tableRow.setTag(formInstance);

        // click on the form title to edit in ODK
        TextView titleText = (TextView) tableRow.findViewById(R.id.form_title);
        titleText.setText(formInstance.getDisplayName());
        final String instanceUri = formInstance.getUri();
        if (null != instanceUri) {
            titleText.setTextColor(R.color.Blue);
            titleText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    File selectedFile = new File(formInstance.getFilePath());
                    EncryptionHelper.decryptFile(selectedFile, getApplicationContext());

                    Intent intent = OdkInstanceGateway.buildEditFormInstanceIntent(instanceUri);
                    showShortToast(FormReviewActivity.this, R.string.launching_odk_collect);
                    startActivityForResult(intent, 0);
                }
            });
        }

        // show the date in a long for current locale
        TextView dateText = (TextView) tableRow.findViewById(R.id.form_instance_date);
        dateText.setText(milliStringToDateString(formInstance.getLastStatusChangeDate()));

        // look up localized status label
        TextView statusText = (TextView) tableRow.findViewById(R.id.form_instance_status);
        Integer statusId = formStatusLabels.get(formInstance.getStatus());
        statusText.setText(null == statusId ? R.string.form_instance_status_unknown : statusId);

        // disclose error message from server, if any
        statusText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                raiseErrorDialog(formInstance);
            }
        });

        // show the current checked state
        CheckBox selectedCheckBox = (CheckBox) tableRow.findViewById(R.id.form_instance_selected);
        selectedCheckBox.setChecked(formSelections.contains(formInstance.getUri()));

        // allow toggling of the checked state
        selectedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    formSelections.add(formInstance.getUri());
                } else {
                    formSelections.remove(formInstance.getUri());
                }
            }
        });
    }

    private String milliStringToDateString(String millis) {
        try {
            long milliseconds = Long.parseLong(millis);
            Date formDate = new Date(milliseconds);
            return DateFormat.getDateTimeInstance().format(formDate);
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    private void raiseErrorDialog(FormInstance formInstance) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        final String title = formInstance.getDisplayName()
                + "\n"
                + milliStringToDateString(formInstance.getLastStatusChangeDate());
        builder.setTitle(title);

        builder.setCancelable(true);

        String errorMessage = formErrors.get(formInstance.getUri());
        builder.setMessage(null == errorMessage ? "" : errorMessage);

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void uploadSelectedForms() {

    }
}
