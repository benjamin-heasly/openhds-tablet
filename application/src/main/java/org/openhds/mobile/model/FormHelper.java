package org.openhds.mobile.model;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.ElementFilter;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.openhds.mobile.FormsProviderAPI;
import org.openhds.mobile.InstanceProviderAPI;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import org.openhds.mobile.projectdata.ProjectFormFields;
import org.openhds.mobile.projectdata.ProjectResources;
import org.openhds.mobile.utilities.EncryptionHelper;
import org.openhds.mobile.utilities.OdkCollectHelper;

public class FormHelper {

    Uri contentUri;
    ContentResolver resolver;
    Map<String, String> formFieldNames;
    FormBehaviour form;

    String finalizedFormFilePath;

    public FormHelper(ContentResolver resolver) {
        this.resolver = resolver;

    }

    public String getFinalizedFormFilePath() {
        return finalizedFormFilePath;
    }

    public FormBehaviour getForm() {
        return form;
    }

    public Intent buildEditFormInstanceIntent() {
        Intent intent = new Intent(Intent.ACTION_EDIT, contentUri);
        return intent;
    }

    // Pull out to ODKCollectHelper
    public boolean checkFormInstanceStatus() {
        Cursor cursor = resolver.query(contentUri, new String[] {
                        InstanceProviderAPI.InstanceColumns.STATUS,
                        InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH },
                InstanceProviderAPI.InstanceColumns.STATUS + "=?",
                new String[] { InstanceProviderAPI.STATUS_COMPLETE }, null);
        if (cursor.moveToNext()) {
            finalizedFormFilePath = cursor
                    .getString(cursor
                            .getColumnIndex(InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH));

            cursor.close();
            return true;
        } else {
            cursor.close();
            return false;
        }
    }

    public static String getFormTagValue(String tag, String formFilePath) {
        Map<String, String> formData = FormHelper.getFormInstanceData(formFilePath);
        if (null == formData) {
            return null;
        } else {
            return formData.get(tag);
        }
    }

    public static boolean isFormReviewed(String formFilePath) {

        String needsReview = FormHelper.getFormTagValue(ProjectFormFields.General.NEEDS_REVIEW, formFilePath);
        if (needsReview == null) {
            return false;
        }
        boolean value = needsReview.equalsIgnoreCase(ProjectResources.General.FORM_NO_REVIEW_NEEDED);
        return value;

    }

    public static boolean setFormTagValue(String tag, String value, String formFilePath) {

        Map<String, String> formFieldMap = FormHelper.getFormInstanceData(formFilePath);
        formFieldMap.put(tag, value);
        return FormHelper.updateExistingFormInstance(formFieldMap, formFilePath);
    }

    private static boolean updateExistingFormInstance(Map<String, String> formFieldMap, String formFilePath) {

        try {
            SAXBuilder builder = new SAXBuilder();

            Document oldForm = builder.build(new File(formFilePath));
            Document newForm = new Document();

            Element root = oldForm.getRootElement();
            root.detach();
            root.removeContent();
            newForm.setRootElement(root);

            for (String elementName : formFieldMap.keySet()) {

                Element child = new Element(elementName);
                child.setText(formFieldMap.get(elementName));
                newForm.getRootElement().addContent(child);
            }

            FileOutputStream fos = new FileOutputStream(formFilePath);
            XMLOutputter xmlOutput = new XMLOutputter();
            xmlOutput.setFormat(Format.getPrettyFormat());
            xmlOutput.output(newForm, fos);
            fos.close();

            return true;

        } catch (Exception e) {
            return false;
        }
    }

    //    TODO: Convert FormHelper to a static class
//    Static implementation of getFormInstanceData()
    public static Map<String, String> getFormInstanceData(String formFilePath) {

        Map<String, String> formFields = new HashMap<>();
        if (null == formFilePath) {
            return null;
        }
        SAXBuilder builder = new SAXBuilder();
        try {
            Document finalizedDoc = builder.build(new File(
                    formFilePath));
            Element root = finalizedDoc.getRootElement();
            Iterator<Element> itr = root.getDescendants(new ElementFilter());

            while (itr.hasNext()) {
                Element child = itr.next();
                formFields.put(child.getName(), child.getText());
            }

        } catch (Exception e) {
            return null;
        }

        formFields = formFields;
        return formFields;

    }
    public Map<String, String> getFormInstanceData() {

        formFieldNames.clear();

        if (null == finalizedFormFilePath) {
            return null;
        }

        SAXBuilder builder = new SAXBuilder();
        try {

            Document finalizedDoc = builder.build(new File(
                    finalizedFormFilePath));
            Element root = finalizedDoc.getRootElement();
            Iterator<Element> itr = root.getDescendants(new ElementFilter());

            while (itr.hasNext()) {
                Element child = itr.next();
                formFieldNames.put(child.getName(), child.getText());
            }

        } catch (Exception e) {
            return null;
        }

        return formFieldNames;
    }

