package org.openhds.mobile.forms;

import android.net.Uri;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FormInstance implements Serializable {

    public static final String UUID_FIELD_NAME = "uuid";
    public static final String TOP_LEVEL_ALIAS = "topLevel";

    private static final long serialVersionUID = 1L;

    private String formName;
    private String filePath;
    private String fileName;
    private String uriString;
    private String formVersion;

    public String getFormVersion() {
        return formVersion;
    }

    public void setFormVersion(String formVersion) {
        this.formVersion = formVersion;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

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

    public String getUriString() {
        return uriString;
    }

    public void setUriString(String uriString) {
        this.uriString = uriString;
    }

    public boolean writeFormContent(FormContent formContent) {

        try {
            // read XML form into DOM document
            SAXBuilder builder = new SAXBuilder();
            Document document = builder.build(new File(filePath));
            Element rootElement = document.getRootElement();

            // match form content with XML elements by name
            List<Element> topLevelElements = rootElement.getChildren();
            Map<Element, String> toUpdate = new HashMap<>();
            for (Element topLevelElement : topLevelElements) {

                String topLevelName = topLevelElement.getName();
                if (!formContent.hasContent(topLevelName)) {
                    continue;
                }

                // look for element fields under this top-level element
                List<Element> fieldElements = topLevelElement.getChildren();
                if (fieldElements.isEmpty()) {
                    // element with no children represents a top-level field
                    if (formContent.hasContent(TOP_LEVEL_ALIAS, topLevelName)) {
                        toUpdate.put(topLevelElement, formContent.getContentString(TOP_LEVEL_ALIAS, topLevelName));
                    }

                } else {
                    // each child element represents a field under the top-level alias
                    for (Element fieldElement : fieldElements) {
                        String fieldName = fieldElement.getName();
                        if (formContent.hasContent(topLevelName, fieldName)) {
                            toUpdate.put(fieldElement, formContent.getContentString(topLevelName, fieldName));
                        }
                    }

                }

                // special case hack for uuid references: treat fooUuid like <foo><uuid/></foo>
                if (topLevelName.toLowerCase().endsWith(UUID_FIELD_NAME)) {
                    String alias = topLevelName.substring(0, topLevelName.length() - 4);
                    if (formContent.hasContent(alias, UUID_FIELD_NAME)) {
                        toUpdate.put(topLevelElement, formContent.getContentString(alias, UUID_FIELD_NAME));
                    }
                }

            }

            // set text of matched elements
            for (Map.Entry<Element, String> entry : toUpdate.entrySet()) {
                Element element = entry.getKey();
                String value = entry.getValue();
                element.setText(value);
            }

            // write modified Dom document back to file
            FileOutputStream fos = new FileOutputStream(filePath);
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

    public FormContent readFormContent() {
        FormContent formContent = new FormContent();

        try {
            // read XML form into DOM document
            SAXBuilder builder = new SAXBuilder();
            Document document = builder.build(new File(filePath));
            Element rootElement = document.getRootElement();

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

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return formContent;
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
