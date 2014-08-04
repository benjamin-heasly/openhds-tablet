package org.openhds.mobile.utilities;

import static org.openhds.mobile.InstanceProviderAPI.InstanceColumns.CONTENT_URI;

import java.util.ArrayList;
import java.util.List;

import org.openhds.mobile.InstanceProviderAPI;
import org.openhds.mobile.model.FormInstance;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

public class OdkCollectHelper {

	public static List<FormInstance> getAllFormInstances(
			ContentResolver resolver) {

		ArrayList<FormInstance> formInstances = new ArrayList<FormInstance>();

		Cursor cursor = resolver.query(CONTENT_URI, new String[] {
				InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH,
				InstanceProviderAPI.InstanceColumns._ID,
                InstanceProviderAPI.InstanceColumns.JR_FORM_ID,
                InstanceProviderAPI.InstanceColumns.DISPLAY_NAME}, null, null, null);

		while (cursor.moveToNext()) {

			FormInstance formInstance = new FormInstance();
			String filePath;
            String formName;
            String fileName;

			Uri uri;

			filePath = cursor
					.getString(cursor
							.getColumnIndex(InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH));

			uri = Uri.withAppendedPath(CONTENT_URI, cursor.getString(cursor
					.getColumnIndex(InstanceProviderAPI.InstanceColumns._ID)));

            formName = cursor
                    .getString(cursor
                            .getColumnIndex(InstanceProviderAPI.InstanceColumns.JR_FORM_ID));

            fileName = cursor.getString(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.DISPLAY_NAME));

			formInstance.setFilePath(filePath);
			formInstance.setUri(uri);
            formInstance.setFormName(formName);
            formInstance.setFileName(fileName);

			formInstances.add(formInstance);

		}
		cursor.close();
		return formInstances;
	}
}
