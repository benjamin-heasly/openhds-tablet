package org.openhds.mobile.projectdata.QueryHelpers;

import android.content.ContentResolver;
import org.openhds.mobile.projectdata.ProjectActivityBuilder;
import org.openhds.mobile.repository.DataWrapper;
import org.openhds.mobile.repository.GatewayRegistry;
import org.openhds.mobile.repository.gateway.IndividualGateway;
import org.openhds.mobile.repository.gateway.LocationGateway;
import org.openhds.mobile.repository.gateway.LocationHierarchyGateway;

import java.util.ArrayList;
import java.util.List;

public class CensusQueryHelper implements QueryHelper {

    // These must match the server data.
    // They come from the name column of the locationhierarchylevel table
    public static final String REGION_HIERARCHY_LEVEL_NAME = "Region";
    public static final String PROVINCE_HIERARCHY_LEVEL_NAME = "Province";
    public static final String DISTRICT_HIERARCHY_LEVEL_NAME = "District";
    public static final String SUB_DISTRICT_HIERARCHY_LEVEL_NAME = "SubDistrict";
    public static final String LOCALITY_HIERARCHY_LEVEL_NAME = "Locality";
    public static final String MAP_AREA_HIERARCHY_LEVEL_NAME = "MapArea";
    public static final String SECTOR_HIERARCHY_LEVEL_NAME = "Sector";

    public CensusQueryHelper() {}

    public List<DataWrapper> getAll(ContentResolver contentResolver, String state) {

        if (state.equals(ProjectActivityBuilder.CensusActivityModule.REGION_STATE)) {
            LocationHierarchyGateway locationHierarchyGateway = GatewayRegistry.getLocationHierarchyGateway();
            return locationHierarchyGateway.getQueryResultList(contentResolver,
                    locationHierarchyGateway.findByLevel(REGION_HIERARCHY_LEVEL_NAME), state);

        } else if (state.equals(ProjectActivityBuilder.CensusActivityModule.PROVINCE_STATE)) {
            LocationHierarchyGateway locationHierarchyGateway = GatewayRegistry.getLocationHierarchyGateway();
            return locationHierarchyGateway.getQueryResultList(contentResolver,
                    locationHierarchyGateway.findByLevel(PROVINCE_HIERARCHY_LEVEL_NAME), state);

        } else if (state.equals(ProjectActivityBuilder.CensusActivityModule.DISTRICT_STATE)) {
            LocationHierarchyGateway locationHierarchyGateway = GatewayRegistry.getLocationHierarchyGateway();
            return locationHierarchyGateway.getQueryResultList(contentResolver,
                    locationHierarchyGateway.findByLevel(DISTRICT_HIERARCHY_LEVEL_NAME), state);

        } else if (state.equals(ProjectActivityBuilder.CensusActivityModule.SUB_DISTRICT_STATE)) {
            LocationHierarchyGateway locationHierarchyGateway = GatewayRegistry.getLocationHierarchyGateway();
            return locationHierarchyGateway.getQueryResultList(contentResolver,
                    locationHierarchyGateway.findByLevel(SUB_DISTRICT_HIERARCHY_LEVEL_NAME), state);

        } else if (state.equals(ProjectActivityBuilder.CensusActivityModule.LOCALITY_STATE)) {
            LocationHierarchyGateway locationHierarchyGateway = GatewayRegistry.getLocationHierarchyGateway();
            return locationHierarchyGateway.getQueryResultList(contentResolver,
                    locationHierarchyGateway.findByLevel(LOCALITY_HIERARCHY_LEVEL_NAME), state);

        } else if (state.equals(ProjectActivityBuilder.CensusActivityModule.MAP_AREA_STATE)) {
            LocationHierarchyGateway locationHierarchyGateway = GatewayRegistry.getLocationHierarchyGateway();
            return locationHierarchyGateway.getQueryResultList(contentResolver,
                    locationHierarchyGateway.findByLevel(MAP_AREA_HIERARCHY_LEVEL_NAME), state);

        } else if (state.equals(ProjectActivityBuilder.CensusActivityModule.SECTOR_STATE)) {
            LocationHierarchyGateway locationHierarchyGateway = GatewayRegistry.getLocationHierarchyGateway();
            return locationHierarchyGateway.getQueryResultList(contentResolver,
                    locationHierarchyGateway.findByLevel(SECTOR_HIERARCHY_LEVEL_NAME), state);

        } else if (state.equals(ProjectActivityBuilder.CensusActivityModule.HOUSEHOLD_STATE)) {
            LocationGateway locationGateway = GatewayRegistry.getLocationGateway();
            return locationGateway.getQueryResultList(contentResolver, locationGateway.findAll(), state);

        } else if (state.equals(ProjectActivityBuilder.CensusActivityModule.INDIVIDUAL_STATE)) {
            IndividualGateway individualGateway = GatewayRegistry.getIndividualGateway();
            return individualGateway.getQueryResultList(contentResolver, individualGateway.findAll(), state);
        }

        return new ArrayList<DataWrapper>();
    }

