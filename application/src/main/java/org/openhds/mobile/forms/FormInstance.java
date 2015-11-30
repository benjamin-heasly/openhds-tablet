package org.openhds.mobile.forms;

import android.net.Uri;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FormInstance implements Serializable {

    private static final long serialVersionUID = 1L;

    private String displayName;
    private String displaySubtext;
    private String filePath;
    private String uri;
    private String formId;
    private String version;
    private String status;
    private String canEditWhenComplete;
    private String lastStatusChangeDate;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplaySubtext() {
        return displaySubtext;
    }

    public void setDisplaySubtext(String displaySubtext) {
        this.displaySubtext = displaySubtext;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getFormId() {
        return formId;
    }

    public void setFormId(String formId) {
        this.formId = formId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCanEditWhenComplete() {
        return canEditWhenComplete;
    }

    public void setCanEditWhenComplete(String canEditWhenComplete) {
        this.canEditWhenComplete = canEditWhenComplete;
    }

    public String getLastStatusChangeDate() {
        return lastStatusChangeDate;
    }

    public void setLastStatusChangeDate(String lastStatusChangeDate) {
        this.lastStatusChangeDate = lastStatusChangeDate;
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
            uriList.add(Uri.parse(instance.getUri()));
        }
        return uriList;
    }
}
