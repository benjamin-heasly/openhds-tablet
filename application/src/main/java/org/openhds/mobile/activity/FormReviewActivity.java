package org.openhds.mobile.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
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
import java.util.List;
import java.util.Map;
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

    private Map<FormInstance, Boolean> formSelections;
    private Map<FormInstance, String> formErrors;

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

        showSubmittedCheckBox = (CheckBox) findViewById(R.id.show_submitted_check);
        showSubmittedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                showSubmitted.set(b);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshFormSummary();
        refreshControls();
    }

    // query for fresh data, discard volatile selections etc.
    private void refreshFormSummary() {
        // move to async task?
        List<FormInstance> formInstances = OdkInstanceGateway.findAllInstances(getContentResolver(), formInstanceOrderBy);

        LayoutInflater layoutInflater = getLayoutInflater();
        TableLayout tableLayout = (TableLayout) findViewById(R.id.form_instance_summary_table);
        tableLayout.removeAllViews();
        for (FormInstance formInstance: formInstances) {
            TableRow tableRow = (TableRow) layoutInflater.inflate(R.layout.form_review_row, tableLayout, false);
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

        // show the date in a long for for current locale
        TextView dateText = (TextView) tableRow.findViewById(R.id.form_instance_date);
        long formMilliseconds = Long.parseLong(formInstance.getLastStatusChangeDate());
        Date formDate = new Date(formMilliseconds);
        dateText.setText(DateFormat.getDateTimeInstance().format(formDate));

        // look up localized status label
        TextView statusText = (TextView) tableRow.findViewById(R.id.form_instance_status);
        Integer statusId = formStatusLabels.get(formInstance.getStatus());
        statusText.setText(null == statusId ? R.string.form_instance_status_unknown : statusId);

        // no attempt to remember previous checked state
        CheckBox selectedCheckBox = (CheckBox) tableRow.findViewById(R.id.form_instance_selected);
        selectedCheckBox.setChecked(false);
    }
}