    public DataWrapper getIfExists(ContentResolver contentResolver, String state, String extId) {

        if (state.equals(ProjectActivityBuilder.CensusActivityModule.REGION_STATE)
                || state.equals(ProjectActivityBuilder.CensusActivityModule.PROVINCE_STATE)
                || state.equals(ProjectActivityBuilder.CensusActivityModule.DISTRICT_STATE)
                || state.equals(ProjectActivityBuilder.CensusActivityModule.SUB_DISTRICT_STATE)
                || state.equals(ProjectActivityBuilder.CensusActivityModule.LOCALITY_STATE)
                || state.equals(ProjectActivityBuilder.CensusActivityModule.MAP_AREA_STATE)
                || state.equals(ProjectActivityBuilder.CensusActivityModule.SECTOR_STATE)) {

            LocationHierarchyGateway locationHierarchyGateway = GatewayRegistry.getLocationHierarchyGateway();
            return locationHierarchyGateway.getFirstQueryResult(
                    contentResolver, locationHierarchyGateway.findById(extId), state);

        } else if (state.equals(ProjectActivityBuilder.CensusActivityModule.HOUSEHOLD_STATE)) {
            LocationGateway locationGateway = GatewayRegistry.getLocationGateway();
            return locationGateway.getFirstQueryResult(contentResolver, locationGateway.findById(extId), state);

        } else if (state.equals(ProjectActivityBuilder.CensusActivityModule.INDIVIDUAL_STATE)) {
            IndividualGateway individualGateway = GatewayRegistry.getIndividualGateway();
            return individualGateway.getFirstQueryResult(contentResolver, individualGateway.findById(extId), state);
        }

        return null;
    }

    public List<DataWrapper> getChildren(ContentResolver contentResolver, DataWrapper qr, String childState) {
        String state = qr.getCategory();

        if (state.equals(ProjectActivityBuilder.CensusActivityModule.REGION_STATE)
                || state.equals(ProjectActivityBuilder.CensusActivityModule.PROVINCE_STATE)
                || state.equals(ProjectActivityBuilder.CensusActivityModule.DISTRICT_STATE)
                || state.equals(ProjectActivityBuilder.CensusActivityModule.SUB_DISTRICT_STATE)
                || state.equals(ProjectActivityBuilder.CensusActivityModule.LOCALITY_STATE)
                || state.equals(ProjectActivityBuilder.CensusActivityModule.MAP_AREA_STATE)) {

            LocationHierarchyGateway locationHierarchyGateway = GatewayRegistry.getLocationHierarchyGateway();
            return locationHierarchyGateway.getQueryResultList(contentResolver,
                    locationHierarchyGateway.findByParent(qr.getExtId()), childState);

        } else if (state.equals(ProjectActivityBuilder.CensusActivityModule.SECTOR_STATE)) {
            LocationGateway locationGateway = GatewayRegistry.getLocationGateway();
            return locationGateway.getQueryResultList(contentResolver,
                    locationGateway.findByHierarchyDescendingBuildingNumber(qr.getExtId()), childState);

        } else if (state.equals(ProjectActivityBuilder.CensusActivityModule.HOUSEHOLD_STATE)) {
            IndividualGateway individualGateway = GatewayRegistry.getIndividualGateway();
            return individualGateway.getQueryResultList(contentResolver,
                    individualGateway.findByResidency(qr.getExtId()), childState);
        }

        return new ArrayList<DataWrapper>();
    }
}
