package org.openhds.mobile.forms;

import android.content.ContentValues;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * Container for data going to or coming from a form.
 *
 * Has a small amount of structure: values are represented by alias and field name.  Aliases should
 * refer to an entity record by name or class name, for example "district" or "locationHierarchy".
 * Values should  refer to fields within a record, such as "uuid" or "extId".
 *
 */
public class FormContent {

    private final Map<String, ContentValues> contentByAlias = new HashMap<>();

    public boolean hasContent(String alias) {
        return contentByAlias.containsKey(alias);
    }

    public boolean hasContent(String alias, String fieldName) {
        return contentByAlias.containsKey(alias) && contentByAlias.get(alias).containsKey(fieldName);
    }

    // null if no content at alias and fieldName
    public String getContentString(String alias, String fieldName) {
        if (!hasContent(alias, fieldName)) {
            return null;
        }
        return contentByAlias.get(alias).getAsString(fieldName);
    }

    // replace any existing content at the same alias
    public void setContent(String alias, ContentValues contentValues) {
        contentByAlias.put(alias, contentValues);
    }

    // replace any existing content at the same alias and fieldName
    public void setContent(String alias, String fieldName, String value) {
        if (!contentByAlias.containsKey(alias)) {
            contentByAlias.put(alias, new ContentValues());
        }
        contentByAlias.get(alias).put(fieldName, value);
    }
}
