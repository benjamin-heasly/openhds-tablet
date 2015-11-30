package org.openhds.mobile.repository.search;

import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.R;
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
                OpenHDS.FieldWorkers.FIRST_NAME, R.string.field_worker_first_name_label);
        plugin.getColumnsAndLabels().put(
                OpenHDS.FieldWorkers.LAST_NAME, R.string.field_worker_last_name_label);
        plugin.getColumnsAndLabels().put(
                OpenHDS.FieldWorkers.FIELD_WORKER_ID, R.string.field_worker_id_label);

        return plugin;
    }

    // Search for an individual based name and phone number.
    public static FormSearchPluginModule getIndividualPlugin(String fieldName, String label) {
        FormSearchPluginModule plugin = new FormSearchPluginModule(
                GatewayRegistry.getIndividualGateway(), label, fieldName, "individual");
        plugin.getColumnsAndLabels().put(
                OpenHDS.Individuals.FIRST_NAME, R.string.individual_first_name_label);
        plugin.getColumnsAndLabels().put(
                OpenHDS.Individuals.LAST_NAME, R.string.individual_last_name_label);

        return plugin;
    }

    // Search for a location based on name, id, and location hierarchy names.
    public static FormSearchPluginModule getLocationPlugin(String fieldName, String label) {
        FormSearchPluginModule plugin = new FormSearchPluginModule(
                GatewayRegistry.getLocationGateway(), label, fieldName, "location");
        plugin.getColumnsAndLabels().put(
                OpenHDS.Locations.NAME, R.string.location_name_label);
        plugin.getColumnsAndLabels().put(
                OpenHDS.Locations.EXT_ID, R.string.location_ext_id_label);

        return plugin;
    }

    // Search for a social group based on name and id.
    public static FormSearchPluginModule getSocialGroupPlugin(String fieldName, String label) {
        FormSearchPluginModule plugin = new FormSearchPluginModule(
                GatewayRegistry.getSocialGroupGateway(), label, fieldName, "socialGroup");
        plugin.getColumnsAndLabels().put(
                OpenHDS.SocialGroups.GROUP_NAME, R.string.social_group_name);
        plugin.getColumnsAndLabels().put(
                OpenHDS.SocialGroups.UUID, R.string.social_group_ext_id);

        return plugin;
    }

}
