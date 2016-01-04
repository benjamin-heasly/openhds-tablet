package org.openhds.mobile.forms;

import android.content.ContentValues;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * Container for data going to or coming from a form instance.
 *
 * Has a small amount of structure: values are represented by alias and field name.  Aliases should
 * refer to an entity record by name or class name, for example "district" or "locationHierarchy".
 * Values should  refer to fields within a record, such as "uuid" or "extId".
 *
 */
public class FormContent {

    public static final String UUID_FIELD_NAME = "uuid";
    public static final String TOP_LEVEL_ALIAS = "topLevel";

    private final Map<String, ContentValues> contentByAlias = new HashMap<>();

    public Set<String> getAliases() {
        return contentByAlias.keySet();
    }

    public Set<String> getFieldNames(String alias) {
        return containsAlias(alias) ? getByAlias(alias).keySet() : new HashSet<String>();
    }

    public boolean hasContent(String alias) {
        return containsAlias(alias);
    }

    public boolean hasContent(String alias, String fieldName) {
        return containsAlias(alias) && getByAlias(alias).containsKey(fieldName);
    }

    // null if no content at alias and fieldName
    public String getContentString(String alias, String fieldName) {
        if (!hasContent(alias, fieldName)) {
            return null;
        }
        return getByAlias(alias).getAsString(fieldName);
    }

    // replace any existing content at the same alias
    public void setContent(String alias, ContentValues contentValues) {
        putByAlias(alias, contentValues);
    }

    // replace any existing content at the same alias and fieldName
    public void setContent(String alias, String fieldName, String value) {
        if (!containsAlias(alias)) {
            putByAlias(alias, new ContentValues());
        }
        getByAlias(alias).put(fieldName, value);
    }

    // replace any existing content that matches given content
    public void addAll(FormContent otherContent) {
        for (Map.Entry<String, ContentValues> entry : otherContent.contentByAlias.entrySet()) {
            String alias = entry.getKey();
            ContentValues contentValues = entry.getValue();
            for (String fieldName : contentValues.keySet()) {
                String value = contentValues.getAsString(fieldName);
                setContent(alias, fieldName, value);
            }
        }
    }

    // true iff all content in subset is present and equal in this
    public boolean matchesAll(FormContent subset) {
        for (Map.Entry<String, ContentValues> entry : subset.contentByAlias.entrySet()) {
            String alias = entry.getKey();
            if (!containsAlias(alias)) {
                return false;
            }

            ContentValues contentValues = entry.getValue();
            for (String fieldName : contentValues.keySet()) {
                if (!hasContent(alias, fieldName)) {
                    return false;
                }

                String value = contentValues.getAsString(fieldName);
                if (!value.equals(getContentString(alias, fieldName))) {
                    return false;
                }
            }
        }

        return true;
    }

    // treat alias as case insensitive
    private boolean containsAlias(String alias) {
        return contentByAlias.containsKey(alias.toLowerCase());
    }

    // treat alias as case insensitive
    private ContentValues getByAlias(String alias) {
        return contentByAlias.get(alias.toLowerCase());
    }

    // treat alias as case insensitive
    private ContentValues putByAlias(String alias, ContentValues contentValues) {
        if (null == contentValues || null == alias) {
            return null;
        }
        return contentByAlias.put(alias.toLowerCase(), contentValues);
    }

    public boolean initializeFormContent(File file, Element element) {
        try {
            // write new Dom to file
            element.detach();
            Document document = new Document();
            document.setRootElement(element);

            file.getParentFile().mkdirs();
            FileOutputStream fos = new FileOutputStream(file.getAbsolutePath());
            XMLOutputter xmlOutput = new XMLOutputter();
            xmlOutput.setFormat(Format.getPrettyFormat());
            xmlOutput.output(document, fos);
            fos.close();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public boolean updateFormContent(File file) {

        try {
            // read existing XML form into DOM document
            SAXBuilder builder = new SAXBuilder();
            Document document = builder.build(file);

            updateFormContent(document.getRootElement());

            // write modified Dom document back to file
            FileOutputStream fos = new FileOutputStream(file.getAbsolutePath());
            XMLOutputter xmlOutput = new XMLOutputter();
            xmlOutput.setFormat(Format.getPrettyFormat());
            xmlOutput.output(document, fos);
            fos.close();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public boolean updateFormContent(Element rootElement) {

        // match form content with XML elements by name
        List<Element> topLevelElements = rootElement.getChildren();
        Map<Element, String> toUpdate = new HashMap<>();
        for (Element topLevelElement : topLevelElements) {

            String topLevelName = topLevelElement.getName();

            // look for element fields under this top-level element
            List<Element> fieldElements = topLevelElement.getChildren();
            if (fieldElements.isEmpty()) {
                // element with no children represents a top-level field
                if (hasContent(TOP_LEVEL_ALIAS, topLevelName)) {
                    toUpdate.put(topLevelElement, getContentString(TOP_LEVEL_ALIAS, topLevelName));
                }

            } else {
                // each child element represents a field under the top-level alias
                for (Element fieldElement : fieldElements) {
                    String fieldName = fieldElement.getName();
                    if (hasContent(topLevelName, fieldName)) {
                        toUpdate.put(fieldElement, getContentString(topLevelName, fieldName));
                    }
                }

            }

            // special case hack for uuid references: treat fooUuid like <foo><uuid/></foo>
            if (topLevelName.toLowerCase().endsWith(UUID_FIELD_NAME)) {
                String alias = topLevelName.substring(0, topLevelName.length() - 4);
                if (hasContent(alias, UUID_FIELD_NAME)) {
                    toUpdate.put(topLevelElement, getContentString(alias, UUID_FIELD_NAME));
                }
            }

        }

        // set text of matched elements
        for (Map.Entry<Element, String> entry : toUpdate.entrySet()) {
            Element element = entry.getKey();
            String value = entry.getValue();
            element.setText(value);
        }

        return true;
    }

    public static FormContent readFormContent(File file) {
        FormContent formContent;

        try {
            // read XML form into DOM document
            SAXBuilder builder = new SAXBuilder();
            Document document = builder.build(file);
            formContent = readFormContent(document.getRootElement());

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return formContent;
    }

    public static FormContent readFormContent(Element rootElement) {
        FormContent formContent = new FormContent();

        // match form content with XML elements by name
        List<Element> topLevelElements = rootElement.getChildren();
        for (Element topLevelElement : topLevelElements) {

            String topLevelName = topLevelElement.getName();

            // look for element fields under this top-level element
            List<Element> fieldElements = topLevelElement.getChildren();
            if (fieldElements.isEmpty()) {
                // element with no children represents a top-level field
                formContent.setContent(TOP_LEVEL_ALIAS, topLevelName, topLevelElement.getText());

            } else {
                // each child element represents a field under the top-level alias
                for (Element fieldElement : fieldElements) {
                    String fieldName = fieldElement.getName();
                    formContent.setContent(topLevelName, fieldName, fieldElement.getText());
                }
            }

            // special case hack for uuid references: treat fooUuid like <foo><uuid/></foo>
            if (topLevelName.toLowerCase().endsWith(UUID_FIELD_NAME)) {
                String alias = topLevelName.substring(0, topLevelName.length() - 4);
                formContent.setContent(alias, UUID_FIELD_NAME, topLevelElement.getText());
            }
        }

        return formContent;
    }
}