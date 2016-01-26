package org.openhds.mobile.forms.odk;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.ElementFilter;
import org.jdom2.input.SAXBuilder;
import org.openhds.mobile.forms.FormContent;
import org.openhds.mobile.forms.FormDefinition;
import org.openhds.mobile.forms.FormInstance;
import org.openhds.mobile.repository.RepositoryUtils;
import org.openhds.mobile.utilities.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static android.provider.BaseColumns._ID;
import static org.openhds.mobile.forms.odk.InstanceProviderAPI.InstanceColumns.CAN_EDIT_WHEN_COMPLETE;
import static org.openhds.mobile.forms.odk.InstanceProviderAPI.InstanceColumns.CONTENT_URI;
import static org.openhds.mobile.forms.odk.InstanceProviderAPI.InstanceColumns.DISPLAY_NAME;
import static org.openhds.mobile.forms.odk.InstanceProviderAPI.InstanceColumns.DISPLAY_SUBTEXT;
import static org.openhds.mobile.forms.odk.InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH;
import static org.openhds.mobile.forms.odk.InstanceProviderAPI.InstanceColumns.JR_FORM_ID;
import static org.openhds.mobile.forms.odk.InstanceProviderAPI.InstanceColumns.JR_VERSION;
import static org.openhds.mobile.forms.odk.InstanceProviderAPI.InstanceColumns.LAST_STATUS_CHANGE_DATE;
import static org.openhds.mobile.forms.odk.InstanceProviderAPI.InstanceColumns.STATUS;
import static org.openhds.mobile.repository.RepositoryUtils.extractString;


/**
 * Created by ben on 30 November 2015.
 * <p/>
 * Register and access form instances with installed ODK Collect app.
 */
public class OdkInstanceGateway {

    public static final String DATA_ELEMENT_NAME = "data";
    public static final String REVIEW_LEVEL_NEEDS_REVIEW = "1";
    public static final String REVIEW_LEVEL_REVIEW_OK = "0";
    public static final String REVIEW_LEVEL_FIELD_NAME = "needsReview";

    public static List<FormInstance> findAllInstances(ContentResolver contentResolver) {
        return findAllInstances(contentResolver, JR_FORM_ID);
    }

    public static List<FormInstance> findAllInstances(ContentResolver contentResolver, String orderBy) {
        final String whereStatement = RepositoryUtils.buildWhereStatement(null, null);
        Cursor cursor = RepositoryUtils.query(contentResolver, CONTENT_URI, whereStatement, null, orderBy);

        List<FormInstance> formInstances = new ArrayList<>();
        if (null == cursor) {
            return formInstances;
        }
        while (cursor.moveToNext()) {
            formInstances.add(fromCursor(cursor));
        }
        cursor.close();
        return formInstances;
    }

    public static List<FormInstance> findInstancesByStatus(ContentResolver contentResolver, String status) {
        final String[] columnNames = {STATUS};
        final String[] operators = {RepositoryUtils.EQUALS};
        final String[] columnValues = {status};
        final String whereStatement = RepositoryUtils.buildWhereStatement(columnNames, operators);
        Cursor cursor = RepositoryUtils.query(contentResolver, CONTENT_URI, whereStatement, columnValues, JR_FORM_ID);

        List<FormInstance> formInstances = new ArrayList<>();
        if (null == cursor) {
            return formInstances;
        }
        while (cursor.moveToNext()) {
            formInstances.add(fromCursor(cursor));
        }
        cursor.close();
        return formInstances;
    }

    public static FormInstance findByUri(ContentResolver contentResolver, String instanceUri) {
        Cursor cursor = contentResolver.query(Uri.parse(instanceUri), null, null, null, null);
        FormInstance formInstance = null;
        if (null == cursor) {
            return formInstance;
        }
        if (cursor.moveToNext()) {
            formInstance = fromCursor(cursor);
        }
        cursor.close();
        formInstance.setUri(instanceUri);
        return formInstance;
    }

