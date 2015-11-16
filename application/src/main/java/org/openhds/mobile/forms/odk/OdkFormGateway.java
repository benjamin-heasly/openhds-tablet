package org.openhds.mobile.forms.odk;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import org.openhds.mobile.forms.FormDefinition;
import org.openhds.mobile.repository.RepositoryUtils;
import org.openhds.mobile.utilities.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.openhds.mobile.forms.odk.FormsProviderApi.FormsColumns.CONTENT_URI;
import static org.openhds.mobile.forms.odk.FormsProviderApi.FormsColumns.DATE;
import static org.openhds.mobile.forms.odk.FormsProviderApi.FormsColumns.DESCRIPTION;
import static org.openhds.mobile.forms.odk.FormsProviderApi.FormsColumns.DISPLAY_NAME;
import static org.openhds.mobile.forms.odk.FormsProviderApi.FormsColumns.DISPLAY_SUBTEXT;
import static org.openhds.mobile.forms.odk.FormsProviderApi.FormsColumns.FORM_FILE_PATH;
import static org.openhds.mobile.forms.odk.FormsProviderApi.FormsColumns.JR_FORM_ID;
import static org.openhds.mobile.forms.odk.FormsProviderApi.FormsColumns.JR_VERSION;
import static org.openhds.mobile.forms.odk.FormsProviderApi.FormsColumns.SUBMISSION_URI;
import static org.openhds.mobile.repository.RepositoryUtils.extractString;


/**
 * Created by ben on 10/26/15.
 * <p/>
 * Register and access form definitions with installed ODK Collect app.
 */
public class OdkFormGateway {

    public static final String BUNDLED_FORMS_DIR = "bundledForms";

    public static List<FormDefinition> expandBundledForms(Context context) {
        List<FormDefinition> forms = new ArrayList<>();

        try {
            String[] assetNames = context.getAssets().list(BUNDLED_FORMS_DIR);
            for (String assetName : assetNames) {
                // expand the bundled asset into a file
                String assetPath = BUNDLED_FORMS_DIR + File.separator +  assetName;
                String externalRelativePath = FileUtils.relativeFilePath(BUNDLED_FORMS_DIR, assetName, false);
                File assetFile = FileUtils.writeAssetExternalFile(context, assetPath, externalRelativePath);

                // wrap the new file in a form definition
                String[] parts = assetName.split("\\.");
                String basename = parts[0];
                FormDefinition formDefinition = new FormDefinition();
                formDefinition.setFilePath(assetFile.getAbsolutePath());
                formDefinition.setDisplayName(basename);
                formDefinition.setId(basename);
                forms.add(formDefinition);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return forms;
    }

    public static List<FormDefinition> registerForms(ContentResolver contentResolver, List<FormDefinition> formDefinitions) {
        List<FormDefinition> registeredForms = new ArrayList<>();
        for (FormDefinition formDefinition : formDefinitions) {
            // the new form to register
            ContentValues contentValues = toContentValues(formDefinition);

            // insert new or update existing?
            FormDefinition existing = findRegisteredFormById(contentResolver, formDefinition.getId());
            if (null == existing) {
                Uri uri = contentResolver.insert(CONTENT_URI, contentValues);
                if (null != uri) {
                    registeredForms.add(formDefinition);
                }
            } else {
                final String[] columnNames = {JR_FORM_ID};
                final String[] operators = {RepositoryUtils.EQUALS};
                final String[] columnValues = {existing.getId()};
                final String whereStatement = RepositoryUtils.buildWhereStatement(columnNames, operators);
                int rowCount = contentResolver.update(CONTENT_URI, contentValues, whereStatement, columnValues);
                if (rowCount > 0) {
                    registeredForms.add(formDefinition);
                }
            }
        }
        return registeredForms;
    }

    public static FormDefinition findRegisteredFormById(ContentResolver contentResolver, String formId) {
        final String[] columnNames = {JR_FORM_ID};
        final String[] operators = {RepositoryUtils.EQUALS};
        final String[] columnValues = {formId};
        final String whereStatement = RepositoryUtils.buildWhereStatement(columnNames, operators);
        Cursor cursor = RepositoryUtils.query(contentResolver, CONTENT_URI, whereStatement, columnValues, JR_FORM_ID);

        FormDefinition formDefinition = null;
        if (cursor.moveToNext()) {
            formDefinition = fromCursor(cursor);
        }
        cursor.close();
        return formDefinition;
    }

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

        putIfNotNull(contentValues, DISPLAY_NAME, formDefinition.getDisplayName());
        putIfNotNull(contentValues, DISPLAY_SUBTEXT, formDefinition.getDisplaySubtext());
        putIfNotNull(contentValues, DESCRIPTION, formDefinition.getDescription());
        putIfNotNull(contentValues, JR_FORM_ID, formDefinition.getId());
        putIfNotNull(contentValues, JR_VERSION, formDefinition.getVersion());
        putIfNotNull(contentValues, DATE, formDefinition.getDate());
        putIfNotNull(contentValues, FORM_FILE_PATH, formDefinition.getFilePath());
        putIfNotNull(contentValues, SUBMISSION_URI, formDefinition.getSubmissionUri());

        return contentValues;
    }

    private static void putIfNotNull(ContentValues contentValues, String key, String value) {
        if (null == key || null == value) {
            return;
        }
        contentValues.put(key, value);
    }

}
