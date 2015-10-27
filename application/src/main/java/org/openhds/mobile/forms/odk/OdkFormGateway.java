package org.openhds.mobile.forms.odk;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import org.openhds.mobile.forms.FormDefinition;
import org.openhds.mobile.repository.RepositoryUtils;

import java.util.ArrayList;
import java.util.List;

import static org.openhds.mobile.forms.odk.FormsProviderAPI.FormsColumns.CONTENT_URI;
import static org.openhds.mobile.forms.odk.FormsProviderAPI.FormsColumns.DATE;
import static org.openhds.mobile.forms.odk.FormsProviderAPI.FormsColumns.DESCRIPTION;
import static org.openhds.mobile.forms.odk.FormsProviderAPI.FormsColumns.DISPLAY_NAME;
import static org.openhds.mobile.forms.odk.FormsProviderAPI.FormsColumns.DISPLAY_SUBTEXT;
import static org.openhds.mobile.forms.odk.FormsProviderAPI.FormsColumns.FORM_FILE_PATH;
import static org.openhds.mobile.forms.odk.FormsProviderAPI.FormsColumns.JR_FORM_ID;
import static org.openhds.mobile.forms.odk.FormsProviderAPI.FormsColumns.JR_VERSION;
import static org.openhds.mobile.forms.odk.FormsProviderAPI.FormsColumns.SUBMISSION_URI;
import static org.openhds.mobile.repository.RepositoryUtils.extractString;


/**
 * Created by ben on 10/26/15.
 * <p/>
 * Register and access form definitions with installed ODK Collect app.
 */
public class OdkFormGateway {

    public static List<FormDefinition> findRegisteredForms(ContentResolver contentResolver) {
        final String whereStatement = RepositoryUtils.buildWhereStatement(null, null);
        Cursor cursor = RepositoryUtils.query(contentResolver, CONTENT_URI, whereStatement, null, JR_FORM_ID);

        List<FormDefinition> formDefinitions = new ArrayList<>();
        while (cursor.moveToNext()) {
            formDefinitions.add(fromCursor(cursor));
        }
        cursor.close();
        return formDefinitions;
    }

    public static Uri registerForm(ContentResolver contentResolver, FormDefinition formDefinition) {
        ContentValues contentValues = toContentValues(formDefinition);
        return contentResolver.insert(CONTENT_URI, contentValues);
    }

    public static FormDefinition fromCursor(Cursor cursor) {
        FormDefinition formDefinition = new FormDefinition();

        formDefinition.setDisplayName(extractString(cursor, DISPLAY_NAME));
        formDefinition.setDisplaySubtext(extractString(cursor, DISPLAY_SUBTEXT));
        formDefinition.setDescription(extractString(cursor, DESCRIPTION));
        formDefinition.setId(extractString(cursor, JR_FORM_ID));
        formDefinition.setVersion(extractString(cursor, JR_VERSION));
        formDefinition.setDate(extractString(cursor, DATE));
        formDefinition.setFilePath(extractString(cursor, FORM_FILE_PATH));
        formDefinition.setSubmissionUri(extractString(cursor, SUBMISSION_URI));

        return formDefinition;
    }

    public static ContentValues toContentValues(FormDefinition formDefinition) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(DISPLAY_NAME, formDefinition.getDisplayName());
        contentValues.put(DISPLAY_SUBTEXT, formDefinition.getDisplaySubtext());
        contentValues.put(DESCRIPTION, formDefinition.getDescription());
        contentValues.put(JR_FORM_ID, formDefinition.getId());
        contentValues.put(JR_VERSION, formDefinition.getVersion());
        contentValues.put(DATE, formDefinition.getDate());
        contentValues.put(FORM_FILE_PATH, formDefinition.getFilePath());
        contentValues.put(SUBMISSION_URI, formDefinition.getSubmissionUri());

        return contentValues;
    }

}
