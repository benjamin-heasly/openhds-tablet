package org.openhds.mobile.projectdata.FormAdapters;

import org.openhds.mobile.model.Location;
import org.openhds.mobile.projectdata.ProjectFormFields;

import java.util.Map;

import static org.openhds.mobile.OpenHDS.Locations.*;

public class LocationFormAdapter {

    public static Location fromForm(Map<String, String> formInstanceData) {
        Location location = new Location();

        location.setExtId(formInstanceData.get(
                ProjectFormFields.Locations.getFieldNameFromColumn(COLUMN_LOCATION_EXTID)));
        location.setName(formInstanceData.get(
                ProjectFormFields.Locations.getFieldNameFromColumn(COLUMN_LOCATION_NAME)));
        location.setHierarchyExtId(formInstanceData.get(
                ProjectFormFields.Locations.getFieldNameFromColumn(COLUMN_LOCATION_HIERARCHY)));
        location.setCommunityName(formInstanceData.get(
                ProjectFormFields.Locations.getFieldNameFromColumn(COLUMN_LOCATION_COMMUNITY_NAME)));
        location.setCommunityCode(formInstanceData.get(ProjectFormFields.Locations.getFieldNameFromColumn(COLUMN_LOCATION_COMMUNITY_CODE)));
        location.setLocalityName(formInstanceData.get(
                ProjectFormFields.Locations.getFieldNameFromColumn(COLUMN_LOCATION_LOCALITY_NAME)));
        location.setSectorName(formInstanceData.get(
                ProjectFormFields.Locations.getFieldNameFromColumn(COLUMN_LOCATION_SECTOR_NAME)));
        location.setMapAreaName(formInstanceData.get(
                ProjectFormFields.Locations.getFieldNameFromColumn(COLUMN_LOCATION_MAP_AREA_NAME)));
        location.setBuildingNumber(Integer.parseInt(formInstanceData.get(
                ProjectFormFields.Locations.getFieldNameFromColumn(COLUMN_LOCATION_BUILDING_NUMBER))));
        location.setFloorNumber(Integer.parseInt(formInstanceData.get(
                ProjectFormFields.Locations.getFieldNameFromColumn(COLUMN_LOCATION_FLOOR_NUMBER))));
        location.setRegionName(formInstanceData.get(
                ProjectFormFields.Locations.getFieldNameFromColumn(COLUMN_LOCATION_REGION_NAME)));
        location.setProvinceName(formInstanceData.get(
                ProjectFormFields.Locations.getFieldNameFromColumn(COLUMN_LOCATION_PROVINCE_NAME)));
        location.setSubDistrictName(formInstanceData.get(
                ProjectFormFields.Locations.getFieldNameFromColumn(COLUMN_LOCATION_SUB_DISTRICT_NAME)));
        location.setDistrictName(formInstanceData.get(
                ProjectFormFields.Locations.getFieldNameFromColumn(COLUMN_LOCATION_DISTRICT_NAME)));
        location.setHasRecievedBedNets(formInstanceData.get(
                ProjectFormFields.Locations.getFieldNameFromColumn(COLUMN_LOCATION_HAS_RECIEVED_BEDNETS)));
        location.setDescription(formInstanceData.get(
                ProjectFormFields.Locations.getFieldNameFromColumn(COLUMN_LOCATION_DESCRIPTION)));

        return location;
    }
}
