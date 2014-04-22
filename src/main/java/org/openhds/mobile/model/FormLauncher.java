package org.openhds.mobile.model;

import static org.openhds.mobile.utilities.MessageUtils.showLongToast;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
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

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;

public class FormLauncher {

	Uri contentUri;
	Activity activity;
	Map<String, String> formFieldNames;

	public FormLauncher(String formName, Activity activity, Map<String, String> formFieldNames) {

		this.formFieldNames = formFieldNames;
		this.activity = activity;
		contentUri = makeEditableFormCopy(formName);
		
	}

	public Intent launchForm() {

		Intent intent = new Intent(Intent.ACTION_EDIT, contentUri);
		activity.startActivityForResult(intent, 0);
		return intent;
	}

	private Uri makeEditableFormCopy(String name) {
		// find a blank form with given name
		Cursor cursor = activity.getContentResolver().query(
				FormsProviderAPI.FormsColumns.CONTENT_URI,
				new String[] { FormsProviderAPI.FormsColumns.JR_FORM_ID,
						FormsProviderAPI.FormsColumns.FORM_FILE_PATH },
				FormsProviderAPI.FormsColumns.JR_FORM_ID + " like ?", new String[] { name + "%" }, null);

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

				if (root.getDescendants(filter).hasNext()) {

					Element filledFormRoot = root.getDescendants(filter).next();
					filledFormRoot.detach();
					filledForm.setRootElement(filledFormRoot);

					List<Element> dataChildren;

					dataChildren = filledFormRoot.getChildren();

					for (Element child : dataChildren) {
	
						if (formFieldNames.containsKey(child.getName())
								&& null != formFieldNames.get(child.getName())) {
							
							child.setText(formFieldNames.get(child.getName()));
							
						}else{
							child.setText("");
						}
	
					}
				}

				File editableFormFile = getExternalStorageXmlFile(jrFormId, name, ".xml");
				FileOutputStream fos = new FileOutputStream(editableFormFile);
				XMLOutputter xmlOutput = new XMLOutputter();
				xmlOutput.setFormat(Format.getPrettyFormat());
				xmlOutput.output(filledForm, fos);
				fos.close();

				return shareOdkFormInstance(editableFormFile, editableFormFile.getName(), jrFormId);


			} catch (Exception e) {
				showLongToast(activity, e.getMessage() + " " + e.getCause());
				return null;
			}

		}
		return null;
	}

	private Uri shareOdkFormInstance(File targetFile, String displayName, String formId) {
		ContentValues values = new ContentValues();
		values.put(InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH, targetFile.getAbsolutePath());
		values.put(InstanceProviderAPI.InstanceColumns.DISPLAY_NAME, displayName);
		values.put(InstanceProviderAPI.InstanceColumns.JR_FORM_ID, formId);
		return activity.getContentResolver().insert(InstanceProviderAPI.InstanceColumns.CONTENT_URI, values);

	}

	private File getExternalStorageXmlFile(String subDir, String baseName, String extension) {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd_hh:mm:ss");
		df.setTimeZone(TimeZone.getDefault());
		String date = df.format(new Date());
		String externalFileName = baseName + date + extension;

		File root = Environment.getExternalStorageDirectory();
		String destinationPath = root.getAbsolutePath() + File.separator + "Android" + File.separator
				+ "data" + File.separator + "org.openhds.mobile" + File.separator + "files" + File.separator
				+ subDir;
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