    // this method assumes/relies on finalizedFormFilePath is NOT null.
    public boolean updateExistingFormInstance() {

        try {
            SAXBuilder builder = new SAXBuilder();

            Document oldForm = builder.build(new File(finalizedFormFilePath));
            Document newForm = new Document();

            Element root = oldForm.getRootElement();
            root.detach();
            root.removeContent();
            newForm.setRootElement(root);

            for (String elementName : formFieldNames.keySet()) {

                Element child = new Element(elementName);
                child.setText(formFieldNames.get(elementName));
                newForm.getRootElement().addContent(child);
            }

            FileOutputStream fos = new FileOutputStream(finalizedFormFilePath);
            XMLOutputter xmlOutput = new XMLOutputter();
            xmlOutput.setFormat(Format.getPrettyFormat());
            xmlOutput.output(newForm, fos);
            fos.close();

            return true;

        } catch (Exception e) {
            return false;
        }
    }

    public boolean newFormInstance(FormBehaviour form,
                                   Map<String, String> formFieldNames) {
        // find a blank form with given name

        this.form = form;
        this.formFieldNames = formFieldNames;
        finalizedFormFilePath = null;

        Cursor cursor = resolver.query(
                FormsProviderAPI.FormsColumns.CONTENT_URI, new String[] {
                        FormsProviderAPI.FormsColumns.JR_FORM_ID,
                        FormsProviderAPI.FormsColumns.FORM_FILE_PATH },
                FormsProviderAPI.FormsColumns.JR_FORM_ID + " like ?",
                new String[] { form.getFormName() + "%" }, null);

        if (cursor.moveToFirst()) {
            String jrFormId = cursor.getString(0);
            String formFilePath = cursor.getString(1);

            try {
                SAXBuilder builder = new SAXBuilder();

                // get reference to unfilled form

                Document blankDoc = builder.build(new File(formFilePath));

                Element root = blankDoc.getRootElement();

                ElementFilter filter = new ElementFilter("data");

                Document filledForm = new Document();

                Iterator<Element> itr = root.getDescendants(filter);

                if (itr.hasNext()) {

                    Element filledFormRoot = itr.next();
                    filledFormRoot.detach();
                    filledForm.setRootElement(filledFormRoot);
                    Iterator<Element> dataDecendantsItr = filledFormRoot.getDescendants(new ElementFilter());

                    Map<Element, String> toModify = new HashMap<>();

                    while (dataDecendantsItr.hasNext()) {

                        Element child = dataDecendantsItr.next();
                        String name = child.getName();

                        if (formFieldNames.containsKey(name) && null != formFieldNames.get(name)) {
                            toModify.put(child, formFieldNames.get(name));
                        } else {
                            toModify.put(child, "");
                        }
                    }

                    for (Element child : toModify.keySet()) {
                        child.setText(toModify.get(child));
                    }
                }

                File editableFormFile = getExternalStorageXmlFile(jrFormId,
                        form.getFormName(), ".xml");

                FileOutputStream fos = new FileOutputStream(editableFormFile);
                XMLOutputter xmlOutput = new XMLOutputter();
                xmlOutput.setFormat(Format.getPrettyFormat());
                xmlOutput.output(filledForm, fos);
                fos.close();

                contentUri = shareOdkFormInstance(editableFormFile,
                        editableFormFile.getName(), jrFormId);

                return true;

            } catch (Exception e) {
                return false;
            }

        }
        return false;
    }

    // ODKCollectHelper
    private Uri shareOdkFormInstance(File targetFile, String displayName,
                                     String formId) {
        ContentValues values = new ContentValues();
        values.put(InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH,
                targetFile.getAbsolutePath());
        values.put(InstanceProviderAPI.InstanceColumns.DISPLAY_NAME,
                displayName);
        values.put(InstanceProviderAPI.InstanceColumns.JR_FORM_ID, formId);
        return resolver.insert(InstanceProviderAPI.InstanceColumns.CONTENT_URI,
                values);
    }

    private File getExternalStorageXmlFile(String subDir, String baseName,
                                           String extension) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd_hh:mm:ss",
                Locale.getDefault());
        df.setTimeZone(TimeZone.getDefault());
        String date = df.format(new Date());
        String externalFileName = baseName + date + extension;

        File root = Environment.getExternalStorageDirectory();
        String destinationPath = root.getAbsolutePath() + File.separator
                + "Android" + File.separator + "data" + File.separator
                + "org.openhds.mobile" + File.separator + "files"
                + File.separator + subDir;
        File parentDir = new File(destinationPath);
        if (!parentDir.exists()) {
            boolean created = parentDir.mkdirs();
            if (!created) {
                return null;
            }
        }

        destinationPath += File.separator + externalFileName;
        File targetFile = new File(destinationPath);
        return targetFile;
    }

}
