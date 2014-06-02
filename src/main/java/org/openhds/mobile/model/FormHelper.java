package org.openhds.mobile.model;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

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

public class FormHelper {

	Uri contentUri;
	ContentResolver resolver;
	Map<String, String> formFieldNames;
	FormRecord form;

	String finalizedFormFilePath;

	public FormHelper(ContentResolver resolver) {
		this.resolver = resolver;

	}

	public String getFinalizedFormFilePath() {
		return finalizedFormFilePath;
	}

	public FormRecord getForm() {
		return form;
	}

	public Intent buildEditFormInstanceIntent() {
		Intent intent = new Intent(Intent.ACTION_EDIT, contentUri);
		return intent;
	}

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

	public Map<String, String> getFormInstanceData() {

		if (null == finalizedFormFilePath) {
			return null;
		}
		Map<String, String> finalizedFormData = new HashMap<String, String>();
		SAXBuilder builder = new SAXBuilder();
		try {

			Document finalizedDoc = builder.build(new File(
					finalizedFormFilePath));
			Element root = finalizedDoc.getRootElement();
			Iterator<Element> itr = root.getDescendants(new ElementFilter());

			while (itr.hasNext()) {
				Element child = itr.next();
				finalizedFormData.put(child.getName(), child.getText());
			}

		} catch (Exception e) {
			return null;
		}

		return finalizedFormData;
	}

	public boolean newFormInstance(FormRecord form,
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

			SAXBuilder builder = new SAXBuilder();

			try {

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
					Iterator<Element> dataDecendantsItr = filledFormRoot
							.getDescendants(new ElementFilter());

					while (dataDecendantsItr.hasNext()) {

						Element child = dataDecendantsItr.next();

						if (formFieldNames.containsKey(child.getName())
								&& null != formFieldNames.get(child.getName())) {

							child.setText(formFieldNames.get(child.getName()));

						} else {
							child.setText("");
						}
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

			} catch (Exception e) {
				return false;
			}

		}
		return false;
	}

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
