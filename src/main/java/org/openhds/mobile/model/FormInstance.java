package org.openhds.mobile.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.net.Uri;

public class FormInstance {

	private String formName;
	private String filePath;
	private Uri uri;

	public String getFormName() {
		return formName;
	}

	public void setFormName(String formName) {
		this.formName = formName;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public Uri getUri() {
		return uri;
	}

	public void setUri(Uri uri) {
		this.uri = uri;
	}

	public static List<File> toListOfFiles(List<FormInstance> formInstances) {
		ArrayList<File> fileList = new ArrayList<File>();

		for (FormInstance instance : formInstances) {
			fileList.add(new File(instance.getFilePath()));
		}
		return fileList;
	}

	public static List<Uri> toListOfUris(List<FormInstance> formInstances) {
		ArrayList<Uri> uriList = new ArrayList<Uri>();

		for (FormInstance instance : formInstances) {
			uriList.add(instance.getUri());
		}
		return uriList;
	}
}
