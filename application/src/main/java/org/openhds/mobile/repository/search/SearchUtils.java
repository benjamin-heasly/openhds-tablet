package org.openhds.mobile.repository.search;

import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.repository.GatewayRegistry;

/**
 * Utility methods for working with Gateways, SearchFragment
 * and FormSearchActivity.
 *
 * For example, get pre-configured FormSearchPluginModules for
 * each gateway with "typical" labels and columns to search.
 */
public class SearchUtils {

    // Search for a field worker based on name and id.
    public static FormSearchPluginModule getFieldWorkerPlugin(String fieldName, String label) {
        FormSearchPluginModule plugin = new FormSearchPluginModule(
                GatewayRegistry.getFieldWorkerGateway(), label, fieldName, "collectedBy");
        plugin.getColumnsAndLabels().put(
                OpenHDS.FieldWorkers.FIRST_NAME, "First Name");
        plugin.getColumnsAndLabels().put(
                OpenHDS.FieldWorkers.LAST_NAME, "Last Name");
        plugin.getColumnsAndLabels().put(
                OpenHDS.FieldWorkers.FIELD_WORKER_ID, "Id");

        return plugin;
    }

    // Search for an individual based name and phone number.
    public static FormSearchPluginModule getIndividualPlugin(String fieldName, String label) {
        FormSearchPluginModule plugin = new FormSearchPluginModule(
                GatewayRegistry.getIndividualGateway(), label, fieldName, "individual");
        plugin.getColumnsAndLabels().put(
                OpenHDS.Individuals.FIRST_NAME, "First Name");
        plugin.getColumnsAndLabels().put(
                OpenHDS.Individuals.LAST_NAME, "Last Name");

        return plugin;
    }

    // Search for a location based on name, id, and location hierarchy names.
    public static FormSearchPluginModule getLocationPlugin(String fieldName, String label) {
        FormSearchPluginModule plugin = new FormSearchPluginModule(
                GatewayRegistry.getLocationGateway(), label, fieldName, "location");
        plugin.getColumnsAndLabels().put(
                OpenHDS.Locations.NAME, "Name");
        plugin.getColumnsAndLabels().put(
                OpenHDS.Locations.EXT_ID, "Id");

        return plugin;
    }

    // Search for a social group based on name and id.
    public static FormSearchPluginModule getSocialGroupPlugin(String fieldName, String label) {
        FormSearchPluginModule plugin = new FormSearchPluginModule(
                GatewayRegistry.getSocialGroupGateway(), label, fieldName, "socialGroup");
        plugin.getColumnsAndLabels().put(
                OpenHDS.SocialGroups.GROUP_NAME, "Group Name");
        plugin.getColumnsAndLabels().put(
                OpenHDS.SocialGroups.UUID, "Group Id");

        return plugin;
    }

}
