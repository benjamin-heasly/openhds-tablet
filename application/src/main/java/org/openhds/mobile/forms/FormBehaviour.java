package org.openhds.mobile.forms;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.ElementFilter;
import org.jdom2.input.SAXBuilder;
import org.openhds.mobile.repository.GatewayRegistry;
import org.openhds.mobile.repository.gateway.Gateway;
import org.openhds.mobile.repository.search.FormSearchPluginModule;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class FormBehaviour {

    public static final String LIST_SPLITTER = ",";
    public static final String META_ELEMENT_NAME = "meta";
    public static final String DISPLAY_LEVEL_ELEMENT_NAME = "displayLevel";
    public static final String DISPLAY_FILTER_ELEMENT_NAME = "displayFilter";
    public static final String CONSUMER_ELEMENT_NAME = "consumer";
    public static final String FOLLOW_UP_ELEMENT_NAME = "followUp";
    public static final String FOLLOW_UP_FORM_ID_ELEMENT_NAME = "formId";
    public static final String FOLLOW_UP_FILTER_ELEMENT_NAME = "filter";
    public static final String SEARCH_ELEMENT_NAME = "search";

    private final FormDefinition formDefinition;

    private final List<String> displayLevels = new ArrayList<>();
    private FormContent displayFilter;
    private final List<String> consumers = new ArrayList<>();
    private final Map<String, FormContent> followUpFilters = new HashMap<>();
    private FormContent searches;

    public FormBehaviour(FormDefinition formDefinition) {
        this.formDefinition = formDefinition;
    }

    public FormDefinition getFormDefinition() {
        return formDefinition;
    }

    public boolean parseMetadata() {
        try {
            // read form definition into DOM document
            SAXBuilder builder = new SAXBuilder();
            Document document = builder.build(new File(formDefinition.getFilePath()));

            // locate the form metadata at root:head:model:instance:data:meta
            Element metaElement = getDescendantIgnoreNamespace(document.getRootElement(), META_ELEMENT_NAME);
            if (null == metaElement) {
                return true;
            }

            parseDisplayCriteria(metaElement);
            parseConsumers(metaElement);
            parseFollowUps(metaElement);
            parseSearches(metaElement);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private void parseDisplayCriteria(Element metaElement) {
        Element displayLevelElement = getChildIgnoreNamespace(metaElement, DISPLAY_LEVEL_ELEMENT_NAME);
        if (null != displayLevelElement) {
            String[] levels = displayLevelElement.getText().split(LIST_SPLITTER);
            for (String level : levels) {
                displayLevels.add(level.trim());
            }
        }

        Element displayFilterElement = getChildIgnoreNamespace(metaElement, DISPLAY_FILTER_ELEMENT_NAME);
        displayFilter = null == displayFilterElement ? null : FormContent.readFormContent(displayFilterElement);
    }

    private void parseConsumers(Element metaElement) {
        List<Element> consumerElements = getChildrenIgnoreNamespace(metaElement, CONSUMER_ELEMENT_NAME);
        for (Element consumer : consumerElements) {
            String consumerName = consumer.getText().trim();
            if (!consumerName.isEmpty()) {
                consumers.add(consumerName);
            }
        }
    }

    private void parseFollowUps(Element metaElement) throws Exception {
        List<Element> followUpElements = getChildrenIgnoreNamespace(metaElement, FOLLOW_UP_ELEMENT_NAME);
        for (Element followUp : followUpElements) {
            Element formIdElement = getChildIgnoreNamespace(followUp, FOLLOW_UP_FORM_ID_ELEMENT_NAME);
            if (null == formIdElement) {
                continue;
            }
            String formId = formIdElement.getText().trim();

            Element filterElement = getChildIgnoreNamespace(followUp, FOLLOW_UP_FILTER_ELEMENT_NAME);
            FormContent filter = null == filterElement ? null : FormContent.readFormContent(filterElement);

            followUpFilters.put(formId, filter);
        }
    }

    private void parseSearches(Element metaElement) throws Exception {
        Element searchesElement = getChildIgnoreNamespace(metaElement, SEARCH_ELEMENT_NAME);
        searches = null == searchesElement ? null : FormContent.readFormContent(searchesElement);
    }

    private Element getDescendantIgnoreNamespace(Element element, String name) {
        Iterator<Element> descendants = element.getDescendants(new ElementFilter(name));
        if (!descendants.hasNext()) {
            return null;
        }
        return descendants.next();
    }

    private Element getChildIgnoreNamespace(Element element, String name) {
        List<Element> byName = getChildrenIgnoreNamespace(element, name);
        return byName.isEmpty() ? null : byName.get(0);
    }

    private List<Element> getChildrenIgnoreNamespace(Element element, String name) {
        List<Element> byName = new ArrayList<>();
        for (Element child : element.getChildren()) {
            if (child.getName().equals(name)) {
                byName.add(child);
            }
        }
        return byName;
    }

    public List<String> getDisplayLevels() {
        return displayLevels;
    }

    public boolean shouldDisplay(String level) {
        return displayLevels.isEmpty() || displayLevels.contains(level);
    }

    public boolean shouldDisplay(String level, FormContent formContent) {
        return shouldDisplay(level) && (null == displayFilter || formContent.matchesAll(displayFilter));
    }

    public List<String> getConsumers() {
        return Collections.unmodifiableList(consumers);
    }

    public List<String> getFollowUpForms(FormContent formContent) {
        List<String> followUpFormIds = new ArrayList<>();

        for (Map.Entry<String, FormContent> entry : followUpFilters.entrySet()) {
            String formId = entry.getKey();
            FormContent filterContent = entry.getValue();
            if (null == filterContent || formContent.matchesAll(filterContent)) {
                followUpFormIds.add(formId);
            }
        }

        return followUpFormIds;
    }

    public List<FormSearchPluginModule> getSearchPlugins() {
        List<FormSearchPluginModule> searchPluginModules = new ArrayList<>();

        if (null == searches) {
            return searchPluginModules;
        }

        for (String gatewayName : searches.getAliases()) {
            Gateway<?> gateway = GatewayRegistry.getGatewayByName(gatewayName);
            if (null == gateway) {
                continue;
            }

            for (String fieldName : searches.getFieldNames(gatewayName)) {
                String label = searches.getContentString(gatewayName, fieldName);
                searchPluginModules.add(new FormSearchPluginModule(gateway, label, fieldName, FormContent.TOP_LEVEL_ALIAS));
            }
        }

        return searchPluginModules;
    }

}
