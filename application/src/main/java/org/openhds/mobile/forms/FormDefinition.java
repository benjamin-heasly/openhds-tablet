package org.openhds.mobile.forms;

/**
 * A form definition consistent with ODK form data model.
 *
 * <p/>
 * See: https://github.com/opendatakit/collect/blob/master/collect_app/src/main/java/org/odk/collect/android/provider/FormsProviderAPI.java
 * <p/>
 * public static final String DISPLAY_NAME = "displayName";
 * public static final String DESCRIPTION = "description";  // can be null
 * public static final String JR_FORM_ID = "jrFormId";
 * public static final String JR_VERSION = "jrVersion"; // can be null
 * public static final String FORM_FILE_PATH = "formFilePath";
 * public static final String SUBMISSION_URI = "submissionUri"; // can be null
 * public static final String BASE64_RSA_PUBLIC_KEY = "base64RsaPublicKey"; // can be null
 * <p/>
 * // these are generated for you (but you can insert something else if you want)
 * public static final String DISPLAY_SUBTEXT = "displaySubtext";
 * public static final String MD5_HASH = "md5Hash";
 * public static final String DATE = "date";
 * public static final String JRCACHE_FILE_PATH = "jrcacheFilePath";
 * public static final String FORM_MEDIA_PATH = "formMediaPath";
 *
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
