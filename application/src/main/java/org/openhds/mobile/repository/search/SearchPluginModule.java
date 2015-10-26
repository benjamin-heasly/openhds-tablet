package org.openhds.mobile.repository.search;

import org.openhds.mobile.repository.gateway.Gateway;

import java.util.HashMap;
import java.util.Map;

/**
 * Represent a "search" that a user may need to perform.
 *
 * The plugin specifies behavior for an instance of SearchFragment:
 * a UI label for the search  being performed, which gateway to search,
 * a collection of column names to search using that gateway, and a UI
 * label for each column.
 *
 * The user would use the SearchFragment UI to supply column values for
 * the gateway query.
 *
 * BSH
 */
public class SearchPluginModule {

    private static final long serialVersionUID = 91916640L;

    protected Gateway gateway;
    protected String label;
    protected Map<String, Integer> columnsAndLabels;

    public SearchPluginModule() {}

    public SearchPluginModule(Gateway gateway, String label) {
        this.gateway = gateway;
        this.label = label;
        columnsAndLabels = new HashMap<>();
    }

    public Map<String, Integer> getColumnsAndLabels() {
        return columnsAndLabels;
    }

    public void setColumnsAndLabels(Map<String, Integer> columnsAndLabels) {
        this.columnsAndLabels = columnsAndLabels;
    }

    public Gateway getGateway() {
        return gateway;
    }

    public void setGateway(Gateway gateway) {
        this.gateway = gateway;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
