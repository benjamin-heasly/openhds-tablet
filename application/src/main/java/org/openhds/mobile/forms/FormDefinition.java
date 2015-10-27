package org.openhds.mobile.forms;

/**
 * A form definition consistent with ODK form data model.
 */
public class FormDefinition {

    private String displayName;
    private String displaySubtext;
    private String description;
    private String id;
    private String version;
    private String date;
    private String filePath;
    private String submissionUri;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getSubmissionUri() {
        return submissionUri;
    }

    public void setSubmissionUri(String submissionUri) {
        this.submissionUri = submissionUri;
    }
}
