package org.openhds.mobile.forms;

import android.net.Uri;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FormInstance implements Serializable {

    private static final long serialVersionUID = 1L;

    private FormBehaviour formBehaviour;
    private String filePath;
    private String uriString;

    public FormBehaviour getFormBehaviour() {
        return formBehaviour;
    }

    public void setFormBehaviour(FormBehaviour formBehaviour) {
        this.formBehaviour = formBehaviour;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getUriString() {
        return uriString;
    }

    public void setUriString(String uriString) {
        this.uriString = uriString;
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
            uriList.add(Uri.parse(instance.getUriString()));
        }
        return uriList;
    }
}
