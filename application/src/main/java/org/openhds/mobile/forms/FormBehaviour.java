package org.openhds.mobile.forms;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.ElementFilter;
import org.jdom2.input.SAXBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class FormBehaviour {

    private final FormDefinition formDefinition;

    private String displayLevel;
    private FormContent displayFilter;
    private final List<String> consumers = new ArrayList<>();
    private final Map<String, FormContent> followUpFilters = new HashMap<>();
    private FormContent searchDefaults;

    public FormBehaviour(FormDefinition formDefinition) {
        this.formDefinition = formDefinition;
    }

    public boolean parseMetadata() {
        try {
            // read form definition into DOM document
            SAXBuilder builder = new SAXBuilder();
            Document document = builder.build(new File(formDefinition.getFilePath()));

            // locate the form metadata at root:head:model:instance:data:meta
            Element metaElement = getDescendantIgnoreNamespace(document.getRootElement(), "meta");
            if (null == metaElement) {
                return true;
            }

            parseDisplayCriteria(metaElement);
            parseConsumers(metaElement);
            parseFollowUps(metaElement);
            parseSearchDefaults(metaElement);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private void parseDisplayCriteria(Element metaElement) {
        Element displayLevelElement = getDescendantIgnoreNamespace(metaElement, "displayLevel");
        displayLevel = null == displayLevelElement ? null : displayLevelElement.getText();

        Element displayFilterElement = getDescendantIgnoreNamespace(metaElement, "displayFilter");
        displayFilter = null == displayFilterElement ? null : parseFormContent(displayFilterElement);
    }

    private void parseConsumers(Element metaElement) {
        List<Element> consumerElements = metaElement.getChildren("consumer");
        for (Element consumer : consumerElements) {
            String consumerName = consumer.getText().trim();
            if (!consumerName.isEmpty()) {
                consumers.add(consumerName);
            }
        }
    }

    private void parseFollowUps(Element metaElement) throws Exception {
        List<Element> followUpElements = metaElement.getChildren("followUp");
        for (Element followUp : followUpElements) {
            Element formIdElement = getDescendantIgnoreNamespace(followUp, "formId");
            if (null == formIdElement) {
                continue;
            }
            String formId = formIdElement.getText().trim();

            Element filterElement = getDescendantIgnoreNamespace(followUp, "filter");
            FormContent filter = null == filterElement ? null : parseFormContent(filterElement);

            followUpFilters.put(formId, filter);
        }
    }

    private void parseSearchDefaults(Element metaElement) throws Exception {
        Element searchDefaultsElement = getDescendantIgnoreNamespace(metaElement, "searchDefaults");
        searchDefaults = null == searchDefaultsElement ? null : parseFormContent(searchDefaultsElement);
    }

    private FormContent parseFormContent(Element element) {
        FormContent formContent = new FormContent();

        String alias = element.getName();
        List<Element> fieldElements = element.getChildren();
        for (Element fieldElement : fieldElements) {
            String fieldName = fieldElement.getName();
            formContent.setContent(alias, fieldName, fieldElement.getText());
        }

        return formContent;
    }

    private Element getDescendantIgnoreNamespace(Element element, String name) {
        Iterator<Element> descendants = element.getDescendants(new ElementFilter(name));
        if (!descendants.hasNext()) {
            return null;
        }
        return descendants.next();
    }

    public boolean shouldDisplayAtLevel(String level) {
        return null == displayLevel || displayLevel.equals(level);
    }

    public FormInstance createNewInstance() {
        return null;
    }
}