    public static FormInstance findByFilePath(ContentResolver contentResolver, String instanceFilePath) {
        final String[] columnNames = {INSTANCE_FILE_PATH};
        final String[] operators = {RepositoryUtils.EQUALS};
        final String[] columnValues = {instanceFilePath};
        final String whereStatement = RepositoryUtils.buildWhereStatement(columnNames, operators);
        Cursor cursor = RepositoryUtils.query(contentResolver, CONTENT_URI, whereStatement, columnValues, JR_FORM_ID);

        FormInstance formInstance = null;
        if (null == cursor) {
            return formInstance;
        }
        if (cursor.moveToNext()) {
            formInstance = fromCursor(cursor);
        }
        cursor.close();
        return formInstance;
    }

    public static boolean updateInstanceStatus(ContentResolver resolver, String instanceUri, String status) {
        ContentValues cv = new ContentValues();
        cv.put(STATUS, status);
        return null != instanceUri && 0 < resolver.update(Uri.parse(instanceUri), cv, null, null);
    }

    public static boolean instanceNeedsReview(ContentResolver resolver,  FormInstance formInstance) {
        String reviewLevel = getInstanceReviewLevel(resolver, formInstance);
        return null != reviewLevel && !REVIEW_LEVEL_REVIEW_OK.equals(reviewLevel);
    }

    public static String getInstanceReviewLevel(ContentResolver contentResolver, FormInstance formInstance) {
        if (null == formInstance) {
            return null;
        }

        FormContent formContent = FormContent.readFormContent(new File(formInstance.getFilePath()));
        if (null == formContent) {
            return null;
        }

        return formContent.getContentString(FormContent.TOP_LEVEL_ALIAS, REVIEW_LEVEL_FIELD_NAME);
    }

    public static boolean setInstanceNeedsReview(ContentResolver contentResolver, FormInstance formInstance) {
        File formFile = new File(formInstance.getFilePath());
        FormContent formContent = FormContent.readFormContent(formFile);
        if (null == formContent) {
            return false;
        }

        formContent.setContent(FormContent.TOP_LEVEL_ALIAS, REVIEW_LEVEL_FIELD_NAME, REVIEW_LEVEL_NEEDS_REVIEW);
        boolean updatedContent = formContent.updateFormContent(formFile);
        boolean updatedStatus = updateInstanceStatus(contentResolver, formInstance.getUri(), InstanceProviderAPI.STATUS_INCOMPLETE);
        return updatedContent && updatedStatus;
    }

    public static boolean setInstanceReviewOk(ContentResolver contentResolver, FormInstance formInstance) {
        File formFile = new File(formInstance.getFilePath());
        FormContent formContent = FormContent.readFormContent(formFile);
        if (null == formContent) {
            return false;
        }

        formContent.setContent(FormContent.TOP_LEVEL_ALIAS, REVIEW_LEVEL_FIELD_NAME, REVIEW_LEVEL_REVIEW_OK);
        boolean updatedContent = formContent.updateFormContent(formFile);
        boolean updatedStatus = updateInstanceStatus(contentResolver, formInstance.getUri(), InstanceProviderAPI.STATUS_COMPLETE);
        return updatedContent && updatedStatus;
    }


    public static FormInstance registerOrUpdateInstance(ContentResolver contentResolver, FormInstance formInstance) {
        // the new instance to register
        ContentValues contentValues = toContentValues(formInstance);

        // insert new or update existing?
        String existingUri = formInstance.getUri();
        FormInstance existing = null == existingUri ? null : findByUri(contentResolver, formInstance.getUri());
        if (null == existing) {
            existingUri = String.valueOf(contentResolver.insert(CONTENT_URI, contentValues));
        } else {
            int rowCount = contentResolver.update(Uri.parse(existingUri), contentValues, null, null);
            if (0 == rowCount) {
                return null;
            }
        }

        return findByUri(contentResolver, existingUri);
    }

    public static FormInstance instantiateFormDefinition(FormDefinition formDefinition) {
        FormInstance formInstance = new FormInstance();
        formInstance.setDisplayName(formDefinition.getDisplayName());
        formInstance.setDisplaySubtext(formDefinition.getDisplaySubtext());
        formInstance.setFormId(formDefinition.getId());
        formInstance.setVersion(formDefinition.getVersion());

        String filePath = FileUtils.openHdsExternalFilesPath()
                + File.separator
                + FileUtils.relativeFilePath(formDefinition.getId(), formDefinition.getId()
                        + ".xml",
                true);
        formInstance.setFilePath(filePath);

        Element dataElement = getDataElement(formDefinition);
        if (null == dataElement) {
            return null;
        }

        FormContent defaultContent = FormContent.readFormContent(dataElement);
        if (null == defaultContent) {
            return null;
        }

        defaultContent.initializeFormContent(new File(formInstance.getFilePath()), dataElement);

        return formInstance;
    }

    private static Element getDataElement(FormDefinition formDefinition) {
        SAXBuilder builder = new SAXBuilder();

        try {
            Document definitionDoc = builder.build(new File(formDefinition.getFilePath()));
            ElementFilter filter = new ElementFilter(DATA_ELEMENT_NAME);
            Iterator<Element> elementIterator = definitionDoc.getDescendants(filter);
            return elementIterator.hasNext() ? elementIterator.next() : null;

        } catch (JDOMException e) {
            e.printStackTrace();
            return null;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Intent buildEditFormInstanceIntent(String instanceUri) {
        Intent intent = new Intent(Intent.ACTION_EDIT);
        intent.setDataAndType(Uri.parse(instanceUri), InstanceProviderAPI.InstanceColumns.CONTENT_ITEM_TYPE);
        return intent;
    }

    private static FormInstance fromCursor(Cursor cursor) {
        FormInstance formInstance = new FormInstance();

        String id = extractString(cursor, _ID);
        formInstance.setUri(instanceUriForId(id));

        formInstance.setDisplayName(extractString(cursor, DISPLAY_NAME));
        formInstance.setDisplaySubtext(extractString(cursor, DISPLAY_SUBTEXT));
        formInstance.setFilePath(extractString(cursor, INSTANCE_FILE_PATH));
        formInstance.setFormId(extractString(cursor, JR_FORM_ID));
        formInstance.setVersion(extractString(cursor, JR_VERSION));
        formInstance.setStatus(extractString(cursor, STATUS));
        formInstance.setCanEditWhenComplete(extractString(cursor, CAN_EDIT_WHEN_COMPLETE));
        formInstance.setLastStatusChangeDate(extractString(cursor, LAST_STATUS_CHANGE_DATE));

        return formInstance;
    }

    private static ContentValues toContentValues(FormInstance formInstance) {
        ContentValues contentValues = new ContentValues();

        putIfNotNull(contentValues, DISPLAY_NAME, formInstance.getDisplayName());
        putIfNotNull(contentValues, DISPLAY_SUBTEXT, formInstance.getDisplaySubtext());
        putIfNotNull(contentValues, INSTANCE_FILE_PATH, formInstance.getFilePath());
        putIfNotNull(contentValues, JR_FORM_ID, formInstance.getFormId());
        putIfNotNull(contentValues, JR_VERSION, formInstance.getVersion());
        putIfNotNull(contentValues, STATUS, formInstance.getStatus());
        putIfNotNull(contentValues, CAN_EDIT_WHEN_COMPLETE, formInstance.getCanEditWhenComplete());
        putIfNotNull(contentValues, LAST_STATUS_CHANGE_DATE, formInstance.getLastStatusChangeDate());

        return contentValues;
    }

    // ie content://org.odk.collect.android.provider.odk.instances/instances/46
    private static String instanceUriForId(String id) {
        return "content://" + InstanceProviderAPI.AUTHORITY + "/instances/" + id;
    }

    private static void putIfNotNull(ContentValues contentValues, String key, String value) {
        if (null == key || null == value) {
            return;
        }
        contentValues.put(key, value);
    }

}
